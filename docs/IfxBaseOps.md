# Базовые операции по работе с Informix

## 1. Создание баз данных

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-create-database-statement).

```bash
echo "CREATE DATABASE mydb1 IN work1 WITH BUFFERED LOG" | dbaccess sysmaster
```

## 2. Создание учётных записей

[Документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=security-internal-users-unix-linux).

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
```

```bash
onmode -wf USERMAPPING=BASIC
echo "CREATE DEFAULT USER WITH PROPERTIES USER 'guest'" | dbaccess sysmaster
echo 'create user user1 with password "P@ssw0rd"' | dbaccess sysmaster
```
