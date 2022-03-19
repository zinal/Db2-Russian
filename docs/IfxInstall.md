# 1. Первоначальная установка сервера Informix

Инструкция подготовлена для операционной системы Linux на примере Informix 14.10, для более ранних версий есть отличия в порядке действий.
Инструкция готовилась и проверялась под управлением ОС CentOS 7.9, со следующими установленными дополнительными пакетами:

```bash
yum install -y libaio libgcc libstdc++ ncurses pam elfutils-libelf
```

Точный состав зависимостей приводится в документах Machine Notes к конкретной версии Informix, см.
[пример](https://www.ibm.com/docs/en/informix-servers/14.10?topic=notes-linux-x86-64).

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

Начиная с версии Informix 14.10, стандартный дистрибутив после установки
активирует редакцию Informix Developer Edition.
Для переключения на нужную (приобретённую) редакцию Informix используется компонент 
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

Пример команд для минимальной настройки каталога данных для размещения файлов
Informix, относящихся к серверу номер `0`:

```bash
# Выполняется от имени пользователя root
mkdir /ifxdata
mkdir /ifxdata/srv0
mkdir /ifxdata/srv0/tmp
chown -R informix:informix /ifxdata
chmod -R 770 /ifxdata
```

Пример команд для выделения пустого файла под root dbspace:

```bash
# Выполняется от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >rootdbs0
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
* `DIRECT_IO` - использование режиме direct io для исключения двойной буферизации файлов;
* `BUFFERPOOL` - настройка буферных пулов.

Пример изменённых настроек параметров в копии файла `onconfig.std`:

```
ROOTPATH /ifxdata/srv0/rootdbs0
PLOG_OVERFLOW_PATH  /ifxdata/srv0/tmp
MSGPATH /ifxdata/srv0/tmp/online.log
CONSOLE /ifxdata/srv0/tmp/console.log
DBSERVERNAME ifx1
DBSERVERALIASES ifx1_shm,ifx1_pub
MULTIPROCESSOR 1
VPCLASS cpu,num=4,noage
VP_MEMORY_CACHE_KB 16384,DYNAMIC
DIRECT_IO 1
```

## 1.6. Настройка конфигурации сетевых подключений

Для настройки подключений к серверу Informix по протоколу TCP необходимо
выделить номер порта сервиса и зарегистрировать его в файле сервисов `/etc/services`.
Пример строки файла сервисов для регистрации порта `35000` с именем `on_ifx`:

```
on_ifx		35000/tcp
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

Пример файла `sqlhosts` с настройкой сетевых подключений к серверу Informix 
по локальному протоколу общей памяти и по протоколу TCP (для внутреннего и публичного
IP-адресов):

```
ifx1        onsoctcp   ifx1       on_ifx
ifx1_shm    onipcshm   ifx1       on_ifx
ifx1_pub    onsoctcp   ifx1-pub   on_ifx
```

Имена `ifx1` и `ifx1-pub` могут быть связаны с IP-адресами в файле `/etc/hosts`,
либо установлены через службу DNS.

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

INFORMIXSERVER=ifx1
export INFORMIXSERVER

GL_USEGLU=1
export GL_USEGLU

DB_LOCALE=en_us.utf8
export DB_LOCALE

PATH=$PATH:$INFORMIXDIR/bin
export PATH

PS1='\u@\h$ '
export PS1

# End Of File
```

См. описание порядка настройки переменных окружения в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=installation-setting-environment-variables).

## 1.8. Инициализация сервера Informix

Перед инициализацией сервера Informix должны быть установлены переменные окружения 
`INFORMIXDIR`, `ONCONFIG`, `INFORMIXSQLHOSTS`, `INFORMIXSERVER`.

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
$ grep on_ifx /etc/services 
on_ifx		35000/tcp
$
$ ss -ln --tcp
State      Recv-Q Send-Q  Local Address:Port         Peer Address:Port
LISTEN     0      128                 *:22                      *:*
LISTEN     0      512    192.168.56.103:35000                   *:*
LISTEN     0      512     192.168.7.101:35000                   *:*
LISTEN     0      100         127.0.0.1:25                      *:*
LISTEN     0      128              [::]:22                   [::]:*
LISTEN     0      100             [::1]:25                   [::]:*
```

В выводе команд выше видно, что порт `35000`, назначенный для сервиса `on_ifx`, доступен для подключений.

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

Для Informix версии 12.10 и выше рекомендуется размещение физического лога
в отдельной области специального типа (plogspace), которая может быть создана
с помощью вызова команды
[onspaces](https://www.ibm.com/docs/en/informix-servers/14.10?topic=utility-onspaces-c-p-create-plogspace).

```bash
# Команды выполняются от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >plogspace0
chmod 660 plogspace0
# Создание plogspace и переключение на него
onspaces -c -P plogspace -p /ifxdata/srv0/plogspace0 -o 0 -s 2097152
```

Для Informix 11.70 и более ранних версий физический лог можно вынести
в отдельный dbspace, который предварительно должен быть создан.
Перенести в новый dbspace физическический лог можно с помощью команды
[onparams](https://www.ibm.com/docs/en/informix-servers/14.10?topic=log-change-physical-location-size).

```bash
# Команды выполняются от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >logspace0
chmod 660 logspace0
# Создание нового dbspace, состоящего из одного чанка размером 1 Гбайт
onspaces -c -d logspace -p /ifxdata/srv0/logspace0 -o 0 -s 1048576
# Перенос физического лога во вновь созданный dbspace
onparams -p -s 1048470 -d logspace -y
```

## 2.2. Создание основных областей хранения данных

Для хранения таблиц и индексов необходимо создать дополнительные объекты типа
[dbspace](https://www.ibm.com/docs/en/informix-servers/14.10?topic=storage-dbspaces)
Хранение пользовательских данных в root dbspace возможно, но крайне не рекомендовано.

Пример набора команд по созданию нового dbspace:

```bash
# Команды выполняются от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >work1_0
chmod 660 work1_0
# Создание dbspace work1, состоящего из одного чанка размером 2 Гбайт
onspaces -c -d work1 -p /ifxdata/srv0/work1_0 -o 0 -s 2097152
```

<details>
<summary>Пример вывода информации об областях хранения данных</summary>

```
$ onstat -l

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line -- Up 01:13:37 -- 208392 Kbytes

Physical Logging
Buffer bufused  bufsize  numpages   numwrits   pages/io
  P-1  51       64       315        20         15.75
      phybegin         physize    phypos     phyused    %used   
      2:53             1048523    438        51         0.00    

Logical Logging
Buffer bufused  bufsize  numrecs    numpages   numwrits   recs/pages pages/io
  L-3  0        32       3090       334        248        9.3        1.3     
	Subsystem    numrecs    Log Space used
	OLDRSAM      3061       322008        
	SBLOB        5          252           
	HA           24         1056          

address          number   flags    uniqid   begin                size     used    %used
4553a050         5        U-B----- 5        3:53                16384      187     1.14
4553a0b8         6        U-B----- 6        3:16437             16384       19     0.12
4553a120         7        U-B---L- 7        3:32821             16384       67     0.41
4553a188         8        U---C--- 8        3:49205             16384       67     0.41
4553a1f0         9        A------- 0        3:65589             16384        0     0.00
4553a258         10       A------- 0        3:81973             16384        0     0.00
4553a2c0         11       A------- 0        3:98357             16384        0     0.00
4553a328         12       A------- 0        3:114741            16384        0     0.00
 8 active, 8 total

$
$ onstat -d

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line -- Up 01:14:45 -- 208392 Kbytes

Dbspaces
address          number   flags      fchunk   nchunks  pgsize   flags    owner    name
45409028         1        0x20001    1        1        2048     N  BA    informix rootdbs
4553a420         2        0x1000001  2        1        2048     N PBA    informix plogspace
4553a660         3        0x1        3        1        2048     N  BA    informix llog
4553a8a0         4        0x1        4        1        2048     N  BA    informix work1
4f532cf8         5        0x2001     5        1        2048     N TBA    informix temp1
4ec166f0         6        0x8001     6        1        2048     N SBA    informix sbs1
 6 active, 2047 maximum

Chunks
address          chunk/dbs     offset     size       free       bpages     flags pathname
45409268         1      1      0          150000     139845                PO-B-D /ifxdata/srv0/rootdbs0
4eddd028         2      2      0          1048576    0                     PO-BED /ifxdata/srv0/plogspace0
4edde028         3      3      0          1048576    917451                PO-B-D /ifxdata/srv0/llog0
4eddf028         4      4      0          1048576    1048523               PO-B-D /ifxdata/srv0/work1_0
4ec54028         5      5      0          1048576    1048523               PO-B-- /ifxdata/srv0/temp1_0
4fa7f028         6      6      0          524288     488925     488925     POSB-D /ifxdata/srv0/sbs1_0
                                 Metadata 35310      26274      35310   
 6 active, 32766 maximum

NOTE: The values in the "size" and "free" columns for DBspace chunks are
      displayed in terms of "pgsize" of the DBspace to which they belong.


Expanded chunk capacity mode: always
```
</details>

## 2.3. Настройка размещения временных таблиц

Временные таблицы в Informix хранятся в специальных временных dbspace.
Порядок создания временных dbspace приведён есть в
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=dbspaces-creating-temporary-dbspace).

Ниже приведён пример команд по созданию временного dbspace.

```bash
# Команды выполняются от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >temp1_0
chmod 660 temp1_0
# Создание временного dbspace temp1 размером 2 Гбайт
onspaces -c -t -d temp1 -p /ifxdata/srv0/temp1_0 -o 0 -s 2097152
# Включить использование временного dbspace
onmode -wf DBSPACETEMP=temp1
```

## 2.4. Настройка размещения данных BLOB и CLOB

Данные BLOB и CLOB в Informix хранятся в специальных контейнерах, которые называются
[sbspace](https://www.ibm.com/docs/en/informix-servers/14.10?topic=storage-sbspaces).
Порядок создания sbspace приведён есть в
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=sbspaces-creating-sbspace).

Ниже приведён пример команд по созданию sbspace.

```bash
# Команды выполняются от имени пользователя informix
cd /ifxdata/srv0
cat /dev/null >sbs1_0
chmod 660 sbs1_0
# Создание sbspace sbs1 размером 1 Гбайт
onspaces -c -S sbs1 -p /ifxdata/srv0/sbs1_0 -o 0 -s 1048576
# Включить использование sbspace для хранения данных BLOB и CLOB
onmode -wf SBSPACENAME=sbs1
```

## 2.4. Настройка резервного копирования средствами Informix ON-Bar и Informix Primary Storage Manager

Простейший вариант организации резервного копирования Informix основан
на хранении резервных копий данных в файловой системе (на отдельных дисках,
сервере NFS/CIFS или на подключенной кластерной файловой системе).
Соответствующий сценарий описан в разделе о резервном копировании
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=ipsm-examples-manage-storage-devices-informix-primary-storage-manager).

Предварительно необходимо создать каталоги для хранения резервных копий,
например:

```bash
mkdir /backups/ifx1/logs
mkdir /backups/ifx1/spaces
```

Для хранения резервных копий Informix Primary Storage Manager по умолчанию использует
два пула: `LOGPOOL` для резервных копий логического лога и `DBSPOOL` для резервных
копий данных. Созданные каталоги необходимо включить в указанные пулы с помощью
следующих команд:

```bash
onpsm -D add /backups/ifx1/logs -g LOGPOOL -p HIGHEST -t FILE
onpsm -D add /backups/ifx1/spaces -g DBSPOOL -p HIGHEST -t FILE
```

Для настройки сжатия резервных копий с помощью утилиты `xz` необходимо выставить
параметры `BACKUP_FILTER` и `RESTORE_FILTER`. 

```bash
BACKUP_FILTER   'xz -2 -T4 -c'
RESTORE_FILTER  'xz -dc'
```

Для выполнения резервного копирования в параллельном режиме необходимо
установить параметр `BAR_MAX_BACKUP`.

```bash
onmode -wf BAR_MAX_BACKUP=2
```

Полное резервное копирование (создание резервной копии уровня 0) после этого
осуществляется с помощью следующей команды:

```bash
onbar -b -w -L 0
```

Проверить наличие и размещение созданных файлов резервной копии можно на основе
вывода следующей команды:

```bash
onpsm -O list
```

## 2.5. Изменение размеров и размещения логического лога

[Логический лог](https://www.ibm.com/docs/en/informix-servers/14.10?topic=log-what-is-logical)
используется Informix для ведения истории изменения данных с момента создания последней
резервной копии, а также при решении задач репликации данных.

Для размещения логического лога на отдельной группе устройств необходимо создать dbspace
и настроить сервер Informix для хранения логического лога в новом dbspace.

```bash
cd /ifxdata/srv0
cat /dev/null >llog0
chmod 660 llog0
onspaces -c -d llog -p /ifxdata/srv0/llog0 -o 0 -s 2097152
```

Изменение размера и количества файлов логического лога требует установки настроек
`LOGFILES` и `LOGSIZE`. Критерии установки соответствующих параментов приведены в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=file-performance-considerations).

Также часто используется параметр `AUTO_LLOG`, который разрешает серверу Informix автоматически
создавать дополнительные файлы логического лога в указанном dbspace. Пример настройки параметров,
влияющих на размер и создание файлов логического лога:

```
LOGFILES 8
LOGSIZE 65536
DYNAMIC_LOGS 2
LOGBUFF 64
AUTO_LLOG 1,llog,100000
```

Процедура переноса файлов логического лога в новый dbspace изложена в официальной
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=files-move-logical-log)
и сводится к выполнению следующих шагов:
1. созданию нового комплекта файлов логического лога;
2. переключению текущего файла логического лога на первый файл нового комплекта;
3. выполнению резервного копирования для освобождения существующих файлов логического лога;
4. удалению старых файлов логического лога;
5. выполнению резервного копирования для окончательного удаления старых файлов логического лога.

Добавление новых 8-ми файлов логического лога в ранее созданном dbspace `llog` может быть
выполнено следующей командой:

```bash
for i in `seq 1 8`; do onparams -a -d llog; done
```

Далее необходимо выполнить одно или несколько переключений текущего файла логического лога
с тем, чтобы текущим файлом стал один из вновь созданных. Переключение выполняется командой
`onmode -l`, контроль результата переключения выполняется на основе вывода команды `onstat -l`.

Перед удалением старых файлов логического лога их необходимо освободить, для чего следует
выполнить резервное копирование всех dbspace (см. предыдущий раздел, команда `onbar -b -L0`).

Затем старые файлы логического лога могут быть удалены командой `onparams -d -l НомерЛога`:

```bash
for x in `seq 1 6`; do onparams -d -l $x -y; done
```

Старые файлы логического лога в этот момент будут помечены для удаления.
Фактическое удаление и освобождение занятого ими пространства произойдёт при следующем полном
резервном копировании (например, командой `onbar -b -w -L 0`).
