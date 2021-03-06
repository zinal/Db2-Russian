            <heading alttoc="" refname="schemamig" type="h2" back-to-top="no"
                >Изменение структуры баз данных</heading>

<p>IBM DB2 реализует комплекс функций и средств для обеспечения изменения структуры баз
данных и взаимосвязанных программных объектов с минимальным воздействием на функционирование
приложений. Правильное использование этих средств позволяет минимизировать либо полностью
исключить периоды приостановки доступа пользователей к данным и приложениям.</p>

<p>Многие операции изменения структуры таблиц требуют физической реорганизации хранения данных
в изменяемой таблице. Например, при радикальном изменении типа данных колонки таблицы
(число - строка, или наоборот) необходимо прочитать каждую запись таблицы, изменить её структуру
и записать обратно.</p>

<p>Изменения структуры таблицы, которые не требуют физической реорганизации,
сохраняют таблицу доступной для чтения и записи, но будут учтены при проведении
следующей плановой реорганизации. Пример: добавление колонок в существующую таблицу
обрабатывается без прекращения доступа приложений к данной таблице и не требует
обновления всех записей таблицы.</p>

<p>В рамках одной транзакции может быть выполнено неограниченное количество изменений
структуры таблицы без выполнения её реорганизации, при этом доступ к данным таблицы
может быть приостановлен до выполнения следующей реорганизации. Реализованная в DB2
отслеживания изменений структуры таблицы, требующих её реорганизации, более
детально описана в <a href="http://www-01.ibm.com/support/docview.wss?uid=swg21440178"
>следующем техническом документе</a>, а также в разделе
«<a href="http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.admin.dbobj.doc/doc/r0053739.html"
>Altering tables</a>» документации DB2.</p>

<p>Обратите внимание, что командный процессор DB2 по умолчанию подтверждает транзакцию
после каждой выполненной команды - это часто приводит к отказам выполнения
второй и последующих операций изменения структуры таблицы (будет возвращено сообщение
о необходимости выполнить реорганизацию).
Поэтому перед выполнением серии операций изменения структуры таблицы необходимо отключить
автоматическое подтверждение транзакций командным процессором (например, с помощью команды
<code>UPDATE COMMAND OPTIONS USING c OFF</code>).</p>

<p>IBM DB2 также поддерживает полную реорганизацию структуры таблиц без приостановки
доступа к реорганизуемым таблицам, с помощью административной процедуры <code>ADMIN_MOVE_TABLE</code>.
Описание порядка работы с данной функцией приведено в
<a href="http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.sql.rtn.doc/doc/r0055069.html"
>документации DB2</a>. Административная процедура <code>ADMIN_MOVE_TABLE</code> осуществляет
копирование данных из исходной таблицы в новую (с требуемой структурой), выполняя отслеживание
изменений данных в ходе копирования и применяя изменения к созданной копии, а затем
заменяет исходную таблицу вновь созданной копией. Одним из вариантов применения данной
процедуры является перемещение существующей таблицы в другое табличное пространство.
Примеры использования данной функции опубликованы в статье 
«<a href="http://www.ibm.com/developerworks/data/library/dmmag/DMMag_2011_Issue4/DistributedDBA/"
>Distributed DBA: Table movement made easy</a>».</p>

<p>В отличие от таблиц, изменение программных объектов (представлений, хранимых процедур,
пользовательских функций и триггеров) осуществляется путем их полного пересоздания.
Программные объекты могут зависеть от структуры таблиц и от других программных объектов (например,
представления могут обращаться к таблицам, другим представлениям и пользовательским функциям).
Зависимости между объектами базы данных отслеживаются DB2, и в случае удаления объектов (либо
блокировки доступа к ним) зависимые программные объекты отмечаются как некорректные
(требующие перекомпиляции либо исправления). IBM DB2 поддерживает автоматическую
перекомпиляцию зависимых объектов (automatic revalidation, см.
<a href="http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.admin.dbobj.doc/doc/c0055269.html"
>документацию</a>), выполняемую при первом обращении к объекту, требующему перекомпиляции.
По умолчанию для всех новых баз данных, созданных в DB2 версии 9.7 и более поздних,
используется режим автоматической перекомпиляции.</p>

<p>Рекомендуемым методом изменения программного объекта является замена его описания
с помощью оператора <code>CREATE OR REPLACE</code>. Применение данного оператора
сохраняет выданные привилегии на доступ к объекту, в то время как удаление и повторное
создание объекта потребует повторной выдачи всех ранее установленных полномочий.
IBM DB2 поддерживает режим мягкого удаления (soft invalidation, см.
<a href="http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.admin.dbobj.doc/doc/c0053738.html"
>документацию</a>), используемый по умолчанию, который позволяет удалять и заменять
программные объекты базы данных даже в тот момент, когда они используются работающими
приложениями.</p>

<p>Выполнение необходимых операций реорганизации таблиц и перекомпиляции программных
объектов могут быть автоматизированы с использованием административной процедуры
<code>ADMIN_REVALIDATE_DB_OBJECTS</code>. Данная процедура определяет объекты
базы данных, которые необходимо реорганизовать либо перекомпилировать, и выполняет
соответствующие операции. Инструкции по использованию данной процедуры приведены в
<a href="http://www.ibm.com/support/knowledgecenter/SSEPGG_11.1.0/com.ibm.db2.luw.sql.rtn.doc/doc/r0053626.html"
>документации DB2</a>.</p>
