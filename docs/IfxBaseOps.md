# Базовые операции по работе с Informix

## 1. Создание баз данных

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-create-database-statement)
на оператор `CREATE DATABASE`.

При создании БД необходимо указать основное пространство для её размещения
(будет пространством по умолчанию для создаваемых объектов), а также установить
режим логирования БД.

```bash
echo "CREATE DATABASE mydb1 IN work1 WITH BUFFERED LOG" | dbaccess sysmaster
```

## 2. Создание учётных записей с аутентификацией операционной системы

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=security-internal-users-unix-linux)
по методам аутентификации.

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statement-database-level-privileges)
по полномочиям на уровне базы данных.

Порядок создания учётных записей с аутентификацией через операционную систему:
1. Создать пользователя операционной системы
2. Установить пароль созданного пользователя
3. Создать пользователя Informix командой `CREATE USER`
4. Установить права созданному пользователю Informix (напрямую либо через роль)

```bash
# Команды от имени пользователя root
adduser ifxguest
passwd ifxguest
# Далее работаем от имени пользователя informix
su - informix
echo "CREATE USER ifxguest" | dbaccess sysmaster
echo "GRANT RESOURCE TO ifxguest" | dbaccess mydb1
```

Для проверки доступа пользователя к нужной базе данных можно использовать процедуру,
описанную далее в разделе 4.

## 3. Создание учётных записей с аутентификацией Informix

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=linux-creating-database-server-users-unix)
по порядку создания пользователей с аутентификацией Informix.

[Документация](https://www.ibm.com/docs/en/informix-servers/12.10?topic=linux-specifying-surrogates-mapped-users-unix)
по настройке файла `/etc/informix/allowed.surrogates`.

Перед созданием учётных записей с аутентификацией Informix должен быть создан
пользователь "по умолчанию", который будет применяться как шаблон при создании
"внутренних" пользователей Informix. Необходимо:
1. Выбрать учётную запись с аутентификацией операционной системы, которая будет служить шаблоном
2. Включить выбранную учётную запись в файл `/etc/informix/allowed.surrogates`, при необходимости создав этот файл
3. Создать пользователя по умолчанию.

Пример команд по настройке пользователя "по умолчанию":

```bash
# Выполняется от имени пользователя root
mkdir /etc/informix
echo 'USERS:ifxguest' >/etc/informix/allowed.surrogates
echo 'GROUPS:' >>/etc/informix/allowed.surrogates
# Дальнейшие действия выполняются от имени пользователя informix
su - informix
# Обновляем кэш записей пользователей-суррогатов
onmode -cache surrogates
# Создаём пользователя "по умолчанию"
echo "CREATE DEFAULT USER WITH PROPERTIES USER 'ifxguest'" | dbaccess sysuser
```

При наличии пользователя "по умолчанию" создание учётных записей с аутентификацией Informix
производится путём выполнения команды `CREATE USER логин WITH PASSWORD пароль`, например:

```bash
echo "CREATE USER myuser1 WITH PASSWORD 'passw0rd'" | dbaccess sysuser
echo "GRANT RESOURCE TO myuser1" | dbaccess mydb1
```

Для проверки доступа пользователя к нужной базе данных можно использовать процедуру,
описанную далее в разделе 4.

## 4. Выполнение запросов в программе dbaccess

Подключение от имени вновь созданной учётной записи через программу `dbaccess`:
1. Запустить программу `dbaccess` без указания параметров
2. В меню выбрать пункт `Connection`, затем `Connect`, выбрать нужный сервер.
3. Ввести логин пользователя (например, `ifxguest`).
4. Ввести пароль пользователя
5. Выбрать нужную базу данных, для подключения к которой у пользователя есть права (например, `mydb1`).
6. В текущем меню выбрать пункт `Exit` для возврата к верхнему уровню.
7. В меню выбрать `Query-language`, затем `New`.
8. Ввести текст SQL-запроса без завершающего символа `;` (например, `SELECT * FROM "informix".systables`)
9. Нажать клавишу `ESC` для выхода в меню (возможно, потребуется два нажатия)
10. В меню выбрать пункт `Run` для запуска запроса и просмотра его результатов
11. Для выхода из режима отображения результатов в меню выбрать `Exit`.
12. Для выхода из программы `dbaccess` в меню верхнего уровня выбрать `Exit`.

```bash
onmode -wf USERMAPPING=BASIC
echo "CREATE DEFAULT USER WITH PROPERTIES USER 'guest'" | dbaccess sysmaster
echo 'create user user1 with password "P@ssw0rd"' | dbaccess sysmaster
```
