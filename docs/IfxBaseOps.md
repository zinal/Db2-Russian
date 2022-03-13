# Базовые операции по работе с Informix

## 1. Создание баз данных

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-create-database-statement).

```bash
echo "CREATE DATABASE mydb1 IN work1 WITH BUFFERED LOG" | dbaccess sysmaster
```

## 2. Создание учётных записей

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
