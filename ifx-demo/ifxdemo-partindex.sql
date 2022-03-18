
CREATE INDEX ifxdemo1_ix1 ON ifxdemo1(F)
  FRAGMENT BY EXPRESSION
  PARTITION part_0 (F<=-10000.0) IN rootdbs INDEX OFF,
  PARTITION part_1 REMAINDER IN work1;

CREATE INDEX ifxdemo2_ix1 ON ifxdemo2(F)
  FRAGMENT BY EXPRESSION
  PARTITION part_0 (F<=-10000.0) IN rootdbs INDEX OFF,
  PARTITION part_1 REMAINDER IN work1;

CREATE INDEX ifxdemo3_ix1 ON ifxdemo3(F)
  FRAGMENT BY EXPRESSION
  PARTITION part_0 (F<=-10000.0) IN rootdbs INDEX OFF,
  PARTITION part_1 REMAINDER IN work1;

CREATE INDEX ifxdemo4_ix1 ON ifxdemo4(F)
  FRAGMENT BY EXPRESSION
  PARTITION part_0 (F<=-10000.0) IN rootdbs INDEX OFF,
  PARTITION part_1 REMAINDER IN work1;

(SELECT 'ifxdemo1' AS tabname, AVG(F) AS rowcount FROM ifxdemo1 WHERE F<=-10000.0)
UNION ALL
(SELECT 'ifxdemo2' AS tabname, COUNT(*) AS rowcount FROM ifxdemo2 WHERE F<=-10000.0)
UNION ALL
(SELECT 'ifxdemo3' AS tabname, COUNT(*) AS rowcount FROM ifxdemo3 WHERE F<=-10000.0)
UNION ALL
(SELECT 'ifxdemo4' AS tabname, COUNT(*) AS rowcount FROM ifxdemo4 WHERE F<=-10000.0);
