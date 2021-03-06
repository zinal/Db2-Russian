# Структура сервера баз данных Db2

Под сервером баз данных Db2 понимается компьютер, на котором установлено программное
обеспечение сервера Db2 (DB2 engine) и который обеспечивает предоставление сервисов
по управлению структурированной информацией.

Доступ к сервисам Db2 со стороны приложений обеспечивается клиентским
программным обеспечением  Db2 (IBM Data Server Driver Package),
обеспечивающее взаимодействие с сервером Db2 в соответствии с поддерживаемыми методами
подключения приложений (включая ODBC, JDBC, OLE DB, ADO, CLI и другие методы).
В большинстве случаев вместе с сервером DB2 устанавливается и необходимое
клиентское программное обеспечение, что обеспечивает возможность подключения
к серверу Db2 приложений, непосредственно размещаемых на сервере баз данных.

На сервере баз данных Db2 может размещаться несколько копий программного
обеспечения Db2, отличающихся друг от друга версиями программного обеспечения
и каталогами установки. Несколько копий программного Db2 на одном
сервере функционируют независимо друг от друга при условии отсутствия конфликтов
ресурсов между ними (включая достаточность вычислительных ресурсов сервера и
отсутствие конфликтов за логические ресурсы операционной системы: сетевые имена,
номера портов, каталоги файловой системы и т.п.).

Непосредственное предоставление сервисов СУБД обеспечивается
компонентом менеджера баз данных Db2 (DB2 database manager,
DB2 DBM). В каждой копии может быть создано несколько экземпляров
менеджера баз данных Db2, или, более кратко – экземпляров Db2 (DB2 instances).
Экземпляр – это независимая среда, в которой могут создаваться базы
данных и работают приложения. Каждый экземпляр DB2 обладает
собственной конфигурацией и предоставляет доступ к определенному набору баз
данных. Экземпляры DB2 являются независимыми в том
смысле, что выполнение операций на одном экземпляре не
влияет на другие – за исключением ресурсных ограничений, вызываемых
функционированием нескольких экземпляров на одном и том же 
физическом либо виртуальном сервере.

Запуск и остановка сервисов Db2 выполняется
на уровне экземпляра, т.е. каждый экземпляр
Db2 может находиться в запущенном либо остановленном
состоянии. Параметры экземпляра Db2 могут определять его
ресурсные ограничения (например, в части использования оперативной
памяти или привязки к определённым процессорным ядрам).
Ресурсы экземпляра Db2 используются для обслуживания
существующих в рамках экземпляра баз данных.

База данных – это совокупность объектов, составляющих
единый информационный массив (таблиц, представлений, индексов и т.д.).
Базы данных являются независимыми единицами и, соответственно,
обычно не используют объекты совместно с другими базами данных
(исключением могут быть распределенные конфигурации баз данных,
использующие механизмы федерализации доступа к данным).

Схематически пример структуры сервера баз данных представлен
на рисунке.


![Пример структуры сервера баз данных IBM Db2](https://raw.githubusercontent.com/zinal/Db2-Russian/master/db2-overview/part01/images/db-server-structure.png)

Во многих случаях сервер баз данных Db2 содержит
только одну установленную копию Db2 с единственным
созданным экземпляром, обслуживающим единственную базу
данных. При такой конфигурации все ресурсы сервера баз данных
используются для обеспечения функционирования одной-единственной базы
данных Db2.

Обслуживание запросов подключенных приложений на стороне
сервера баз данных выполняется так называемыми агентами Db2. Для
каждого подключенного приложения в рамках экземпляра Db2
запускается координирующий (основной) агент, который
при необходимости может запускать несколько дополнительных (вспомогательных)
агентов. Технически каждый агент представляет собой отдельный поток выполнения
либо (для старых версий Db2) отдельный процесс
операционной системы, с ассоциированными ресурсами, необходимыми
для его работы.
