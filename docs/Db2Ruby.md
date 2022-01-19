# Настройка драйвера IBM Db2 для среды программирования Ruby

Ниже описан вариант настройки на базе ODBC/CLI драйвера Db2 и
[адаптера для Ruby](https://github.com/ibmdb/ruby-ibmdb).

## 1. Установить клиента IBM Db2 CLI/ODBC для используемой операционной системы.

Актуального клиента IBM Db2 можно скачать с сайта IBM Fix Central, ссылки для скачивания доступны на
[следующей странице](https://www.ibm.com/support/pages/download-fix-packs-version-ibm-data-server-client-packages).

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
```
<configuration>
   <!-- Multi-line comments are not supported -->
   <dsncollection>
      <dsn alias="pubwh1" name="BLUDB" host="publicocp" port="31715"/>
   </dsncollection>
</configuration>
```

Проверить корректность файла `db2dsdriver.cfg` можно путём выполнения команды
`db2cli validate -dsn <dsn_name>`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-validating-db2dsdrivercfg-file).

Проверить возможность подключения к БД можно путём выполнения команды
`db2cli validate -dsn <dsn_name> -connect -user <username> -passwd <password>`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-testing-cli-database-connectivity-db2dsdrivercfg-file).

Альтернативный способ проверки подключения - путём запуска сеанса `clpplus`, например:
`clpplus -nw username@alias`.
