-- SQL0286N: create user temporary tablespace usertemp1 pagesize 32768;

-- 1. Подготовка (создание и начальное заполнение временных таблиц)
UPDATE MONITOR SWITCHES USING BUFFERPOOL ON;
UPDATE MONITOR SWITCHES USING LOCK ON;
UPDATE MONITOR SWITCHES USING SORT ON;
UPDATE MONITOR SWITCHES USING TIMESTAMP ON;

DECLARE GLOBAL TEMPORARY TABLE mvz_metrics_0 (
  metric_name VARCHAR(128) NOT NULL,
  total_time_value BIGINT NOT NULL,
  total_count BIGINT NOT NULL
) ON COMMIT PRESERVE ROWS NOT LOGGED;

DECLARE GLOBAL TEMPORARY TABLE mvz_metrics_1 (
  metric_name VARCHAR(128) NOT NULL,
  total_time_value BIGINT NOT NULL,
  total_count BIGINT NOT NULL,
  parent_name VARCHAR(128)
) ON COMMIT PRESERVE ROWS NOT LOGGED;

INSERT INTO session.mvz_metrics_0
SELECT f.metric_name,
       COALESCE(SUM(f.total_time_value), 0),
       COALESCE(SUM(f.count), 0)
FROM TABLE(MON_GET_WORKLOAD_DETAILS(NULL, -2)) AS x,
     TABLE(MON_FORMAT_XML_TIMES_BY_ROW(x.details)) AS f
GROUP BY f.metric_name;

-- End Of File
