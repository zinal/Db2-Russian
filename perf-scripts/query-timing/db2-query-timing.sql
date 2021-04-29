-- Measure query time distribution for a specific set of queries
-- To run on OpenShift:
-- DB2WH=c-db2wh-1619528657651763-db2u-0
-- oc cp db2-query-timing.sql $DB2WH:/tmp/db2-query-timing.sql
-- oc rsh $DB2WH
-- su - db2inst1
-- db2 connect to bludb
-- db2 create user temporary tablespace usertemp1 pagesize 32768  # in case of SQL0286N
-- db2 -tf /tmp/db2-query-timing.sql
-- db2 connect to bludb
-- db2 "create unique index dwh.h_test_pk on dwh.h_test(id) page split high"
-- db2 "alter table dwh.h_test add constraint h_test_pk primary key(id) enforced"

DECLARE GLOBAL TEMPORARY TABLE mvz_metrics_0 (
  metric_name VARCHAR(128) NOT NULL,
  total_time_value BIGINT NOT NULL,
  total_count BIGINT NOT NULL
) ON COMMIT PRESERVE ROWS;

DECLARE GLOBAL TEMPORARY TABLE mvz_metrics_1 (
  metric_name VARCHAR(128) NOT NULL,
  total_time_value BIGINT NOT NULL,
  total_count BIGINT NOT NULL,
  parent_name VARCHAR(128)
) ON COMMIT PRESERVE ROWS;


CALL WLM_SET_CONN_ENV(NULL, '<collectactdata>WITH DETAILS, SECTION</collectactdata><collectactpartition>ALL</collectactpartition>');
COMMIT;

INSERT INTO session.mvz_metrics_0
SELECT f.metric_name,
       COALESCE(SUM(f.total_time_value), 0),
       COALESCE(SUM(f.count), 0)
FROM TABLE(MON_GET_CONNECTION_DETAILS(MON_GET_APPLICATION_HANDLE(), -2)) AS x,
     TABLE(MON_FORMAT_XML_TIMES_BY_ROW(x.details)) AS f
GROUP BY f.metric_name;


values ('*** BEFORE', MON_GET_APPLICATION_HANDLE(), current timestamp);

COMMIT;

-- BEGIN WORKLOAD

INSERT INTO DWH.H_TEST(ID, ID_SYSTEM, ID_ORIGINAL)
WITH s10(id) AS (SELECT SMALLINT(1) AS t FROM syscat.tables FETCH FIRST 10 ROWS ONLY),
     s1k(id) AS (SELECT SMALLINT(1) AS t FROM s10 q1, s10 q2, s10 q3),
     s1m(id) AS (SELECT SMALLINT(1) AS t FROM s1k q1, s1k q2),
     sq(id) AS (SELECT BIGINT(ROW_NUMBER() OVER()) AS ID FROM s1m)
SELECT ID,
       BIGINT(RAND()*1000000000) AS ID_SYSTEM,
       123 + ID * 10
FROM sq;

-- END WORKLOAD

COMMIT;

values ('*** AFTER', MON_GET_APPLICATION_HANDLE(), current timestamp);


INSERT INTO session.mvz_metrics_1
SELECT f.metric_name,
       COALESCE(SUM(f.total_time_value), 0),
       COALESCE(SUM(f.count), 0),
       f.parent_metric_name
FROM TABLE(MON_GET_CONNECTION_DETAILS(MON_GET_APPLICATION_HANDLE(), -2)) AS x,
     TABLE(MON_FORMAT_XML_TIMES_BY_ROW(x.details)) AS f
GROUP BY f.metric_name, f.parent_metric_name;


WITH datum(metric_name, parent_name, total_time, total_count) AS (
    SELECT m1.metric_name, m1.parent_name,
           CASE WHEN m0.total_time_value IS NULL
                  OR m0.total_time_value > m1.total_time_value THEN
             m1.total_time_value
           ELSE
             m1.total_time_value - m0.total_time_value
           END,
           CASE WHEN m0.total_count IS NULL
                  OR m0.total_count > m1.total_count THEN
             m1.total_count
           ELSE
             m1.total_count - m0.total_count
           END
    FROM session.mvz_metrics_1 m1
    INNER JOIN session.mvz_metrics_0 m0
        ON m0.metric_name=m1.metric_name
), recq(metric_name, total_time, total_count, cur_percent, lvl, chain) AS (
    (SELECT metric_name, total_time, total_count, 100.0, 1, metric_name
     FROM datum WHERE COALESCE(parent_name,'') NOT IN
        (SELECT metric_name FROM session.mvz_metrics_1))
    UNION ALL
    (SELECT d.metric_name, d.total_time, d.total_count,
            100.0 * d.total_time / NULLIF(r.total_time, 0),
            r.lvl+1, r.chain||'|'||d.metric_name
     FROM datum d, recq r
     WHERE d.parent_name=r.metric_name)
) SELECT lvl, total_time,
         SUBSTR(VARCHAR_FORMAT(COALESCE(cur_percent,0), '990.0'),1,6) AS "percent",
         SUBSTR(REPEAT('  ', lvl) || metric_name, 1, 60) AS metric_name
  FROM recq ORDER BY chain;

-- End Of File
