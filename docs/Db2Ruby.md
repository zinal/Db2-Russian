# Настройка драйвера IBM Db2 для среды программирования Ruby

Ниже описан вариант настройки на базе ODBC/CLI драйвера Db2 и
[адаптера для Ruby](https://github.com/ibmdb/ruby-ibmdb).

## 1. Установить клиента IBM Db2 CLI/ODBC для используемой операционной системы.

Актуального клиента IBM Db2 можно скачать с сайта IBM Fix Central, ссылки для скачивания доступны на
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

Проверить корректность файла `db2dsdriver.cfg` можно путём выполнения команды
`db2cli validate -dsn <dsn_name>`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-validating-db2dsdrivercfg-file).

Проверить возможность подключения к БД можно путём выполнения команды
`db2cli validate -dsn pubwh1 -connect -user mzinal -passwd 'passw0rd'`, подробнее см.
[документацию](https://www.ibm.com/docs/en/db2/11.5?topic=systems-testing-cli-database-connectivity-db2dsdrivercfg-file).

Альтернативный способ проверки подключения - путём запуска сеанса `clpplus`, например:
`clpplus -nw mzinal@pubwh1`.

## 3. Убедится в наличии поддерживаемой версии Ruby 2.5+ или 3.0.

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

## 4. Обеспечить установку переменных окружения для работы драйвера Db2 CLI/ODBC.

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

## 5. Установить адаптер IBM Db2 для Ruby

В сессии, в которой установлена переменная окружения `IBM_DB_HOME`, и обрадающей
достаточными правами для установки gem-модулей, выполнить установку адаптера
IBM Db2 для Ruby командой `gem install ibm_db`.

При отсутствии подключения к интернет, либо для внесения изменений в код адаптера
IBM Db2 для Ruby, можно предварительно скачать проект адаптера и выполнить
его сборку по следующей
[инструкции](https://github.com/ibmdb/ruby-ibmdb/blob/master/IBM_DB_Adapter/ibm_db/README).

Собранный локально драйвер затем должен быть установлен командой
`gem install --local ibm_db-5.2.0.gem`.

Временно (январь 2022 года) существуют проблемы со сборкой адаптера на современных
системах (например, использующих GCC 10.2 и старше). Исправление подготовлено и будет
включено в основной код адаптера. Временно можно использовать исправленную копию кода
в [следующем репозитории](git@github.com:zinal/ruby-ibmdb.git).

## 6. Проверить работспособность адаптера IBM Db2 для Ruby

Проверка работоспособности адаптера IBM Db2 для Ruby производится по инструкции в
[документации](https://www.ibm.com/docs/en/db2/11.5?topic=idrdraarg-verifying-db-ruby-driver-installation-interactive-ruby-shell).
