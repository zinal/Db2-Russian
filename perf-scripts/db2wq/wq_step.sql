-- Pre-req: wq_init.sql

-- 2. Итерация по завершении периода ожидания и подготовка к следующей итерации
DELETE FROM session.mvz_metrics_1;

INSERT INTO session.mvz_metrics_1
SELECT f.metric_name,
       COALESCE(SUM(f.total_time_value), 0),
       COALESCE(SUM(f.count), 0),
       f.parent_metric_name
FROM TABLE(MON_GET_WORKLOAD_DETAILS(NULL, -2)) AS x,
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
