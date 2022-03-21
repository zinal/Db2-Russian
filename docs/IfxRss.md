# Настройка вторичного сервера Informix в режиме RSS

Основная информация приведена в [документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=configuration-remote-standalone-secondary-servers).

В примерах команд и конфигурационных файлов далее предполагается, что основной сервер
был настроен по инструкции [IfxInstall].

## 1. Включить журналирование операций ведения индексов

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=logging-enable-disable-index-page).

Проверить, включено ли:

```bash
$ onstat -g ipl

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line -- Up 02:03:29 -- 208392 Kbytes
Index page logging status: Enabled
Index page logging was enabled at: 2022/03/15 11:58:52
```

Включить, если выключено:

```bash
onmode -wf LOG_INDEX_BUILDS=1
```


## 2. Включить по умолчанию журналирование объектов BLOB и CLOB

Для каждого sbspace проверить (`sbs1` - имя sbspace):

```bash
oncheck -pS sbs1 | grep "Create Flags"
```

Если в выводе присутствует `LO_LOG`, то журналирование включено.
Если же вместо указанного выше признака присутствует `LO_NOLOG`,
то журналирование выключено.

Включить журналирование можно следующей командой:

```bash
onspaces -ch sbs1 -Df "LOGGING=ON"
```

После включения журналирования требуется сделать полное
резервное копирование для каждого sbspace, для которого
было включено журналирование.


## 3. Настройка доверенного подключения на основном сервере

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=clusters-configuring-secure-connections-high-availability).

Для взаимодействия основного и вторичного серверов в рамках процесса репликации RSS требуется
предусмотреть дополнительный сервис TCP/IP, который на каждом из серверов должен быть
зарегистрирован для доверенного подключения в файле `sqlhosts` и активирован через
установку параметра `DBSERVERALIASES`.

Пример дополнительных записей файла `/etc/services` для нашей тестовой конфигурации,
с использованием порта `35000` для обычных подключений и порта `35001` для кластерных
коммуникаций между основным и вторичным серверами:

```
on_ifx		35000/tcp
on_ifx_dr	35001/tcp
```

На основном сервере необходимо добавить псевдоним для вторичного сервера Informix в файл `sqlhosts`.
Для записей, описывающих доверенные подключения, необходимо добавить опцию `s=6`.

Например, вот так может выглядеть содержимое файла `/opt/informix/etc/sqlhosts.ifx1`
на сервере `ifx1`, в котором были добавлены строки с псевдонимами `ifx1_dr` и `ifx2_dr`:

```
ifx1        onsoctcp   ifx1       on_ifx
ifx1_dr     onsoctcp   ifx1       on_ifx_dr  s=6
ifx1_shm    onipcshm   ifx1       on_ifx
ifx1_pub    onsoctcp   ifx1-pub   on_ifx
ifx2        onsoctcp   ifx2       on_ifx
ifx2_dr     onsoctcp   ifx2       on_ifx_dr  s=6
```

Также на основном сервере необходимо создать файл `hosts.equiv`, разместив его
в каталоге `$INFORMIXDIR/etc`. Каждая запись файла ставит в соответствие
имя хоста и разрешённый логин пользователя (для сервиса Informix - пользователь `informix`).
Рекомендуется указать имя сервера как в полном, так и в локальном формате, а также
(для унификации настроек основного и вторичного серверов) указать имена основного
и вторичного серверов.

Пример заполнения файла `hosts.equiv`:

```
# trustedhost username
ifx1               informix
ifx1.local         informix
ifx2               informix
ifx2.local         informix
```

Права доступа к файлу `hosts.equiv` необходимо ограничить, пример:

```bash
chmod 640 /opt/informix/etc/hosts.equiv.ifx1
```

Для включения использования файла `hosts.equiv` необходимо
установить параметры:
* `REMOTE_SERVER_CFG` в имя файла без указания пути;
* `S6_USE_REMOTE_SERVER_CFG` в значение `1`.

Для изменения значений параметров можно использовать
следующие команды:

```bash
onmode -wf REMOTE_SERVER_CFG=hosts.equiv.ifx1
onmode -wf S6_USE_REMOTE_SERVER_CFG=1
```

Дополнительно на основном сервере необходимо скорректировать параметр
`DBSERVERALIASES` и установить параметр `HA_ALIAS`, указывающий на
сетевой псевдоним основного сервера для межкластерных коммуникаций.

```bash
onmode -wf DBSERVERALIASES=ifx1_shm,ifx1_pub,ifx1_dr
onmode -wf HA_ALIAS=ifx1_dr
```

В примере выше значение DBSERVERALIASES включает псевдоним для подключений
по протоколу IPC (`ifx1_shm`), псевдоним для подключения приложений через
дополнительный публичный IP-адрес (`ifx1_pub`) и псевдоним для
RSS-подключений (`ifx1_dr`).


## 4. Подготовка вторичного сервера

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=servers-starting-rs-secondary-server-first-time).

1. Установить Informix.
2. Установить дополнительные модули UDR, UDT, DataBlade (если используются).
3. Создать необходимых пользователей операционной системы (например, `ifxguest`).
4. Выделить файловые системы, каталоги и устройства с именами, соответствующими основному серверу.
5. Подготовить файлы настроек Informix.

Пример набора команд для создания структуры каталогов и файлов данных,
аналогичной применяемой на основном сервере:

```bash
mkdir /ifxdata
mkdir /ifxdata/srv0
mkdir /ifxdata/srv0/tmp
chmod -R 700 /ifxdata
touch /ifxdata/srv0/rootdbs0
touch /ifxdata/srv0/plogspace0
touch /ifxdata/srv0/llog0
touch /ifxdata/srv0/work1_0
touch /ifxdata/srv0/sbs1_0
touch /ifxdata/srv0/temp1_0
chmod 660 /ifxdata/srv0/*0
chown -R informix:informix /ifxdata
```

На вторичном сервере необходимо настроить файлы `onconfig`, `sqlhosts`,
`hosts.equiv`, `allowed.surrogates` и переменные окружения.
За основу можно взять варианты файлов с основного сервера.

Пример команд по первоначальному копированию файлов:

```bash
scp /home/informix/.profile ifx2:/home/informix/
scp /etc/informix/allowed.surrogates ifx2:/etc/informix/allowed.surrogates
scp /opt/informix/etc/onconfig.ifx1 ifx2:/opt/informix/etc/onconfig.ifx2
scp /opt/informix/etc/sqlhosts.ifx1 ifx2:/opt/informix/etc/sqlhosts.ifx2
scp /opt/informix/etc/hosts.equiv.ifx1 ifx2:/opt/informix/etc/hosts.equiv.ifx2
```

Необходимые корректировки переменных окружения:
* `INFORMIXSERVER` - исправить имя сервера Informix;
* `ONCONFIG` - изменить имя файла `onconfig` (если отличается);
* `INFORMIXSQLHOSTS` - изменить имя файла `sqlhosts` (если отличается).

Необходимые корректировки параметров `onconfig`:
* `DBSERVERNAME`, `DBSERVERALIASES` - скорректировать для вторичного сервера;
* `HA_ALIAS` - скорректировать для вторичного сервера;
* `REMOTE_SERVER_CFG` - изменить имя файла `hosts.equiv` (если отличается).

Необходимые корректировки файла `sqlhosts` на вторичном сервере:
* удалить псевдонимы для локальных протоколов основного сервера (протокол shm, частные IP-адреса);
* добавить аналогичные псевдонимы для локальных протоколов вторичного сервера;
* сохранить псевдонимы для сетевых подключений к основному серверу.

Пример наполнения файла `sqlhosts` на вторичном сервере `ifx2`:

```
ifx2        onsoctcp   ifx2       on_ifx
ifx2_dr     onsoctcp   ifx2       on_ifx_dr  s=6
ifx2_shm    onipcshm   ifx2       on_ifx
ifx2_pub    onsoctcp   ifx2-pub   on_ifx
ifx1        onsoctcp   ifx1       on_ifx
ifx1_dr     onsoctcp   ifx1       on_ifx_dr  s=6
```


## 5. Первоначальный запуск вторичного сервера Informix в режиме RSS

### Шаг 1 - регистрация вторичного сервера на основном

На основном сервере - добавить информацию о вторичном сервере RSS, выполнив команду:

```bash
onmode -d add RSS ifx2_dr password
```

Здесь `ifx2_dr` - имя псевдонима подключения для вторичного сервера RSS, определённое
в файле `sqlhosts` на основном сервере.

Аргумент `password` опциональный, и позволяет избежать неожиданных
подключений "посторонних" вторичных серверов RSS с подменой сетевого адреса.

Проверить результат выполнения команды можно, вызвав `onstat -g rss`.

<details>
<summary>Пример вывода `onstat -g rss`</summary>

```
$ onstat -g rss

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line -- Up 02:46:34 -- 208392 Kbytes

Local server type: Primary
Index page logging status: Enabled
Index page logging was enabled at: 2022/03/15 11:58:52
Number of RSS servers: 1

RSS Server information:

RSS Srv      RSS Srv      Connection     Next LPG to send        Supports
name         status       status           (log id,page)         Proxy Writes
ifx2_dr      Defined      Disconnected            0,0            N

$
```
</details>

### Шаг 2 - восстановление из резервной копии на вторичном сервере

На вторичном сервере - выполнить восстановление данных из резервной копии с основного сервера.

Может использоваться уже существующая полная резервная копия,
которую необходимо сделать доступной на вторичном сервере.

Альтернативный вариант - процесс восстановления может быть совмещён с процессом
резервного копирования в потоковом режиме.

Ниже приводятся команды для альтернативного варианта.

(а) Подготовить скрипт и сохранить на основном сервере как файл `ontape_STDIO_backup.sh`
    в домашнем каталоге пользователя `informix`.

```bash
#! /bin/sh
. /home/informix/.profile
ontape -s -L 0 -F -t STDIO
```

Разрешить выполнение скрипта на основном сервере:

```bash
chmod +x ontape_STDIO_backup.sh
```

(б) Запустить параллельно резервное копирование на основном сервере
    и восстановление на вторичном сервере в потоковом режиме

Для этого на вторичном сервере от имени пользователя `informix` выполняется команда:

```bash
ssh ifx1 ./ontape_STDIO_backup.sh | ontape -p -t STDIO
```

Рекомендуется использование сессии `screen` или `tmux`, чтобы
избежать прерывания выполнения длительной операции при закрытии
терминала.

После завершения этого шага необходимо немедленно перейти к шагу 3.
Перезапуск вторичного сервера Informix в этот момент приведёт
к необходимости повтора выполнения шага 2.

### Шаг 3 - включение RSS на вторичном сервере

Включить режим RSS на вторичном сервере, выполнив следующую команду:

```bash
onmode -d RSS ifx1_dr password
```

Здесь `ifx1_dr` - имя псевдонима подключения к основному серверу, определённое
в файле `sqlhosts на вторичном сервере.

Аргумент `password` опциональный, и должен быть установлен в значение,
использованное на шаге 1.

### Шаг 4 - проверка состояния RSS

Проверить состояние RSS на основном и вторичном серверах с помощью
команды `onstat -g rss`.

<details>
<summary>Пример вывода на основном сервере</summary>

```
$ onstat -g rss

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line -- Up 05:27:59 -- 208392 Kbytes

Local server type: Primary
Index page logging status: Enabled
Index page logging was enabled at: 2022/03/15 11:58:52
Number of RSS servers: 1

RSS Server information:

RSS Srv      RSS Srv      Connection     Next LPG to send        Supports
name         status       status           (log id,page)         Proxy Writes
ifx2_dr      Active       Connected              20,45823        N
```
</details>

<details>
<summary>Пример вывода на вторичном сервере</summary>

```
$ onstat -g rss

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- Read-Only (RSS) -- Up 01:03:13 -- 273928 Kbytes

Local server type: RSS
Server Status : Active
Source server name: ifx1_dr
Connection status: Connected
Last log page received(log id,page): 20,45822
```
</details>

### Шаг 5 - применение недостающих изменений на вторичном сервере

Если часть файлов логического лога, созданных с момента создания резервной копии, отсутствует
на с основном сервере (была удалена в ходе резервного копирования), их будет необходимо
применить на вторичном сервере из резервной копии с помощью команды `ontape -l` либо `onbar -r -l`.

Доступ к резервным копиям должен быть настроен путём установки соответствующих параметров
на вторичном сервере Informix. Согласованная конфигурация резервного копирования и восстановления
данных, унифицированная между основным и вторичным серверами, в любом случае необходима
для обеспечения возможности полноценного переключения ролей между ними.


# Операции над кластерами Informix RSS и HDR

## Перевод вторичного сервера RSS в режим HDR

RSS - полностью асинхронный режим репликации данных с основного на вторичный сервер Informix.
HDR - полностью либо частично синхронный режим репликации, также поддерживаемый Informix.
Вторичный сервер HDR может быть только один, при этом производительность вторичного сервера
и канал связи между первичным и вторичным сервером HDR влияет на производительность выполнения
транзакций основным сервером.

Для переключения вторичного сервера RSS в режим HDR необходимо на вторичном сервере выполнить
следующую команду от имени пользователя `informix`:

```bash
onmode -d secondary ifx1_dr
```

В приведённом выше примере `ifx1_dr` - заданный в файле `sqlhosts` псевдоним для подключения
к основному серверу в RSS/HDR кластере.

Проконтролировать успешное выполнение операции переключения в режим HDR можно по содержимому
файла сообщений `online.log`, а также по выводу команды `onstat -g dri` на основном
и вторичном серверах.

<details>
<summary>Пример вывода на основном сервере `ifx1`</summary>

```
$ onstat -g dri

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- On-Line (Prim) -- Up 01:53:47 -- 224776 Kbytes

Data Replication at 0x463cc028: 
  Type           State        Paired server        Last DR CKPT (id/pg)    Supports Proxy Writes   
  primary        on           ifx2_dr                      31 / 4836       NA

  DRINTERVAL    0 
  DRTIMEOUT     30 
  HDR_TXN_SCOPE NEAR_SYNC 
  DRAUTO        0 
  DRLOSTFOUND   /opt/informix/etc/dr.lostfound 
  DRIDXAUTO     0 
  ENCRYPT_HDR   0 
  Backlog       0 
  Last Send     2022/03/17 11:12:53 
  Last Receive  2022/03/17 11:12:53 
  Last Ping     2022/03/17 11:12:24 
  Last log page applied(log id,page): 31,4837
```
</details>

<details>
<summary>Пример вывода на вторичном сервере `ifx2`</summary>

```
$ onstat -g dri

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- Read-Only (Sec) -- Up 01:52:21 -- 273928 Kbytes

Data Replication at 0x463cc028: 
  Type           State        Paired server        Last DR CKPT (id/pg)    Supports Proxy Writes   
  HDR Secondary  on           ifx1_dr                      31 / 4836       N 

  DRINTERVAL    0 
  DRTIMEOUT     30 
  HDR_TXN_SCOPE NEAR_SYNC 
  DRAUTO        0 
  DRLOSTFOUND   /opt/informix/etc/dr.lostfound 
  DRIDXAUTO     0 
  ENCRYPT_HDR   0 
  Backlog       0 
  Last Send     2022/03/17 11:13:02 
  Last Receive  2022/03/17 11:13:02 
  Last Ping     2022/03/17 11:12:53 
  Last log page applied(log id,page): 0,0
```
</details>


## Перевод вторичного сервера HDR в режим RSS

Для перевода существующего вторичного сервера HDR в режим вторичного сервера RSS
требуется выполнить следующие действия.

### Шаг 1 - регистрация вторичного сервера RSS на основном сервере

Выполняется путём выполнения на основном сервере команды:

```bash
onmode -d add RSS ifx2_dr passw0rd
```

В команде выше `ifx2_dr` - имя псевдонима доверенного подключения к вторичному
серверу, заданное в файле `sqlhosts` на основном сервере.

Аргумент `passw0rd` - опциональный и используется для аутентификации первоначального
подключения вторичного сервера RSS к основному серверу.

### Шаг 2 - переключение вторичного сервера в режим RSS

Выполняется путём выполнения на вторичном сервере команды:

```bash
onmode -d RSS ifx1_dr passw0rd
```

В команде выше `ifx1_dr` - имя псевдонима доверенного подключения к основному
серверу, заданное в файле `sqlhosts` на вторичном сервере.

Аргумент `passw0rd` - опциональный, и должен быть равен значению, использованному
на шаге 1.

### Шаг 3 - контроль состояния RSS

Проверить состояние RSS на основном и вторичном серверах с помощью
команды `onstat -g rss`.

Вывод команд аналогичен приведённому выше для первоначальной настройки RSS
(см. шаг 4 подраздела 5).


## Переключение ролей между основным и вторичным серверами

Переключение ролей между основным и вторичным серверами возможно только в конфигурации HDR.
Если требуется перевести вторичный RSS-сервер в статус основного, эта операция выполняется
в три этапа:
1. перейти к режиму HDR;
2. выполнить переключение ролей по описанной ниже процедуре;
3. перейти к режиму RSS.

Переключение ролей предполагает кратковременную приостановку доступа пользователей
к базам данных и сервисам Informix.

Первоначальное состояние: `ifx1` - основной сервер, `ifx2` - вторичный сервер HDR.

### Шаг 1 - остановка сервера `ifx1`

На текущем основном сервере `ifx1` выполняется команда:

```bash
onmode -ky
```

### Шаг 2 - перевод сервера `ifx2` в режим первичного

На текущем вторичном сервере `ifx2` запускается скрипт `hdrmkpri.sh`.
В результате происходит переключение вторичного сервера HDR
в режим работы первичного сервера и его остановка.

<details>
<summary>Пример вывода скрипта `hdrmkpri.sh`</summary>

```
$ hdrmkpri.sh

This script changes the type of the Data replication server to Primary.

Steps to switch server types in an HDR pair:

  Instance A (currently Primary)          Instance B (currently Secondary)
  ------------------------------          --------------------------------
  1] onmode -ky                              (server should be up)
                                          2] hdrmkpri.sh <primary_server_name>
  3] hdrmksec.sh <secondary_server_name>
     (now a Secondary server)             4] oninit (now a Primary server)

WARNING: Please ensure the following before proceeding further:

           1] the paired  database server is OFFLINE,
           2] the current database server is up, and
           3] the current database server has Data replication turned off.

         Not doing so, shall make the two database servers in the
         Data replication pair out-of-sync, and shall require
         re-establishing the pair.

Press return to continue...

 
Replication Status: Type          : Secondary
                    State         : off
                    Paired server : ifx1_dr
 
Changing to Standard type via:
	onmode -d standard
 
Changing to Primary type via: (could take a couple of minutes)
	onmode -d primary ifx1_dr
 
Shutting down the database server via:
	onmode -ky
 
NOTE: Shutting down the database server now is MANDATORY, because
      if the two database servers in a Data replication pair
      are brought up as Primary/Standard type OR,
      if one database server is up and the mode of the paired server
      is changed, then the database servers in the pair could
      become out-of-sync, leading to establishing the pair again.
 
Run $INFORMIXDIR/bin/hdrmksec.sh on the paired server.
After the run OR if it has already been run, then WITHOUT bringing the
paired (now Secondary) server down, start the current (now Primary)
database server, to make the Data replication pair operational.
```
</details>

### Шаг 3 - перевод сервера `ifx1` в режим вторичного

На первоначальном первичном сервере `ifx1` запускается скрипт `hdrmksec.sh`.
В результате происходит переключение его в режим работы вторичного сервера HDR.
После выполнения шага сервер `ifx1` запущен, и пытается подключиться
к новому первичному серверу `ifx2`.

<details>
<summary>Пример вывода скрипта `hdrmksec.sh`</summary>

```
$ hdrmksec.sh 

This script changes the type of the Data replication server to Secondary.

Steps to switch server types in an HDR pair:

  Instance A (currently Primary)          Instance B (currently Secondary)
  ------------------------------          --------------------------------
  1] onmode -ky                              (server should be up)
                                          2] hdrmkpri.sh <primary_server_name>
  3] hdrmksec.sh <secondary_server_name>
     (now a Secondary server)             4] oninit (now a Primary server)

WARNING: Please ensure the following before proceeding further:

           1] the paired  database server is OFFLINE, and
           2] the current database server has Data replication turned off.

         Not doing so, shall make the two database servers in the
         Data replication pair out-of-sync, and shall require
         re-establishing the pair.

Press return to continue...

 
Current database server is down/off-line.
 
Run $INFORMIXDIR/bin/hdrmkpri.sh on the paired server.
After the run OR if it has already been run, then
Press Return to continue ...

 
Enter the paired server name and press return:
ifx2_dr
 
Starting the database server via :
	oninit -PHY
  Note : -PHY is an undocumented option only used in this
         HDR failover script
 
Changing to Secondary type via: (could take a couple of minutes)
	onmode -d secondary ifx2_dr
 
WARNING: The current database server should NOT be shutdown now.
         If it is shutdown for any reason, then you need to re-run
         this script again from the start.
 
Start the paired (now Primary) database server, while the current
(now Secondary) database server tries to connect to it, to make the
Data replication pair operational.
```
</details>

### Шаг 4. Запуск нового первичного сервера `ifx2`.

На сервере `ifx2` выполняется команда `oninit`.

Состояние кластера HDR может быть проконтролировано по содержимому файла
сообщений `online.log`, а также исходя из вывода команды `onstat -g dri`
на основном и вторичном серверах.

Для Informix начиная с версии 12.10.fc8 будет выдана ошибка `DR_ERR set to -25`,
и новый вторичный сервер `ifx1` будет аварийно завершён. Это ожидаемое поведение,
описанное в [документации](https://www.ibm.com/support/pages/drerr-25-turning-hdr-primary-secondary-starting-1210fc8).
Для продолжения работы необходимо повторно запустить новый вторичный сервер
`ifx1` командой `oninit`, и проконтролировать успешное подключение основного
и вторичного серверов HDR.


## Удаление вторичного сервера RSS

Операция удаления вторичного сервера RSS выполняется в следующем порядке:
1. Остановить вторичный сервер RSS, если он работает (например, командой `onmode -ky`).
2. Выполнить основном сервере команду `onmode -d delete RSS rss_servername`.

## Удаление вторичного сервера HDR

Операция удаления вторичного сервера HDR выполняется в следующем порядке:
1. Остановить вторичный сервер HDR, если он работает (например, командой `onmode -ky`).
2. Выполнить основном сервере команду `onmode -d standard`.
