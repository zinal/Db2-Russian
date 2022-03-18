-- Initial data filler for dummy workload generator

ALTER TABLE ifxdemo1 DROP CONSTRAINT ifxdemo1_pk;
ALTER TABLE ifxdemo1 TYPE (RAW);

WITH r100(rnum) AS (
  SELECT rnum - 1 FROM (
    SELECT ROW_NUMBER() OVER() AS rnum FROM "informix".syscolumns
  ) x WHERE rnum<=100
)
INSERT INTO ifxdemo1(A,B,C,D,E,F)
SELECT r1.rnum + 100 * (r2.rnum + 100 * r3.rnum) + 1,
   j_rand_str(CASE WHEN MOD(r1.rnum,2)=1 THEN 55 ELSE 30 END),
   j_rand_str(CASE WHEN MOD(r1.rnum,2)=1 THEN 45 ELSE 20 END),
   CURRENT YEAR TO FRACTION,
   dbms_random_random(),
   -10001.0
FROM r100 r1, r100 r2, r100 r3;

ALTER TABLE ifxdemo1 TYPE (STANDARD);
ALTER TABLE ifxdemo1 ADD CONSTRAINT PRIMARY KEY(A) CONSTRAINT ifxdemo1_pk;

ALTER TABLE ifxdemo2 DROP CONSTRAINT ifxdemo2_pk;
ALTER TABLE ifxdemo2 TYPE (RAW);
INSERT INTO ifxdemo2 SELECT * FROM ifxdemo1;
ALTER TABLE ifxdemo2 TYPE (STANDARD);
ALTER TABLE ifxdemo2 ADD CONSTRAINT PRIMARY KEY(A) CONSTRAINT ifxdemo2_pk;

ALTER TABLE ifxdemo3 DROP CONSTRAINT ifxdemo3_pk;
ALTER TABLE ifxdemo3 TYPE (RAW);
INSERT INTO ifxdemo3 SELECT * FROM ifxdemo1;
ALTER TABLE ifxdemo3 TYPE (STANDARD);
ALTER TABLE ifxdemo3 ADD CONSTRAINT PRIMARY KEY(A) CONSTRAINT ifxdemo3_pk;

ALTER TABLE ifxdemo4 DROP CONSTRAINT ifxdemo4_pk;
ALTER TABLE ifxdemo4 TYPE (RAW);
INSERT INTO ifxdemo4 SELECT * FROM ifxdemo1;
ALTER TABLE ifxdemo4 TYPE (STANDARD);
ALTER TABLE ifxdemo4 ADD CONSTRAINT PRIMARY KEY(A) CONSTRAINT ifxdemo4_pk;

(SELECT 'ifxdemo1' AS tabname, COUNT(*) AS rowcount FROM ifxdemo1)
UNION ALL
(SELECT 'ifxdemo2' AS tabname, COUNT(*) AS rowcount FROM ifxdemo2)
UNION ALL
(SELECT 'ifxdemo3' AS tabname, COUNT(*) AS rowcount FROM ifxdemo3)
UNION ALL
(SELECT 'ifxdemo4' AS tabname, COUNT(*) AS rowcount FROM ifxdemo4);

-- L0 backup is needed for the affected dbspaces ( onbar -b -L 0 )

-- End Of File
