# Подключение к SSL-защищённой БД Db2 из штатного клиента командной строки

Исходные данные:
- хост publicocp (пробит в /etc/hosts);
- порт 31206;
- имя базы данных BLUDB;
- логин/пароль известны

## 0. На машине должны быть установлены клиент Db2 и свежий GSKit.

[Клиент Db2](https://www.ibm.com/support/pages/recommended-fix-packs-ibm-data-server-client-packages)

[Дистрибутив GSKit](https://www.ibm.com/support/pages/node/224963)

Установка GSKit на Debian и Ubuntu вполне успешно производится после конвертации пакета RPM через утилиту alien.

## 1. Получаем с сервера сертификат

```bash
openssl s_client -showcerts -connect publicocp:31206
```

Выведенный в консоль сертификат сохраняем в файл.

## 2. Проверяем наличие базы данных ключей

```bash
db2 get dbm cfg | grep SSL_CLNT_KEYDB
```

## 3. При отсутствии базы данных ключей создаём её и настраиваем её использование

```bash
gsk8capicmd_64 -keydb -create -db "/home/db2inst1/certz/client_ssl.kdb" -pw passw0rd -stash

db2 -t
update dbm cfg using 
  SSL_CLNT_KEYDB "/home/db2inst1/certz/client_ssl.kdb"
  SSL_CLNT_STASH "/home/db2inst1/certz/client_ssl.sth";
```

## 4. Добавляем полученный сертификат в базу данных ключей

```bash
gsk8capicmd_64 -cert -add -db "/home/db2inst1/certz/client_ssl.kdb" -pw passw0rd \
  -file "/home/db2inst1/certz/db2wh1.pem" -format ascii
```

Затем можно проверить содержимое базы данных:

```bash
gsk8capicmd_64 -cert -list -db "/Db2Wh/certz/client_ssl.kdb" -pw passw0rd
```

## 5. Добавляем в системный каталог клиента Db2 узел и базу данных

```bash
db2 catalog tcpip node pubwh1 remote publicocp server 31206 security ssl
db2 catalog db bludb as pubwh1 at node pubwh1
```

## 6. Выполняем пробное подключение

```bash
db2 connect to pubwh1 user zinal using passw0rd
```

## 7. Выполняем пробный запрос

```bash
db2 "select count(*) from syscat.tables"
```
