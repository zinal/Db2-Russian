# 1. Первоначальная установка сервера Informix

Инструкция подготовлена для операционной системы Linux на примере Informix 14.10, для более ранних версий есть отличия в порядке действий.

## 1.1. Установка программного обеспечения Informix

Список [поддерживаемых операционных систем](http://www.ibm.com/support/docview.wss?uid=swg27013343).

Распаковка дистрибутива:

```bash
tar xf IDE_14.10.FC4W1_LINUX_X86_64.tar
```

Запуск инсталляционной программы:

```bash
sudo ./ids_install
```

При установке выбран режим "Custom", с полным набором компонентов, но без инициализации сервера Informix.

См. также инструкции в официальной 
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installing-running-interactive-installation)

## 1.2. Применение лицензионного ключа

Стандартный дистрибутив после установки активирует редакцию Informix Developer Edition.
Для переключения на нужную редакцию используется компонент 
["Edition Installer"](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installation-informix-edition-installer),
как показано ниже:

```bash
/opt/informix/jvm/jre/bin/java -jar ee_edition.jar -i console
```

[Коды редакций](https://www.ibm.com/support/pages/ibm-informix-version-number) в номере версии, выводимом через `onstat -`:
* WE : Workgroup Edition
* GE : Growth Edition
* IE : Innovator-C Edition
* EE : Express Edition
* CE : Choice Edition
* DE : Developer Edition

## 1.3. Настройка учётной записи informix

При инсталляции создаётся учётная запись `informix` - владелец сервиса.
По умолчанию домашний каталог учётной записи установлен в корневой каталог `/`, что неудобно для выполнения настройки профиля.
Для изменения домашнего каталога следующий набор команд, выполняемых от имени пользователя `root`:

```bash
mkdir /home/informix
chown informix:informix /home/informix
chmod 700 /home/informix
usermod -d /home/informix informix
```

## 1.4. Размещение данных Informix

Необходимо спланировать размещение данных Informix.
Для размещения файлов данных Informix рекомендуется использовать
специально выделенные устройства, либо отдельный каталог или каталоги,
не пересекающиеся с каталогами установки программного обеспечения.
Владельцем соответствующих каталогов и файлов должен быть пользователь `informix`.

Для высоко-нагруженных систем рекомендуется использование отдельных
устройств хранения или их групп для размещения следующих видов информации:
* программных файлов Informix и операционной системы;
* файлов данных (dbspace, sbspace);
* физических логов;
* логических логов;

Informix поддерживает хранение данных в файлах операционной системы (cooked files)
либо непосредственно на устройствах хранения (raw devices). При организации хранения
в файлах операционной системы файлы требуемого размера необходимо создать перед
их использованием в составе баз данных Informix. См. также
[документацию](https://www.ibm.com/docs/en/informix-servers/14.10?topic=chunks-unbuffered-buffered-disk-access-unix).

Пример команд для минимальной настройки каталога данных для размещения файлов Informix, относящихся к серверу с именем `ifx1`:

```bash
# Выполняется от имени пользователя root
mkdir /ifxdata
mkdir /ifxdata/ifx1
mkdir /ifxdata/ifx1/tmp
chown -R informix:informix /ifxdata
chmod -R 700 /ifxdata
```

Пример команд для выделения файла под root dbspace стандартного (300000 Кбайт) размера:

```bash
# Выполняется от имени пользователя informix
cd /ifxdata/ifx1
dd if=/dev/zero of=rootdbs0 bs=1000K count=300
chmod 660 rootdbs0
```

## 1.5. Настройка основных параметров сервера Informix

Порядок настройки основных параметров описан в 
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installation-setting-configuration-parameters).

Чаще всего производят копирование файла `onconfig.std` в новый файл,
имя которого обычно следует шаблону `onconfig.ИмяСервера`.
Затем необходимые параметры корректируют в созданной копии файла настроек.

```bash
cd /opt/informix/etc
cp onconfig.std onconfig.ifx1
vi onconfig.ifx1
```

Обычно требуется установить, как минимум, следующие параметры:
* `DBSERVERNAME` - основное имя сервера Informix;
* `DBSERVERALIASES` - дополнительные имена сервера Informix (используются для задания дополнительных протоколов доступа в файле `sqlhosts`);
* `ROOTPATH` - путь к файлу root dbspace;
* `PLOG_OVERFLOW_PATH` - путь к каталогу для размещения дополнительных файлов физического лога при его переполнении.

Часто также донастраиваются следующие параметры:
* `MSGPATH` - путь к файлу сообщений сервера Informix;
* `CONSOLE` - путь к файлу консольных сообщений сервера Informix;
* `MULTIPROCESSOR` - включение поддержки многоядерных и многопроцессорных систем;
* `VPCLASS` - количество разрешённых виртуальных процессоров;
* `VP_MEMORY_CACHE_KB` - размер кэша частной памяти виртуального процессора;
* `BUFFERPOOL` - настройка буферных пулов.

Пример изменённых настроек параметров в копии файла `onconfig.std`:

```
ROOTPATH /ifxdata/ifx1/rootdbs0
PLOG_OVERFLOW_PATH  /ifxdata/ifx1/tmp
MSGPATH /ifxdata/ifx1/tmp/online.log
CONSOLE /ifxdata/ifx1/tmp/online.con
DBSERVERNAME ifx1
DBSERVERALIASES ifx1_shm
MULTIPROCESSOR 1
VPCLASS cpu,num=4,noage
VP_MEMORY_CACHE_KB 16384,DYNAMIC
```

## 1.6. Настройка конфигурации сетевых подключений

Для настройки подключений к серверу Informix по протоколу TCP необходимо
выделить номер порта сервиса и зарегистрировать его в файле сервисов `/etc/services`.
Пример строки файла сервисов для регистрации порта `35000` с именем `on_ifx1`:

```
on_ifx1		35000/tcp
```

После установки сервера Informix в подкаталоге `etc` есть файл `sqlhosts.demo`,
который можно использовать в качестве прототипа файла настроек сетевых подключений.

```bash
cd /opt/informix/etc
cp sqlhosts.demo sqlhosts.ifx1
vi sqlhosts.ifx1
```

Описание настройки сетевых подключений Informix приведено в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=key-creating-sqlhosts-file-text-editor).

Пример файла `sqlhosts` с настройкой сетевых подключений к серверу Informix по локальному протоколу общей памяти и по протоколу TCP:

```
ifx1	onsoctcp	ifx1	on_ifx1
ifx1_shm	onipcshm	ifx1	on_ifx1_shm
```

## 1.7. Установка переменных окружения для учётной записи Informix

Значения переменных окружения для работы сервера Informix рекомендуется 
установить в файле настроек профиля пользователя `informix`, с тем, чтобы 
они автоматически устанавливались при входе в систему.

Пример файла `.profile` для пользователя `informix` приведён ниже:

```bash
 # .profile for informix

INFORMIXDIR=/opt/informix
export INFORMIXDIR

ONCONFIG=onconfig.ifx1
export ONCONFIG

INFORMIXSQLHOSTS=$INFORMIXDIR/etc/sqlhosts.ifx1
export INFORMIXSQLHOSTS

GL_USEGLU=1
export GL_USEGLU

DB_LOCALE=en_us.utf8
export DB_LOCALE

PATH=$PATH:$INFORMIXDIR/bin
export PATH

# End Of File
```

См. описание порядка настройки переменных окружения в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installation-setting-environment-variables).

## 1.8. Инициализация сервера Informix

Перед инициализацией сервера Informix должны быть установлены переменные окружения 
`INFORMIXDIR`, `ONCONFIG`, `INFORMIXSQLHOSTS`.

Для инициализации сервера Informix должна быть выполнена следующая команда:

```bash
oninit -i
```

При неправильной настройке инициализация может завершиться аварийно.
Сообщение об этом будет выведено в терминал, детальная информация доступна в файле сообщений
сервера, размещение которого установлено параметром `MSGPATH`.

После успешного завершения инициализации сервер Informix доступен и готов к использованию.
Проконтролировать доступность сервера Informix через TCP/IP можно по факту наличия прослушиваемого порта:

```
$ grep on_ifx1 /etc/services 
on_ifx1		35000/tcp
$ ss -ln --tcp
State      Recv-Q Send-Q      Local Address:Port                     Peer Address:Port              
LISTEN     0      128                     *:22                                  *:*                  
LISTEN     0      512         192.168.7.101:35000                               *:*                  
LISTEN     0      100             127.0.0.1:25                                  *:*                  
LISTEN     0      128                     *:50000                               *:*                  
LISTEN     0      128                  [::]:22                               [::]:*                  
LISTEN     0      100                 [::1]:25                               [::]:* 
```

В выводе команд выше видно, что порт `35000`, назначенный для сервиса `on_ifx1`, доступен для подключений.

Типичные действия по донастройке сервера Informix после его инициализации приведены в следующем разделе.

# 2. Донастройка сервера Informix после первоначальной установки

Конфигурация сервера Informix с параметрами по умолчанию расчитана на достаточно
скромные рабочие нагрузки и крайне ограниченные вычислительные ресурсы. В современных
условиях большинство инсталляций производят донастройку Informix.

## 2.1. Изменение размеров и размещения физического лога

[Физический лог](https://www.ibm.com/docs/en/informix-servers/14.10?topic=administration-physical-logging-checkpoints-fast-recovery)
используется Informix для обеспечения возможности отката незавершённых транзакций.
Размер физического лога должен быть достаточен для обработки текущего потока изменений данных,
рекомендации по оценке необходимого размера приведены в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=log-strategy-estimating-size-physical).

Рекомендуется размещение физического лога в отдельной области (plogspace), которая может быть создана
с помощью специального варианта вызова команд
[onspaces](https://www.ibm.com/docs/en/informix-servers/14.10?topic=utility-onspaces-c-p-create-plogspace)
и [onparams](https://www.ibm.com/docs/en/informix-servers/14.10?topic=log-change-physical-location-size):

```bash
# Команды ниже выполняются от имени пользователя informix
cd /ifxdata/ifx1
dd if=/dev/zero of=plogspace0 bs=1M count=2048
chmod 660 plogspace0
# Инициализация plogspace
onspaces -c -P plogspace -p /ifxdata/ifx1/plogspace0 -o 0 -s 2097152
# Перенос физического лога на созданный plogspace
onparams -p -s 2097152 -d plogspace
```

## 2.2. Изменение размеров и размещения логического лога

[Логический лог](https://www.ibm.com/docs/en/informix-servers/14.10?topic=log-what-is-logical)
используется Informix для ведения истории изменения данных с момента создания последней
резервной копии, а также при решении задач репликации данных.

## 2.3. Изменение размещения временных таблиц

## 2.4. Создание основных областей хранения данных

## 2.5. Создание баз данных
