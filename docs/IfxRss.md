# Настройка резервного сервера Informix в режиме RSS

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

$
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

На основном сервере необходимо добавить псевдоним для резервного сервера Informix в файл `sqlhosts`.
Кроме того, для записей, относящихся к локальному сервису Informix, необходимо добавить опцию `s=6`

Например, вот так может выглядеть содержимое файла `/opt/informix/etc/sqlhosts.ifx1` на сервере `ifx1`:

```
ifx1            onsoctcp	ifx1            on_ifx1         s=6
ifx1_shm	onipcshm	ifx1	        on_ifx1_shm     s=6
ifx1_pub	onsoctcp	ifx1-pub	on_ifx1         s=6
ifx2	        onsoctcp	ifx2	        on_ifx1
```

Была добавлена последняя строка, которая определяет параметры соединения для сервера `ifx2`
с использованием протокола TCP/IP.
Имя сервиса (последний параметр) такое же, как и для сервера `ifx1`,
так как номер порта в нашем примере используется один и тот же.


Также на основном сервере необходимо создать файл `hosts.equiv`, разместив его
в каталоге `$INFORMIXDIR/etc`. Каждая запись файла ставит в соответствие
имя хоста и разрешённый логин пользователя (для сервиса Informix - пользователь `informix`).

Рекомендуется указать имя сервера как в полном, так и в локальном
формате, а также (для унификации настроек основного и резервного
серверов) указать имена основного и резервного серверов.

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

Дополнительно на основном сервере необходимо установить параметр `HA_ALIAS`,
указывающий на основной сетевой псевдоним основного сервера.

```bash
onmode -wf HA_ALIAS=ifx1
```


## 4. Подготовка резервного сервера

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=servers-starting-rs-secondary-server-first-time).

1. Установить Informix.
2. Установить дополнительные модули UDR, UDT, DataBlade (если используются).
3. Выделить файловые системы, каталоги и устройства с именами, соответствующими основному серверу.
4. Подготовить файлы настроек Informix.

Пример набора команд для создания структуры каталогов и файлов данных,
аналогичной применяемой на основном сервере:

```bash
mkdir /ifxdata
mkdir /ifxdata/ifx1
mkdir /ifxdata/ifx1/tmp
chmod -R 700 /ifxdata
touch /ifxdata/ifx1/rootdbs0
touch /ifxdata/ifx1/plogspace0
touch /ifxdata/ifx1/llog0
touch /ifxdata/ifx1/work1_0
touch /ifxdata/ifx1/sbs1_0
touch /ifxdata/ifx1/temp1_0
chmod 660 /ifxdata/ifx1/*0
chown -R informix:informix /ifxdata
```

На резервном сервере необходимо настроить файлы `onconfig`, `sqlhosts`,
`hosts.equiv` и переменные окружения. За основу можно взять варианты файлов
с основного сервера.

Пример команд по первоначальному копированию файлов:

```bash
scp .profile ifx2:.
scp /opt/informix/etc/onconfig.ifx1 ifx2:/opt/informix/etc/onconfig.ifx2
scp /opt/informix/etc/sqlhosts.ifx1 ifx2:/opt/informix/etc/sqlhosts.ifx2
scp /opt/informix/etc/hosts.equiv.ifx1 ifx2:/opt/informix/etc/hosts.equiv.ifx2
```

Необходимые корректировки переменных окружения:
* `INFORMIXSERVER` - исправить имя сервера Informix;
* `ONCONFIG` - изменить имя файла `onconfig` (если отличается);
* `INFORMIXSQLHOSTS` - изменить имя файла `sqlhosts` (если отличается).

Необходимые корректировки параметров `onconfig`:
* `DBSERVERNAME`, `DBSERVERALIASES` - скорректировать для резервного сервера;
* `HA_ALIAS` - скорректировать для резервного сервера (если значение задано на основном сервере).

Необходимые корректировки файла `sqlhosts` на резервном сервере:
* удалить псевдонимы для локальных протоколов основного сервера (протокол shm, частные IP-адреса);
* добавить аналогичные псевдонимы для локальных протоколов резервного сервера;
* сохранить псевдоним для сетевого подключения к основному серверу;
* опции `s=6` применить к псевдонимам резервного сервера.

Пример наполнения файла `sqlhosts` на резервном сервере:

```
ifx2		onsoctcp	ifx2		on_ifx1		s=6
ifx2_shm	onipcshm	ifx2		on_ifx1_shm	s=6
ifx2_pub	onsoctcp	ifx2-pub	on_ifx1		s=6
ifx1		onsoctcp	ifx1		on_ifx1
```

Последняя строка определяет псевдоним `ifx1` для подключения к основному серверу.


## 4. Первоначальный запуск резервного сервера Informix в режиме RSS

### Шаг 1.

На основном сервере - добавить информацию о резервном сервере RSS, выполнив команду:

```bash
onmode -d add RSS ifx2 password
```

Здесь `ifx2` - имя псевдонима резервного сервера RSS, определённое
в файле `sqlhosts на основном сервере.

Аргумент `password` опциональный, и позволяет избежать неожиданных
подключений "посторонних" серверов RSS с подменой сетевого адреса.

Проверить результат выполнения команды можно, вызвав `onstat -g rss`:

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
ifx2         Defined      Disconnected            0,0            N

$
```

### Шаг 2.

На резервном сервере - выполнить восстановление данных из резервной копии с основного сервера.

Может использоваться уже существующая полная резервная копия,
которую необходимо сделать доступной на резервном сервере.

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
    и восстановление на резервном

Для этого на резервном сервере от имени пользователя `informix` выполняется команда:

```bash
ssh ifx1 ./ontape_STDIO_backup.sh | ontape -p -t STDIO
```

Рекомендуется использование сессии `screen` или `tmux`, чтобы
избежать прерывание выполнения длительной операции из-за закрытия
терминала.

После завершения этого шага необходимо немедленно перейти к шагу 3.
Перезапуск сервера Informix в этот момент приведёт к необходимости
повтора выполнения шага 2.

### Шаг 3.

Включить режим RSS на резервном сервере, выполнив следующую команду:

```bash
onmode -d RSS ifx1 password
```

Здесь `ifx1` - имя псевдонима основного сервера RSS, определённое
в файле `sqlhosts на резервном сервере.

Аргумент `password` опциональный, и должен быть установлен в значение,
использованное на шаге 1.

### Шаг 4.

Проверить состояние RSS на основном и резервном серверах с помощью
команды `onstat -g rss`.

Пример вывода на основном сервере:

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
ifx2         Active       Connected              20,45823        N

$
```

Пример вывода на резервном сервере:

```
$ onstat -g rss

IBM Informix Dynamic Server Version 14.10.FC4W1AEE -- Read-Only (RSS) -- Up 01:03:13 -- 273928 Kbytes

Local server type: RSS
Server Status : Active
Source server name: ifx1
Connection status: Connected
Last log page received(log id,page): 20,45822

$
```
