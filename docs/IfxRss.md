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

Для каждого sbspace проверить:

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

## 3. Подготовка резервного сервера

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=servers-starting-rs-secondary-server-first-time).

1. Установить Informix.
2. Установить дополнительные модули UDR, UDT, DataBlade (если используются).
3. Выделить файловый системы и устройства с именами, соответствующими первичной системе.

Примеры команд для создания структуры каталогов, аналогичной основному серверу:

```bash
mkdir /ifxdata
mkdir /ifxdata/ifx1
mkdir /ifxdata/ifx1/tmp
chown -R informix:informix /ifxdata
chmod -R 700 /ifxdata
```

## 4. Первоначальный запуск резервного сервера Informix в режиме RSS

1. На основном сервере - добавить псевдоним для резервного сервера Informix в файл `sqlhosts`.
Например, вот так может выглядеть содержимое файла `/opt/informix/etc/sqlhosts.ifx1` на сервере `ifx1`:

```
ifx1	onsoctcp	ifx1	on_ifx1
ifx1_shm	onipcshm	ifx1	on_ifx1_shm
ifx1_pub	onsoctcp	ifx1-pub	on_ifx1
ifx2	onsoctcp	ifx2	on_ifx1
```

Была добавлена последняя строка, которая определяет параметры соединения для сервера `ifx2`
с использованием протокола TCP/IP.
Имя сервиса (последний параметр) такое же, как и для сервера `ifx1`,
так как номер порта в нашем примере используется один и тот же.

2. На основном сервере - добавить информацию о резервном сервере RSS, выполнив команду:

```bash
onmode -d add RSS ifx2 password
```

Аргумент `password` опциональный, и позволяет избежать неожиданных подключений "посторонних"
серверов RSS с подменой сетевого адреса.

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

3. На резервном сервере - настроить файлы `onconfig`, `sqlhosts` и переменные
окружения. За основу можно взять варианты файлов с основного сервера.

Пример команд по первоначальному копированию файлов:

```bash
scp .profile ifx2:.
scp /opt/informix/etc/onconfig.ifx1 ifx2:/opt/informix/etc/onconfig.ifx2
scp /opt/informix/etc/sqlhosts.ifx1 ifx2:/opt/informix/etc/sqlhosts.ifx2
```

Необходимые корректировки переменных окружения:
* `INFORMIXSERVER` - исправить имя сервера Informix;
* `ONCONFIG` - изменить имя файла `onconfig` (если отличается);
* `INFORMIXSQLHOSTS` - изменить имя файла `sqlhosts` (если отличается).

Необходимые корректировки параметров `onconfig`:
* `DBSERVERNAME`, `DBSERVERALIASES` - скорректировать для резервного сервера;
* `HA_ALIAS` - скорректировать для резервного сервера (если значение задано на основном сервере).

Необходимые корректировки параметров `sqlhosts`:
* удалить псевдонимы для локальных протоколов основного сервера (протокол shm, частные IP-адреса);
* добавить аналогичные псевдонимы для локальных протоколов резервного сервера.

4. На резервном сервере - выполнить восстановление данных

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

