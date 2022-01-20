# Настройка драйвера IBM Db2 для среды программирования Ruby

Ниже описан вариант настройки на базе ODBC/CLI драйвера Db2 и
[адаптера для Ruby](https://github.com/ibmdb/ruby-ibmdb).

## 1. Установить клиента IBM Db2 CLI/ODBC для используемой операционной системы.

Актуальную версию клиента IBM Db2 можно скачать с сайта IBM Fix Central, ссылки
для скачивания доступны на
[следующей странице](https://www.ibm.com/support/pages/download-fix-packs-version-ibm-data-server-client-packages).
Для скачивания требуется IBM ID, регистрация бесплатна.

Достаточно скачать минимальный пакет "Data Server Driver for ODBC and CLI".

Удобнее (и единственно доступно для MacOS) использовать пакет "Data Server Driver Package",
там в комплекте есть утилиты `db2cli` и `clpplus` для выполнения запросов - полезно для
проверки корректности настроек подключения.

Порядок установки драйвера зависит от операционной системы.
Описание видов клиентов IBM Db2 и процедур установки приведено в
[документации](https://www.ibm.com/docs/en/db2/11.5?topic=installing-data-server-drivers-clients).

## 2. Настроить подключение к Db2 в файле db2dsdriver.cfg.

Файл `db2dsdriver.cfg` размещается в подкаталоге `cfg` каталога инсталляции клиента Db2.
Формат файла описан в
[документации](https://www.ibm.com/docs/en/db2/11.5?topic=drivers-data-server-driver-configuration-file).

Пример настройки:
```xml
<configuration>
   <!-- Multi-line comments are not supported -->
   <dsncollection>
      <dsn alias="pubwh1" name="BLUDB" host="publicocp" port="31715"/>
   </dsncollection>
   <databases>
      <database name="BLUDB" host="publicocp" port="31715">
      </database>
   </databases>
</configuration>
```

При подключении к серверам Db2 for LUW в полях `dsn/alias` и `database.name` указывается
имя базы данных, в то время как при подключении к серверам Db2 for z/OS в этих полях
необходимо указать значение параметра LOCATION (см. вывод команды `-DISPLAY DDF`).

Проверить корректность файла `db2dsdriver.cfg` можно путём выполнения команды
`db2cli validate -dsn <dsn_name>`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-validating-db2dsdrivercfg-file).

Проверить возможность подключения к БД можно путём выполнения команды
`db2cli validate -dsn pubwh1 -connect -user mzinal -passwd 'passw0rd'`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-testing-cli-database-connectivity-db2dsdrivercfg-file).

Альтернативный способ проверки подключения - путём запуска сеанса `clpplus`, например:
`clpplus -nw mzinal@pubwh1`.

## 3. Дополнительные настройки для подключения к Db2 for z/OS и IBM i

### 3.1. Установка ключа Db2 Connect

При настройке подключений к серверам Db2 for z/OS и IBM i требуется наличие файла
ключа лицензии (один из файлов `db2consv_ee.lic`, `db2consv_as.lic`,
`db2consv_zs.lic`, `db2consv_is.lic`) в подкаталоге `license` каталога инсталляции
драйвера IBM Db2. Более подробная информация приведена в
[документации](https://www.ibm.com/docs/en/db2/11.5?topic=applications-license-requirements).

При использовании полного клиента IBM Db2 лицензионный ключ Db2 Connect
устанавливается и проверяется с помощью следующих команд, выполняемых
от имени владельца экземпляра / администратора Db2:
```bash
db2licm -a /path/to/file.lic
db2licm -l
```

### 3.2. Связывание пакетов для работы клиента Db2

Перед началом использования определённой версии клиента Db2 для подключения
к серверам Db2 необходимо выполнить связывание пакетов статического SQL,
соответствующих используемой версии клиента.

В случае отсутствия на сервере Db2 нужной версии пакетов статического
SQL работа клиента Db2 будет прерываться ошибками `SQL0805N`, подробности
указаны в [документации](https://www.ibm.com/docs/en/db2/11.5?topic=errors-sql0805n).

Пакеты статического SQL поставляются в составе клиента в подкаталоге `bnd`.
Связывание необходимо выполнить с помощью полного клиента Db2
(IBM Data Server Client). После однократно выполненной для конкретного
сервера Db2 операции связывания все другие инсталляции всех типов клиентов Db2
(строго той же версии) смогут работать с этим сервером Db2, при этом
повторное связывание обычно не требуется.

Ниже приведён пример набора команд для настройки подключения и связывания
пакетов для работы с сервером Db2 for z/OS:

```bash
db2 catalog tcpip node zos1node remote 172.17.204.196 server 446
db2 catalog database DSNALOC as zos1db at node zos1node
db2 catalog dcs database zos1db
db2 connect to zos1db user ibmuser using 'pass****'
cd sqllib/bnd
db2 bind @ddcsmvs.lst blocking all sqlerror continue grant public
```

## 4. Убедится в наличии поддерживаемой версии Ruby 2.5+ или 3.0.

Актуальная информация о поддерживаемых версиях Ruby доступна на
[странице адаптера IBM Db2 для Ruby](https://github.com/ibmdb/ruby-ibmdb).

Необходимо убедиться в отсутствии путаницы версий бинарников (особенно важно при использовании `rbenv`).
Запускаемые команды (`ruby`, `ridk`, `gem`) должны относиться к одной версии Ruby.

Также следует убедиться наличии заголовочных файлов и библиотек для сборки (должно работать включение
`ruby/thread.h`), при необходимости установить недостающие пакеты и поправить переменные окружения
`CPPFLAGS` / `LDFLAGS`.

Например, для системы Debian 11.2 требуется:
```bash
apt install -y ruby-dev
```

## 5. Обеспечить установку переменных окружения для работы драйвера Db2 CLI/ODBC.

Для Linux/AIX/Solaris/MacOS включить в профиль либо загрузить вручную файл `db2profile`
Для Windows всё обычно делает инсталлятор.

Далее необходимо выставить переменную окружения `IBM_DB_HOME` в каталог установки
драйвера Db2 CLI/ODBC. Эта переменная нужна исключительно на время сборки
адаптера для Ruby, при дальнейшей работе она не требуется.

Например, если драйвер IBM Db2 установлен в каталог `/opt/ibm/dsdriver`:

```
. /opt/ibm/dsdriver/db2profile
which db2cli     # Проверить установленные пути
export IBM_DB_HOME=/opt/ibm/dsdriver
```

## 6. Установить адаптер IBM Db2 для Ruby

В сессии, в которой установлена переменная окружения `IBM_DB_HOME`, и обрадающей
достаточными правами для установки gem-модулей, выполнить установку адаптера
IBM Db2 для Ruby командой `gem install ibm_db`.

При отсутствии подключения к интернет, либо для внесения изменений в код адаптера
IBM Db2 для Ruby, можно предварительно скачать проект адаптера и выполнить
его сборку по следующей
[инструкции](https://github.com/ibmdb/ruby-ibmdb/blob/master/IBM_DB_Adapter/ibm_db/README).

Собранный локально драйвер затем должен быть установлен следующей командой:
```bash
gem install --local ibm_db-5.2.0.gem
```

Временно (январь 2022 года) существуют проблемы со сборкой адаптера на современных
системах (например, использующих GCC 10.2 и старше). Исправление подготовлено и будет
включено в основной код адаптера. Временно можно использовать исправленную копию кода
в [следующем репозитории](https://github.com/zinal/ruby-ibmdb).

## 7. Проверить работоспособность адаптера IBM Db2 для Ruby

Проверка работоспособности адаптера IBM Db2 для Ruby производится по инструкции в
[документации](https://www.ibm.com/docs/en/db2/11.5?topic=idrdraarg-verifying-db-ruby-driver-installation-interactive-ruby-shell).

Пример команд по проверке работоспособности и их вывод:
```bash
$ irb
irb(main):001:0> require 'ibm_db'
=> true
irb(main):002:0> conn = IBM_DB.connect 'pubwh1','mzinal','passw0rd$'
=> #<IBM_DB::Connection:0x0000562f87933660>
irb(main):003:0> stmt = IBM_DB.exec conn,'select tabschema, CAST(count(*) AS INTEGER) as cnt from syscat.tables group by tabschema'
=> #<IBM_DB::Statement:0x0000562f874c5240>
irb(main):004:0> IBM_DB.fetch_assoc stmt 
=> {"TABSCHEMA"=>"BANKING ", "CNT"=>8}
irb(main):005:0> 
```

## 8. Пример вызова хранимой процедуры, возвращающей набор результатов

Адаптер IBM Db2 для Ruby поддерживает функцию вызова хранимых процедур с использованием
оператора `CALL`. Если хранимая процедура возвращает наборы результатов, адаптер
позволяет получить данные первого из возвращаемых наборов результатов.
Для обеспечения доступа ко второму и последующим наборам результатов необходима
(по состоянию на январь 2022 года) доработка адаптера IBM Db2 для Ruby.

Пример скрипта создания хранимой процедуры, возвращающей набор результатов:

```SQL
CREATE TABLE ibmuser.mvzdemo1(
  a INTEGER NOT NULL PRIMARY KEY,
  b VARCHAR(100),
  c BIGINT
) @

INSERT INTO ibmuser.mvzdemo1 VALUES(1, 'One',   132870803422839050) @
INSERT INTO ibmuser.mvzdemo1 VALUES(2, 'Two',   265741606845678100) @
INSERT INTO ibmuser.mvzdemo1 VALUES(3, 'Three', 398612410268517150) @

CREATE OR REPLACE PROCEDURE ibmuser.mvzproc1()
  DYNAMIC RESULT SETS 1
BEGIN
  DECLARE c1 CURSOR WITH RETURN
    FOR SELECT * FROM ibmuser.mvzdemo1;
  OPEN c1;
END @
```

Пример вызова хранимой процедуры и выдачи её результата в среде Ruby:

```bash
$ irb
irb(main):001:0> require 'ibm_db'
=> true
irb(main):002:0> conn = IBM_DB.connect 'pubwh1','mzinal','passw0rd$'
=> #<IBM_DB::Connection:0x00005637f4ae5880>
irb(main):003:0> stmt = IBM_DB.exec conn, 'call ibmuser.mvzproc1()'
=> #<IBM_DB::Statement:0x00005637f4706fe8>
irb(main):004:0> IBM_DB.fetch_assoc stmt
=> {"A"=>1, "B"=>"One", "C"=>"132870803422839050"}
irb(main):005:0> IBM_DB.fetch_assoc stmt
=> {"A"=>2, "B"=>"Two", "C"=>"265741606845678100"}
irb(main):006:0> IBM_DB.fetch_assoc stmt
=> {"A"=>3, "B"=>"Three", "C"=>"398612410268517150"}
irb(main):007:0> IBM_DB.fetch_assoc stmt
=> false
irb(main):008:0> 
```
