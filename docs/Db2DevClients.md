# Разработка клиентских приложений IBM Db2

## Доступные языки и стандарты

DB2 предоставляет среду разработки приложений, основанную на
стандартах и прозрачную для всего семейства DB2. Стандартизация SQL на
всей линейке продуктов DB2 обеспечивает общий набор прикладных
программных интерфейсов для доступа к базе данных.

Кроме того, каждый продукт DB2 предоставляет средства прекомпиляции
SQL (т.е. поддержку встраивания SQL-операторов в программы на
различных языках программирования), а также прикладные программные
интерфейсы (API), с помощью которых разработчики могут встраивать
статические и динамические SQL-запросы в разрабатываемые приложения.

Доступные в DB2 языки и стандарты включают в себя:
* SQL;
* XQuery и XPath;
* C/C++ (CLI, ODBC и встраиваемый SQL);
* Java (JDBC и SQLJ);
* COBOL (см.
  [примеры программ](https://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.apdv.samptop.doc/doc/r0008190.html),
  [инструкции по сборке программ](https://www.ibm.com/support/knowledgecenter/en/SSEPGG_11.1.0/com.ibm.db2.luw.apdv.embed.doc/doc/c0021475.html));
* PHP;
* R;
* Perl;
* Python;
* Ruby, Ruby on Rails;
* Языки .NET;
* OLE DB, ADO;
* приложения Microsoft Office®: Excel, Access, Word;
* Веб-службы.

DB2 предлагает разработчикам баз данных гибкость, необходимую для
использования преимуществ функций разработки на стороне сервера, таких
как хранимые процедуры и пользовательские функции, при этом не
ограничивая разработчиков в выборе языка программирования клиентских
приложений.

При разработке на стороне клиента разработчики создают приложения на
компьютере-клиенте, а затем устанавливают соединение и получают доступ
к базе данных DB2 с помощью API, предоставленных DB2.  Разработка на
стороне клиента часто дополняется разработкой на стороне сервера,
когда программный код оформляется в виде специальных объектов,
размещаемых в базе данных и вызываемых из программного обеспечения на
стороне клиента.

Организация разработки на стороне сервера описана в разделе
[«Разработка на стороне сервера Db2»](Db2DevRoutines).

## Разработка на встраиваемом SQL

Приложения на встраиваемом SQL — это приложения, в которых язык SQL
встроен в язык хоста, например, C, C++ или COBOL. Приложение на
встраиваемом SQL может содержать как статический, так и динамический
SQL (см. далее). Встраивание операторов SQL в приложение
осуществляется с помощью специальных программных конструкций,
расширяющих применяемый язык программирования (обычно это конструкция
EXEC SQL, адаптируемая под особенности конкретного языка
программирования).

Приложения на встраиваемом SQL обрабатываются специальной
программой-прекомпилятором, в результате чего формируются два
промежуточных файла: файл связей, содержащий текст встроенных
(статических) SQL-операторов и сопутствующую служебную информацию, и
файл с программой на оригинальном языке программирования (без
встроенных SQL-операторов), которая содержит исходную логику
приложения и специальные вызовы клиента DB2 для выполнения операторов
SQL. Вызов программы-прекомпилятора осуществляется с помощью команды
DB2 `PRECOMPILE` (сокращенное наименование - `PREP`).

Программа на оригинальном языке программирования далее должна быть
скомпилирована штатными средствами (например, обработана компилятором
и редактором связей C/C++), а файл связей должен быть загружен в базу
данных DB2 (командой `BIND`) перед выполнением программы.

Статические SQL-операторы — это операторы, структура SQL которых
полностью известна во время прекомпиляции. Таким образом, работа со
статическим SQL возможна только для приложений на встраиваемом SQL.

Статический SQL лучше всего использовать в базах данных, статистика
распределения записей в которых не сильно изменяется.  Преимуществом
использования статического SQL является экономия системных ресурсов на
выполнение оптимизации запросов и гарантированная стабильность
используемых планов выполнения запросов.

Современные программные API, такие как JDBC и ODBC, всегда используют
динамический SQL, независимо от того, включает SQL-оператор только
заранее известные объекты или нет.  В целом, для обработки
SQL-оператора как динамического используется две команды:
* `PREPARE`: готовит, или компилирует SQL-оператор, рассчитывая
   план доступа, который будет применяться для извлечения данных;
* `EXECUTE`: выполняет SQL-оператор.

Команды `PREPARE` и `EXECUTE` также можно выполнить разом, с помощью
одного оператора: `EXECUTE IMMEDIATE`.


## Разработка с использованием ODBC, CLI, OLE DB или ADO

Интерфейс уровня вызовов (Call Level Interface, CLI) был разработан
компанией компаниями X/Open и SQL Access Group. Этот интерфейс
создавался как спецификация вызываемого интерфейса SQL для разработки
переносимых (совместимых с множеством операционных систем) приложений
на C/C++, независимых от поставщика СУБД.

На основе предварительного проекта интерфейса уровня вызовов X/Open
корпорация Microsoft разработала открытые средства связи с базами
данных (Open Database Connectivity, ODBC), а позже международный
комитет ISO принял большинство спецификаций интерфейса уровня вызовов
X/Open в качестве стандарта SQL/CLI.

Интерфейс CLI DB2 основан на ODBC и Международном стандарте для
SQL/CLI.  Реализация CLI DB2 соответствует спецификации ODBC версии
3.51 и может использоваться в качестве драйвера ODBC при загрузке
диспетчером драйверов ODBC.

CLI/ODBC имеет следующие характеристики:
* программный код относительно легко переносится между продуктами
  нескольких поставщиков СУБД;
* в отличие от встраиваемого SQL, нет необходимости использовать
  прекомпиляцию, создавать файлы связей и загружать их в базу данных;
* высокая распространенность, поддержка множеством инструментов и продуктов.

Для выполнения приложения CLI/ODBC достаточно иметь драйвер CLI
DB2. Для разработки приложения CLI/ODBC необходимы драйвер CLI DB2 и
соответствующие библиотеки разработки.

OLE DB — это набор интерфейсов, предоставляющий доступ к данным из
различных источников.  Он разрабатывался корпорацией Microsoft как
замена ODBC, но был расширен для поддержки более широкого набора
источников, в том числе нереляционных баз данных, текстовых документов
и электронных таблиц.  Реализация OLE DB осуществляется с применением
технологии объектной модели программных компонентов (Component Object
Model, COM), также разработанной компанией Microsoft.

Пользователи OLE DB могут получить доступ к базам данных DB2 с помощью
компонента DB2 OLE DB Provider от компании IBM. Для пользователей ADO
(ActiveX Data Objects) в составе драйверов DB2 поставляется провайдер
ADO.NET для DB2, который является надстройкой над драйвером OLE DB.

## Разработка в среде Java

Интерфейс соединения с базами данных на Java (Java Database
Connectivity, JDBC) — это программный API Java, определяющий стандарты
средств для работы с базами данных и доступа к ним. Программный код
JDBC достаточно легко переносится между продуктами нескольких
поставщиков СУБД. Обычно в код необходимо вносить только изменения,
касающиеся выбора драйвера JDBC для загрузки и используемой строки
соединения.

В настоящее время работа с базами данных с использованием драйверов
JDBC обеспечивается в том числе и для языков программирования,
отличных от Java, но исполняемых средствами виртуальной машины Java
(Java Virtual Machine, JVM), включая Groovy, Scala, Closure, Jython.

SQLJ — это стандарт встраивания SQL в Java-программы. Он используется
преимущественно со статическим SQL, хотя совместим с JDBC. Хотя обычно
SQLJ-программы являются более компактными по сравнению с программами
JDBC, и обеспечивают более высокую производительность, этот стандарт
не получил широкого применения. Программы SQLJ необходимо обрабатывать
препроцессором (транслятором SQLJ) прежде, чем их можно будет
скомпилировать компилятором Java.

Используемое имя класса драйвера JDBC: `com.ibm.db2.jcc.DB2Driver`,
драйвер поставляется в виде архивов `db2jcc4.jar` (актуальная
линейка версий драйвера по спецификации JDBC 4.0) и `db2jcc.jar`
(устаревший драйвер по спецификации JDBC 3.0).

Типичный вариант URL для настройки JDBC-подключений
для драйвера Db2 типа 4:
`jdbc:db2://hostname.example.com:50000/bludb`.
В этом примере значение `50000` - номер порта,
`hostname.example.com` - имя сервера,
`bludb` - имя базы данных.

Дополнительная информация по формату URL и параметрам драйвера приведена в
[документации](https://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.apdv.java.doc/src/tpc/imjcc_tjvjcccn.html).

Различные версии JDBC-драйвера Db2 доступны для загрузки на
[специальной странице](https://www.ibm.com/support/pages/db2-jdbc-driver-versions-and-downloads).