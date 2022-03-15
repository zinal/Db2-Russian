# Создание хранимой процедуры для Db2 на языке PL/SQL

Активация режима совместимости с Oracle:

```bash
db2set DB2_COMPATIBILITY_VECTOR=ORA
db2stop force
db2start
```

Запуск командного интерпретатора CLPPlus (аналог Oracle SQLPlus):

```
$ clpplus -nw db2inst1@localhost:50000/demo1
CLPPlus: Версия 1.6
Copyright (c) 2009, 2011, IBM CORPORATION.  Все права защищены.

Введите пароль: ********

Информация соединения с базой данных :
--------------------------------------
Имя хоста = localhost 
Сервер баз данных = DB2/LINUXX8664  SQL110146 
ID авторизации SQL  = db2inst1 
Алиас локальной базы данных = DEMO1 
Порт = 50000 

SQL> 
```

CLPPlus в среде Linux довольно странно взаимодействует с буфером обмена.
Для исключения этого эффекта между вводом команд рекомендую дополнительно дважды
нажимать клавишу `ENTER`.

Создание примера таблички:
```SQL
CREATE SCHEMA demo1;

CREATE TABLE demo1.tab1 (
  pos INTEGER NOT NULL,
  val VARCHAR(100) NOT NULL,
  CONSTRAINT tab1_pk PRIMARY KEY(pos)
);
```

Создание структурного типа:

```SQL
CREATE TYPE demo1.typ1 AS OBJECT (C1 INTEGER, C2 VARCHAR(20));

CREATE OR REPLACE FUNCTION demo1.typ1_conv
  (C1 IN INTEGER, C2 IN VARCHAR) RETURN demo1.typ1 AS
BEGIN
  DECLARE v_x demo1.typ1;
  BEGIN
    v_x.c1 = c1;
    v_x.c2 = c2;
    RETURN v_x;
  END;
END;
/
```

Создание хранимой процедуры, принимающей параметр структурного типа:

```SQL
CREATE OR REPLACE PROCEDURE demo1.proc1(p1 IN demo1.typ1) AS
BEGIN
  INSERT INTO demo1.tab1(pos, val)
    SELECT 1 + COALESCE(MAX(pos),0),
           p1.c2 || ': ' || p1.c1
    FROM demo1.tab1;
END;
/
```

Вызов созданной хранимой процедуры:

```SQL
DECLARE
  v_x demo1.typ1;
BEGIN
  v_x.c1 := 100;
  v_x.c2 := 'Hundred';
  demo1.proc1(v_x);
  COMMIT;
END;
/

select * from demo1.tab1;
```
