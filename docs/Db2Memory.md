# Использование оперативной памяти

Модель памяти Db2 состоит из различных областей памяти на уровне
экземпляра Db2, базы данных, приложения и агента.

Детальное описание областей памяти Db2 приведено в
[документации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.perf.doc/doc/c0005383.html),
ниже представлено краткое описание назначения различных областей.

Перечень основных областей памяти Db2 приведен на рисунке ниже (изначально взятом из
[официальной документации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.admin.perf.doc/doc/c0005387.html).

![Основные области памяти DB2](https://raw.githubusercontent.com/zinal/Db2-Russian/master/db2-overview/part01/images/db2-memory-areas.gif)

Общая память,  используемая экземпляром СУБД, включает в себя:
* Monitor Heap –   область памяти для ведения
  мониторинга операций и состояния,   размер регулируется
  параметром `MON_HEAP_SZ`;
* FCM Buffers –   область памяти для
  взаимодействия между координирующим агентом и его субагентами,
  а также для обеспечения внутренних взаимодействий в многораздельных
  базах данных;
* Audit Buffer –   область памяти,   в которую помещаются записи аудита
  перед сбросом в журнал аудита.

На уровне базы данных принято различать:
* глобальную область базы данных,   часто
  называемую «Областью производительности» («Performance memory»),   включающую
  различные области кэширования и область ведения блокировок  ;
* область данных приложений,   часто
  называемую «Функциональной областью» («Functional memory») и включающую различные
  рабочие области памяти агентов,   обслуживающих подключения к
  базе данных.


Глобальная область базы данных состоит из следующих основных компонентов:
* Buffer pools – буферные пулы, т.е. области для кэширования данных табличных пространств;
* Lock list – область для хранения информации о блокировках,   размер которой регулируется
  параметром `LOCKLIST`;
* Package cache –   область для кэширования
  планов выполнения запросов,   размер регулируется
  параметром `PCKCACHESZ`;
* Catalog cache –   область для кэширования
  системного каталога, включающего в себя описания всех объектов базы данных,
  размер регулируется параметром `CATALOGCACHE_SZ`;
* Utility heap –   оперативная память для
  выполнения операций обслуживания базы данных (включая операции резервного
  копирования и восстановления),   размер регулируется
  параметром   `UTIL_HEAP_SZ`;
* Database heap –   оперативная память для
  обслуживания операций базы данных (включая буфер транзакционного журнала и кэш
  для ускорения доступа к системному каталогу,   а также
  буфер аудита на уровне базы данных),   размер
  регулируется параметром `DBHEAP`.

Суммарный объем глобальной области базы данных ограничен параметром
`DATABASE_MEMORY`.

Область данных приложений включает в себя:
* Application Global Memory – общие области
  памяти,   совместно используемые при обработке запросов
  приложений, максимальный объем регулируется параметром `APPL_MEMORY`;
* Agent Private Memory –   частные области
  памяти,   используемые для функционирования отдельных
  агентов,   обслуживающих подключенные приложения.


Дополнительно можно выделить области памяти, выделяемые для работы
драйвера Db2 на стороне приложения. Для локальных приложений
(использующих протокол межпроцессного взаимодействия, а не сетевой
доступ для подключении к менеджеру баз данных) установленные параметры
Db2 регулируют объем выделяемой оперативной памяти (преимущественно
это параметр `ASLHEAPSZ`).


## Управление оперативной памятью при выполнении операций сортировки

При выполнении многих видов операций СУБД требуется осуществить
сортировку данных, поэтому управлению оперативной памятью,
используемой для сортировки, уделяется особое внимание.

В случае невозможности размещения области сортировки целиком в
оперативной памяти, данные для сортировки размещаются в системном
временном табличном пространстве. Производительность запросов,
требующих таких объемных операций сортировки, может быть значительно
снижена.

Параметры, управляющие выделением оперативной памяти для сортировки:
* `SORTHEAP` – предельный размер памяти для операции сортировки;
* `SHEAPTHRES` –  предельный размер частной
  области памяти агента,   выделенной для операции сортировки;
* `SHEAPTHRES_SHR` – предельный объем общей
  оперативной памяти, которая может быть использована для
  выполнения операций сортировки (суммарно всеми потребителями) в каждый момент
  времени.

В Db2 поддерживается три модели управления памятью сортировки:
* Модель общей области сортировки (shared sort) –   используется
  по умолчанию, активируется установкой параметра 
  `SHEAPTHRES` в значение 0. Выделение оперативной памяти для
  сортировки осуществляется из глобальной области базы данных.
* Модель частной области сортировки (private sort) -  используется
  при ненулевом значении параметра `SHEAPTHRES` и отсутствии
  настроенной общей памяти сортировки. Выделение оперативной памяти
  для сортировки осуществляется из области данных приложений (точнее,
  из частных областей, принадлежащих агентам).
* Гибридная модель сортировки (hybrid sort) –   используется
  при ненулевом значении параметра `SHEAPTHRES` и наличии
  настроенной общей памяти сортировки. Операции, требующие
  использования общей памяти сортировки,   выполняются с
  выделением памяти в глобальной области базы данных,   остальные
  операции сортировки выполняются с выделением памяти в частных областях агентов.

Использование общей (глобальной) памяти для выполнения операций сортировки
предоставляет ряд важных преимуществ:
* более гибкое управление оперативной памятью при выполнении запросов,
  позволяющее увеличить эффективность использования оперативной памяти;
* возможность использования параллельного варианта алгоритма сортировки за
  счет наличия одновременного доступа к области памяти сортировки у координирующего
  агента и подчиненных ему суб-агентов Db2.

Для включения возможности использования общей памяти при выполнении
операций сортировки может использоваться одна из следующих настроек  :  
* активирована модель общей области сортировки путем установки
  параметра `SHEAPTHRES`  в значение 0;
* активирован параллелизм выполнения операций путем установки
  параметра `INTRA_PARALLEL` в значение `YES`;
* переменная `DB2_WORKLOAD` установлена в
  значение `ANALYTICS`;
* активирована функция DB2 Connection Concentrator (обычно используется при
  организации доступа к базам данных DB2 for z/OS и DB2 for i, см. описание данной функции в
[документации](http://www.ibm.com/support/knowledgecenter/SSEPGG_11.5.0/com.ibm.db2.luw.qb.dbconn.doc/doc/c0006169.html)).


## Автоматическое управление распределением памяти

Наличие большого количества различных областей оперативной памяти и
параметров, регулирующих их объем, может требовать значительных усилий
для ручной настройки сервера Db2. Поэтому, начиная с версии 9, IBM Db2
поддерживает автоматическое управление распределением оперативной
памяти между различными областями с использованием
самонастраивающегося менеджера памяти (STMM, Self-Tuning Memory
Manager).

Если функция самонастройки включена, STMM динамически распределяет
доступные ресурсы памяти между потребителями оперативной памяти в базе
данных.  STMM реагирует на изменения характеристик рабочей нагрузки,
регулируя значения параметров конфигурирования памяти и размер
буферных пулов для оптимизации производительности. Чтобы включить
STMM, необходимо установить для параметра конфигурации базы данных
`SELF_TUNING_MEM` значение `ON`.

Автоматическое управление распределением памяти ведётся для тех
областей памяти, для которых оно было явно разрешено. При установке
значения параметра конфигурации командами `UPDATE DBM CFG` и `UPDATE
DB CFG`, для использования STMM после значения параметра указывается
ключевое слово `AUTOMATIC`.  Указанное при этом числовое значение
параметра используется как начальное, далее STMM осуществляет
периодическую корректировку значений с учетом текущей нагрузки,
перераспределяя оперативную память между различными потребителями.

Автоматическое управление средствами STMM поддерживается для
следующих параметров:
* `INSTANCE_MEMORY` –   суммарный объем оперативной памяти экземпляра Db2;
* `DATABASE_MEMORY` –   глобальные области базы данных;
* `DBHEAP` –   область для обслуживания операций базы данных;
* `LOCKLIST` –   область ведения данных о блокировках;
* `MAXLOCKS` –   процент занятия памяти
  блокировками одного приложения для перехода к эскалации блокировок;
* `PCKCACHESZ` –   область кэширования планов выполнения запросов;
* `SHEAPTHRES_SHR` –   общая область сортировки;
* `SORTHEAP` –   размер области сортировки для одной операции;
* `APPL_MEMORY` –   область функциональной памяти;
* `APPLHEAPSZ` –   предельный объем частной памяти, используемый одним агентом;
* `STMTHEAP` –   ограничение на размер области,
  используемой компилятором   SQL   и  XQuery   запросов (на один запрос);
* `STAT_HEAP_SZ` –   максимальный объем оперативной памяти,
  выделяемый для построения статистик утилитой
  `RUNSTATS` и выделяемый из функциональной области памяти;
* размера буферных пулов, для которых включено автоматическое управление.