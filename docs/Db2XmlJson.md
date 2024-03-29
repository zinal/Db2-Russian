# Данные XML и JSON в Db2

## Работа с данными XML

XML — это технология, лежащая в основе инструментов и технологий Web 2.0, а также SOA.
Технология DB2 pureXML предоставляет расширенные возможности для хранения и оперирования
XML-документами в DB2.

Сервер данных DB2 является гибридным: он дает возможность сохранять в исходном
формате как реляционные, так и иерархические (XML) данные. Старые версии DB2 и прочие
представленные на рынке серверы данных могли хранить XML-документы, однако методы хранения
и обработки, используемые в DB2 начиная с версии 9, повысили производительность и гибкость
работы с данными XML.

Данные XML в DB2 могут быть сохранены в колонках специального типа, с обеспечением
автоматического структурного разбора и (при необходимости) контроля соответствия структуры
документа заданной XML-схеме. Хранение XML-документов в разобранном виде позволяет:
- организовать доступ к полям XML-документов, в том числе при выполнении SQL-операторов
  для фильтрации записей по заданным условиям и формирования результатов запросов;
- ускорить выборку данных за счет создания индексов над хранимыми XML-документами,
  не ограничиваясь механизмами полнотекстового поиска, а с учетом структуры документа,
  и, тем самым, семантики данных;
- обеспечить возможность формирования XML-документов SQL-запросами;
- обеспечить выборку данных в табличной форме путем обращения к XML-документам.

Для хранимых данных XML обеспечивается сжатие, что существенно сокращает необходимый
объем дискового пространства для хранения XML-документов и снижает интенсивность ввода-вывода.

В целом по сравнению с традиционными подходами по хранению XML-документов и их фрагментов
в BLOB-полях, а также по полной декомпозиции XML-документов в набор записей реляционных таблиц,
технология DB2 pureXML предоставляет серьезные преимущества в производительности, гибкости
и доступных функциях.

## Работа с данными JSON

В DB2 версии 10.5 была добавлена поддержка работы с данными JSON, как распространенным
в настоящее время стандартом работы Web-приложений с иерархическими данными.

Хранение данных JSON осуществляется в двоичном формате BSON, с поддержкой расширений
поддерживаемых типов данных, разработанных для MongoDB. Работа с данными JSON обеспечивается
как с использованием инструментов командной строки (для управления и выборки данных JSON),
так и с использованием программных интерфейсов DB2:
- интерфейса для работы с данными JSON, встроенного в JDBC-драйвер DB2;</li>
- интерфейса любого драйвера, реализующего протокол MongoDB (включая штатные
  драйверы MongoDB, а также распространенные библиотеки для Java, C/C++, Perl,
  Python, Ruby и PHP).

Для работы с данными JSON из командной строки используется утилита db2nosql.
Она поддерживает как выполнение административных операций (например, создание необходимых
таблиц для хранения данных JSON), так и операции поиска, выборки, вставки, импорта
и экспорта данных.

Программные интерфейсы для работы с JSON в составе JDBC-драйвера DB2 обеспечивают
удобную работу с документами JSON, поддерживая операции поиска, выборки, вставки,
модификации и удаления данных.

Использование реализации протокола MongoDB на стороне сервера DB2 позволяет
применять все разработанные инструменты для работы с документами JSON,
используемые пользователями MongoDB.

Большинство современных Web-приложений работает как с иерархическими,
так и с реляционными данными, поэтому использование встроенной в DB2 поддержки
JSON и XML позволяет исключить необходимость развертывания нескольких различных
серверов баз данных для решения задач хранения данных.
