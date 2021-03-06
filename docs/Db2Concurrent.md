# Организация параллельной транзакционной обработки

**Транзакция** (или единица работы, unit of work, UOW) состоит из одного или
нескольких операторов SQL, которые при выполнении рассматриваются как отдельная
единица; иными словами, сбой одного оператора транзакции приводит к сбою целой
транзакции, при этом все операторы, выполненные до момента сбоя, откатываются.

Транзакция заканчивается оператором `COMMIT`. Транзакция также
может закончиться оператором `ROLLBACK` либо аварийным
(нештатным) отключением приложения, после которого все
внесённые приложением изменения в базу данных будут отменены. Началом
транзакции является первый выполненный оператор после открытия соединения
приложения с базой данных либо после завершения предыдущей транзакции. Каждое
соединение приложения с базой данных может иметь не более одной активной
транзакции.

Как уже было указано ранее,   изменения
в базе данных фиксируются в транзакционном журнале. Для обеспечения возможности
«отката» изменений,   внесенных отменяемой транзакцией,
в транзакционном журнале также фиксируются границы
транзакций. При этом для транзакций,   выполняющих только
операции чтения данных,   запись в транзакционный журнал
не ведется. Информация о начале транзакции помещается в транзакционный журнал
перед началом выполнения первого (для данной транзакции) оператора записи
данных.

В случае ошибки выполнения единичного оператора, 
осуществляющего запись данных,   все
изменения,   внесенные данным оператором,  
отменяются с использованием данных транзакционного журнала. Приложение,
получив диагностическое сообщение об отказе в выполнении
оператора,   может отменить всю транзакцию (оператором 
`ROLLBACK`)   либо выполнить какие-то другие действия с базой
данных и,   в итоге,   подтвердить
внесённые изменения   (оператором   `COMMIT`).

Приложение может определять дополнительные точки отката в
рамках транзакции   (с помощью оператора 
`SAVEPOINT`)   и отменять изменения,   выполненные
после созданной точки отката   (с помощью оператора 
`ROLLBACK TO`).   Использование точек отката позволяет приложению
выборочно отменять выполненные в рамках транзакции действия,   что
может быть полезно при обработке ошибок контроля целостности данных и в других
сценариях.

### Конкурентный доступ и блокировки

Параллельное использование данных подразумевает, что несколько
пользователей могут одновременно работать над одними и теми же объектами базы
данных. Доступ к данным необходимо должным образом координировать для
обеспечения целостности и согласованности данных.

Для получения согласованных результатов параллельно
выполняемых транзакций требуется контроль параллельного использования общих
ресурсов. Такой контроль основан на применении блокировок.

Концепции блокировки и параллельного использования тесно
взаимосвязаны. Блокировка временно запрещает приложениям выполнять другие
операции до завершения текущей операции. Чем активнее в системе применяется
блокировка, тем меньше остается возможностей параллельного использования. С
другой стороны, чем реже блокировка применяется в системе, тем больше появляется
возможностей параллельного использования.

Блокировка срабатывает автоматически по мере необходимости
для поддержки транзакции и отключается после прерывания такой транзакции (с
помощью команды `COMMIT` или `ROLLBACK`). Блокировка может устанавливаться на
таблицы или строки.

Существует два основных типа блокировки:
* Общая блокировка (S) — устанавливается, когда приложение
  считывает данные и не допускает внесения изменений в ту же строку другими приложениями.
* Эксклюзивная блокировка (X) — устанавливается, когда приложение обновляет,
  вставляет или удаляет строку.

Если двум и больше приложениям необходимо выполнить операцию
с одним и тем же объектом, одному из них придется подождать, чтобы установить
требуемую блокировку. По умолчанию, приложение будет ждать бесконечно долго.
Временем ожидания блокировки приложением управляет параметр конфигурации базы
данных `LOCKTIMEOUT`. По умолчанию этот параметр имеет значение 
`-1` (бесконечное ожидание).

Для установки времени ожидания блокировки в определенном подключении
можно использовать сессионную переменную `CURRENT LOCK TIMEOUT`.
По умолчанию для этой переменной задано значение, соответствующее параметру
`LOCKTIMEOUT`. Чтобы изменить это значение для текущей сессии,
можно воспользоваться оператором `SET LOCK TIMEOUT`.

В случае,   когда два приложения (или более),   подключенных к одной базе данных,
бесконечно долго ожидают ресурсов вследствие неправильной последовательности обращения к
этим ресурсам,   возникает ситуация **взаимоблокировки**.
Период ожидания не может закончиться, поскольку каждое из приложений удерживает
ресурс, необходимый другому приложению. Во всех случаях проблема
взаимоблокировки связана с неправильной структурой или логикой приложений.

Db2 обеспечивает автоматическое обнаружение ситуаций взаимоблокировки,
выполняя соответствующие проверки с периодичностью, заданной
параметром `DLCHKTIME`. Обнаружив взаимоблокировку, Db2
воспользуется внутренним алгоритмом, чтобы определить, какую из двух
транзакций необходимо откатить (завершить с ошибкой), а какую
продолжить.

Более подробная информация о реализации механизма управления конкурентным доступом 
с помощью блокировок приведена в
[официальной документации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.perf.doc/doc/c0054923.html).

### Уровни изоляции

Детальный анализ проблем,   которые могут проявиться при отсутствии контроля
параллельного использования ресурсов,   приведён в
документации Db2, а также в литературе по теории функционирования
реляционных СУБД. Возможные виды проблем включают в себя:
* потерянное обновление (при одновременном изменении одного блока
  данных разными транзакциями, одно из изменений теряется);
* недостоверное чтение (чтение данных, добавленных или изменённых
  транзакцией, которая впоследствии не подтвердится);
* неповторяющееся чтение (при повторном чтении в рамках одной
  транзакции, ранее прочитанные данные оказываются изменёнными);
* фантомное чтение (одни и те же выборки в одной транзакции дают
  разные множества строк из-за добавления,   удаления либо
  изменения строк другими транзакциями).

Управление со стороны приложений встроенными в 
Db2   механизмами защиты от перечисленных выше проблем
осуществляется с помощью установки используемого уровня изоляции. Уровни
изоляции можно рассматривать как политики блокировки, в которых, в зависимости
от выбранного уровня изоляции, можно достичь различных вариантов блокировки
базы данных приложением. Требуемый приложением уровень изоляции может быть
установлен на уровне сеанса и на уровне отдельного выполняемого запроса либо
подзапроса.

Db2 предоставляет следующие уровни защиты для изоляции данных:
* недостоверное чтение (Uncommitted Read, UR);
* стабильность курсора (Cursor Stability, CS);
* стабильность чтения (Read Stability, RS);
* повторяемое чтение (Repeatable Read, RR).

**Недостоверное чтение** также называют «грязным». Это
самый низкий уровень изоляции, который допускает наивысшую степень
параллельного использования. При операциях чтения строки не блокируются, за
исключением случаев, когда другое приложение пытается удалить или изменить
таблицу; операции обновления выполняются так же, как при использовании
следующего уровня изоляции — уровня «Стабильность курсора».

При использовании уровня изоляции «Недостоверное чтение»
предотвращаются следующие проблемы:
* потерянное обновление.

**Стабильность курсора** — это уровень изоляции по
умолчанию. Он обеспечивает минимальную степень блокировки. При этом уровне
изоляции блокируется «текущая» строка курсора. Если строка только считывается,
блокировка сохраняется до перехода на новую строку или завершения операции.
Если строка обновляется, блокировка сохраняется до завершения операции.

При использовании уровня изоляции «Стабильность курсора»
предотвращаются следующие проблемы:
* потерянное обновление;
* недостоверное чтение.

До выхода Db2 9.7 при использовании уровня изоляции
«Стабильность курсора» выполнение записи (операция `UPDATE`) закрывало для чтения
(операция `SELECT`) доступ к той же строке. Логической основой для такого поведения
служило то, что, поскольку операция записи вносит в строку изменения, то для чтения
следует дождаться завершения обновлений, чтобы получить окончательное зафиксированное
значение.

В Db2 9.7 и более поздних по умолчанию  для новых баз данных используется
другой подход к обработке конкурентного доступа на уровне изоляции «Стабильность курсора».
Этот новый подход реализован с помощью «принятой на текущий момент»
(currently committed, CC) семантики.
При использовании CC-семантики операция записи не закрывает доступ к
той же строке для операции чтения. Ранее такой подход был возможен только
при использовании уровня изоляции UR; разница с текущим подходом заключается
в том, что при UR операция чтения получает недостоверные значения,
а при CC-семантике — значения, принятые на текущий момент. Принятые
на текущий момент значения — это значения, зафиксированные до начала операции
записи.

**Стабильность чтения** обеспечивает блокировку всех
строк,   получаемых приложением. Для заданного запроса
блокируются все строки, соответствующие набору результатов. Таким образом,
использование данного режима изоляции может привести к
захвату приложением большого количества блокировок,   и,
в случае достижения установленных ограничений,
к эскалации блокировок с уровня строк на уровень таблиц.
Тем не менее, на уровне изоляции «Стабильность чтения»
оптимизатор запросов Db2 не включает в план выполнения
выполняемых запросов операций по явному захвату блокировок на уровне таблиц,
даже если в набор результатов попадает большая часть
записей таблицы.

При использовании уровня изоляции «Стабильность чтения»
предотвращаются следующие проблемы:
* потерянное обновление;
* недостоверное чтение;
* неповторяющееся чтение.

**Повторяющееся чтение** — это наивысший уровень
изоляции. Он предоставляет наивысшую степень блокировки и меньше всего
возможностей параллельного использования. Блокировка устанавливается на строки,
которые обрабатываются для построения набора результатов; иными словами, могут
блокироваться даже те строки, которые не попадут в конечный пакет результатов.
Другие приложения не могут обновлять, удалять или вставлять строки, которые
повлияют на набор результатов, пока выполняемая операция не будет завершена.
Повторяющееся чтение гарантирует, что один и тот же запрос, созданный
приложением несколько раз за одну операцию, каждый раз будет выводить
одинаковые результаты.

Оптимизатор запросов Db2 при использовании уровня изоляции «Повторяющееся чтение»
может включать в план выполнения запросов явные операции установки блокировок
на уровне таблиц в случае, когда соответствующие запросы предполагают
сканирование всех строк таблицы (что означает необходимость заблокировать
каждую строку таблицы в ходе выполнения запроса).

При использовании уровня изоляции «Повторяющееся чтение»
предотвращаются все возможные проблемы конкурентного доступа, но
одновременно максимально ограничивается возможный параллелизм выполнения
операций.

Более подробная информация об уровнях изоляции Db2 приведена в
[официальной документации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.perf.doc/doc/c0004121.html).
