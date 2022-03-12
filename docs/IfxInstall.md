# Первоначальная установка и настройка сервера Informix

Инструкция подготовлена для операционной системы Linux на примере Informix 14.10, для более ранних версий есть отличия в порядке действий.

## 1. Установка программного обеспечения Informix

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

## 2. Применение лицензионного ключа

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

## 3. Настройка учётной записи informix

При инсталляции создаётся учётная запись `informix` - владелец сервиса.
По умолчанию домашний каталог учётной записи установлен в корневой каталог `/`, что неудобно для выполнения настройки профиля.
Для изменения домашнего каталога следующий набор команд, выполняемых от имени пользователя `root`:

```bash
mkdir /home/informix
chown informix:informix /home/informix
chmod 700 /home/informix
usermod -d /home/informix informix
```

## 4. Размещение данных Informix

Необходимо спланировать размещение данных Informix.
В целом для размещения файлов данных Informix рекомендуется использовать отдельный каталог или каталоги, не пересекающиеся с каталогами установки программного обеспечения.
Владельцем соответствующих каталогов и файлов должен быть пользователь `informix`.

Для высоко-нагруженных систем рекомендуется использование отдельных устройств хранения или их групп для размещения следующих видов информации:
* программных файлов Informix и операционной системы;
* файлов данных Informix (dbspace, sbspace);
* физических и логических логов Informix.

Использование прямого доступа к устройствам для хранения данных Informix в современных системах не рекомендовано, хотя и поддерживается.

Пример команд для минимальной настройки каталога данных для размещения файлов Informix, относящихся к серверу с именем `ifx1`:

```bash
mkdir /ifxdata
mkdir /ifxdata/ifx1
mkdir /ifxdata/ifx1/tmp
chown -R informix:informix /ifxdata
chmod -R 700 /ifxdata
```

## 5. Настройка основных параметров сервера Informix

Порядок настройки основных параметров описан в 
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installation-setting-configuration-parameters).

Чаще всего производят копирование файла `onconfig.std` в новый файл, имя которого обычно следует шаблону `onconfig.ИмяСервера`.
Затем необходимые параметры корректируют в созданной копии файла настроек.

```bash
cd /opt/informix/etc
cp onconfig.std onconfig.ifx1
vi onconfig.ifx1
```

Обычно требуется установить, как минимум, следующие параметры:
* `DBSERVERNAME` - имя сервера Informix;
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
ROOTPATH /ifxdata/ifx1/rootdbs
PLOG_OVERFLOW_PATH  /ifxdata/ifx1/tmp
MSGPATH /ifxdata/ifx1/tmp/online.log
CONSOLE /ifxdata/ifx1/tmp/online.con
DBSERVERNAME ifx1
MULTIPROCESSOR 1
VPCLASS cpu,num=4,noage
VP_MEMORY_CACHE_KB 16384,DYNAMIC
```

## 6. Настройка конфигурации сетевых подключений

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
ifx1	onipcshm	ifx1	on_ifx1
ifx1	onsoctcp	ifx1	on_ifx1
```

## 7. Установка переменных окружения для учётной записи Informix

Значения переменных окружения для работы сервера Informix рекомендуется 
установить в файле настроек профиля пользователя `informix`, с тем, чтобы 
они автоматически устанавливались при входе в систему.

Пример файла `.profile` для пользователя `informix` приведён ниже:

```bash
 # .profile for informix

INFORMIXDIR=/opt/informix
export INFORMIXDIR

ONCONFIG=$INFORMIXDIR/etc/onconfig.ifx1
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
