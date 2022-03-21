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
onmode -wf USERMAPPING=BASIC
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

Программа `dbaccess` также позволяет выполнять SQL-скрипты, хранящиеся в файлах, см.
[пример](https://www.ibm.com/docs/en/informix-servers/14.10?topic=access-example-run-command-file).


## 5. Загрузка драйвера JDBC и подключение JDBC-клиентом

JDBC драйвер поставляется в виде одного архива в формате JAR. Скачать драйвер можно с сайта
[Maven Repository](https://mvnrepository.com/artifact/com.ibm.informix/jdbc),
а также с сайтов IBM Passport Advantage и IBM Fix Central.

Подключение JDBC-клиента рассмотрим на примере приложения
[Squirrel SQL](http://squirrel-sql.sourceforge.net/),
в котором есть поддержка работы с базами данных Informix.

После запуска Squirrel SQL необходимо зарегистрировать архив файла драйвера
Informix в списке поддерживаемых драйверов. Для этого можно указать путь к файлу драйвера
в уже существующем пункте "Informix", либо создать новый пункт и ввести
необходимые параметры. Имя класса драйвера - `com.informix.jdbc.IfxDriver`.

Формат JDBC URL для драйвера Informix описан в 
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=database-drivermanagergetconnection-method),
типичные значения выглядят следующим образом:
```
jdbc:informix-sqli://ifx1.local:35000/mydb1:INFORMIXSERVER=ifx1
```
В примере выше описано подключение к серверу Informix с именем `ifx1`, доступном через порт `35000`
и сетевое имя `ifx1.local` (вместо сетевого имени также может быть указан IP-адрес).

После настройки драйвера в среде Squirrel SQL необходимо создать новое подключение,
указав тип драйвера (Informix), корректный JDBC URL, логин и пароль. При корректной
настройке становится возможным выполнение SQL-команд в интерактивном редакторе
Squirrel SQL.


## 6. Регистрация стандартных модулей DataBlade

Модули DataBlade расширяют возможности Informix, добавляя новые функции и типы данных.
Описание стандартных модули DataBlade, поставляемых с Informix, приведено в
[документации](https://www.ibm.com/docs/en/informix-servers/14.10?topic=modules-database-extensions-users-guide).

Перед использованием стандартных модулей DataBlade их необходимо зарегистрировать
в той базе данных, в которой они будут использоваться. Пример действий по регистрации
одного из стандартных модулей DataBlade приведён на
[этой странице](https://www.ibm.com/support/pages/how-register-and-use-dbmsalert-sql-package-extension).

Пример последовательности команд по регистрации части стандартных модулей DataBlade
в базе данных `mydb1` на сервере `ifx1`:

```bash
$ whoami
informix
$ cd $INFORMIXDIR/extend
$ blademgr
ifx1> show databases
Databases on server:
	mydb1
	sysadmin
	sysuser

ifx1> list mydb1
There are no modules registered in database mydb1.
ifx1> show modules
16 DataBlade modules installed on server ifx1:
	      ifxrltree.3.00  	       binaryudt.1.0  
	            bts.3.11  	    spatial.8.22.FC4  
	        sts.2.00.FC2  	       ifxregex.1.10  
	 TimeSeries.6.01.FC2  	        excompat.1.0  
	     ifxbuiltins.1.1  	        LLD.1.20.FC2  
	         mqblade.2.0  	            Node.2.0 c
	        wfs.1.00.FC1  	   TSPMatch.2.00.FC1  
	   TSPIndex.1.00.FC1  	   TSAFuncs.1.00.FC2  
A 'c' indicates DataBlade module has client files.
If a module does not show up, check the prepare log.
ifx1> register excompat.1.0 mydb1
Register module excompat.1.0 into database mydb1? [Y/n]Y
Registering DataBlade module... (may take a while).
DataBlade excompat.1.0 was successfully registered in database mydb1.
ifx1> register ifxregex.1.10 mydb1
Register module ifxregex.1.10 into database mydb1? [Y/n]Y
Registering DataBlade module... (may take a while).
DataBlade ifxregex.1.10 was successfully registered in database mydb1.
ifx1> register TimeSeries.6.01.FC2 mydb1
Register module TimeSeries.6.01.FC2 into database mydb1? [Y/n]Y
Registering DataBlade module... (may take a while).
DataBlade TimeSeries.6.01.FC2 was successfully registered in database mydb1.
ifx1> list mydb1
DataBlade modules registered in database mydb1:
	        excompat.1.0  	       ifxregex.1.10  
	 TimeSeries.6.01.FC2  

ifx1> quit
Disconnecting...
$
```


## 7. Получение информации о плане выполнения запросов

Два основных варианта:
* оператор `SET EXPLAIN` - [документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-set-explain-statement)
* директивы в запросе - [документация](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statements-explain-directives)

После активации режима `EXPLAIN` сервер Informix записывает информацию о плане
выполнения запроса в текстовый файл. Имя и путь к файлу могут быть настроены
с помощью команды `SET EXPLAIN FILE TO 'ПутьИИмяФайла'`, по умолчанию обычно
информация пишется в файл с именем `	sqexplain.out` в домашнем каталоге
пользователя, см. также [документацию](https://www.ibm.com/docs/en/informix-servers/14.10?topic=statement-default-name-location-explain-output-file-unix).

Пример скрипта, выполняющего запрос и сохраняющего план его выполнения
в файл с указанным именем:

```SQL
SET EXPLAIN FILE TO '/tmp/q1_explain.txt';
SET EXPLAIN ON;

(SELECT 'ifxdemo1' AS tabname, COUNT(*) AS rowcount FROM ifxdemo1) UNION ALL
(SELECT 'ifxdemo2' AS tabname, COUNT(*) AS rowcount FROM ifxdemo2) UNION ALL
(SELECT 'ifxdemo3' AS tabname, COUNT(*) AS rowcount FROM ifxdemo3) UNION ALL
(SELECT 'ifxdemo4' AS tabname, COUNT(*) AS rowcount FROM ifxdemo4);

SET EXPLAIN OFF;
```

В сформированном в результате выполнения скрипта файле на сервере
Informix будет записана информация о плане выполнения, а также
статистика выполнения запроса, включая количество строк, обработанное
на каждом этапе выполнения запроса.

Отключить выполнение запросов, для которых строится план выполнения,
можно с помощью опции `AVOID_EXECUTE`, добавляемой в оператор
`SET EXPLAIN` или к директиве `EXPLAIN` в тексте запроса.
Например:

```SQL
SET EXPLAIN FILE TO '/tmp/q2_explain.txt';
SET EXPLAIN ON AVOID_EXECUTE;

(SELECT 'ifxdemo1' AS tabname, AVG(f) AS rowavg FROM ifxdemo1 WHERE f>-1000) UNION ALL
(SELECT 'ifxdemo2' AS tabname, AVG(f) AS rowavg FROM ifxdemo2 WHERE f>-1000) UNION ALL
(SELECT 'ifxdemo3' AS tabname, AVG(f) AS rowavg FROM ifxdemo3 WHERE f>-1000) UNION ALL
(SELECT 'ifxdemo4' AS tabname, AVG(f) AS rowavg FROM ifxdemo4 WHERE f>-1000);

SET EXPLAIN OFF;
```

Фактическое выполнение оператора при использовании опции `AVOID_EXECUTE`
не производится, поэтому блок информации со статистикой выполнения
не формируется.

## 8. Подключение через DRDA

1. Добавить сервис в `sqlhosts`:

```
ifx1_drda   drsoctcp   ifx1       drda_ifx
```

Номер порта либо задавать числом, либо регистрировать в `/etc/services`:

```
drda_ifx        36000/tcp
```

2. Добавить псевдоним сервиса в `DBSERVERALIASES`.

3. Перезапустить сервер Informix, проверить прослушивание порта через `ss -ln --tcp`.

3. На клиенте Db2 выполнить команды для каталогизации сервера и базы данных, плюс проверки подключения:

```bash
db2 catalog tcpip node ifx1drda  remote ifx1 server drda_ifx
db2 catalog database mydb1 as ifxdb1 at node ifx1drda
db2 connect to ifxdb1 user myuser1 using passw0rd
```
