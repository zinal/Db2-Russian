# Обновление версии Informix и возврат на предыдущую версию

## 1. Обновление на новую версию Informix

Закрыть доступ пользователей

Архивировать последний журнал лога (`onmode -l`, проверить через `onstat -l`).

Отключить архивацию лога (теряет смысл на время апгрейда) - например, выставить `ALARMPROGRAM` в `$INFORMIXDIR/etc/no_log.sh`.

Корректно завершить транзакции:
```bash
oninit
onmode -sy
onmode -l
onmode -c
onmode -ky
oninit -s
onmode -ky
```

Установить новую версию Informix, скопировать конфигурационные файлы (onconfig, sqlhosts, ...).

Создать каталог для отмены апгрейда:
```bash
mkdir /path/to/restore_point
chmod 700 /path/to/restore_point
```

Выставить параметры отмены апгрейда в onconfig:
```
CONVERSION_GUARD 1
RESTORE_POINT_DIR /path/to/restore_point
```

Запустить базу на новой версии Informix.

Возможно - пересоздать `sysmaster` и `sysadmin` (см. подраздел ниже).

Пересобрать статистику: `UPDATE STATISTICS` на базах данных `sys*`, штатный/регламентных пересбор на пользовательских базах.

Возобновить архивацию лога.

Сделать полную резервную копию.

## 2. Возврат на предыдущую версию Informix

Штатно откат возможен только на ту версию Informix, с которой было выполнено обновление.
Есть ряд ограничений (отсутствие изменений кода SPL-процедур, отсутствие не полностью применённого DDL, и ряд других).

Начало - аналогично апгрейду, до шага "корректно завершить транзакции" включительно.

Запустить базу на текущей (более новой) версии Informix.

Определить логическую версию для отката, выполнив команду `onmode -b`:

```
# onmode -b
You may only revert IBM Informix Dynamic Server to
IBM Informix-OnLine Version 11.70.xC1 or later:
    Use 'onmode -b 11.70.xC1' to revert to versions 11.70.xC1 or 11.70.xC2
    Use 'onmode -b 11.70.xC3' to revert to versions 11.70.xC3 or later
    Use 'onmode -b 12.10' to revert to versions 12.10.xC1, 12.10.xC2, or
          12.10.xC3.
    Use 'onmode -b 12.10.xC4' to revert to versions 12.10.xC4 through
          12.10.xC7.
    Use 'onmode -b 12.10.xC8' to revert to versions 12.10.xC8+
    Use 'onmode -b 14.10.xC1' to revert to version 14.10.xC1
    Use 'onmode -b 14.10.xC2' to revert to version 14.10.xC2 through 14.10.xC5.
For more information refer to Section II of IBM Informix Migration guide.
```

Выполнить откат с помощью правильной выбранной команды, например `onmode -b 11.70.xC3`.
В результате произойдёт корректировка системных баз данных для запуска на соответствующей старой версии Informix.

Установить старую версию Informix (ту, которая использовалась перед отменяемым апгрейдом).
Скопировать конфигурационные файлы (onconfig, sqlhosts, ...).

Запустить базу с использованием старой версии Informix:
```bash
oninit -s
onmode -m
```

Возможно - пересоздать `sysmaster` и `sysadmin` (см. подраздел ниже).

Пересобрать статистику: `UPDATE STATISTICS` на базах данных `sys*`, штатный/регламентных пересбор на пользовательских базах.

Возобновить архивацию лога.

Сделать полную резервную копию.

## 3. Пересоздание sysmaster и sysadmin

Исходные материалы:
* https://www.ibm.com/support/pages/rebuilding-sysadmin-database-manually
* https://www.ibm.com/support/pages/rebuilding-sysmaster-database

### 3.1 Пересоздание `sysadmin`

```bash
cd $INFORMIXDIR/etc/sysadmin;
dbaccess - db_uninstall.sql;
dbaccess - db_create.sql;
dbaccess sysadmin db_install.sql;
dbaccess sysadmin sch_tasks.sql;
dbaccess sysadmin sch_aus.sql;
dbaccess sysadmin sch_sqlcap.sql;
dbaccess sysadmin start.sql;
```

Note: db_uninstall.sql will correctly return errors 329/111 and 349 
if the sysadmin database does not exist. Ignore these errors and continue.

### 3.2 Пересоздание sysmaster

Steps to rebuild sysmaster database:
1. Make sure no one is connected to the database.
2. Bring the instance in single user mode: `oninit -j`
3. Shutdown scheduler:
```bash
dbaccess sysadmin - <<EOF
EXECUTE FUNCTION task("scheduler shutdown");
EOF
```
4. Run the script buildsmi to rebuild the sysmaster database: `$INFORMIXDIR/etc/buildsmi`
5. Once sysmaster database is built, you can restart the scheduler. To do this:
```bash
dbaccess sysadmin - <<EOF
EXECUTE FUNCTION task("scheduler start");
EOF
```

If the sysmaster rebuild fails, you will see a file created in directory `/tmp` called `buildsmi.out`.
This file will not exist if the sysmaster database creation is successful.

To ensure further, check the sysmaster build date by running following SQL:

```bash
dbaccess sysmaster - <<EOF
select * from sysdatabases where name = "sysmaster";
EOF
```
