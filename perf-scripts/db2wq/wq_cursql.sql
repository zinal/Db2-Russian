
SELECT
  APPLICATION_HANDLE AS hndl,
  SUBSTR(APPLICATION_NAME,1,32) AS aname,
  ELAPSED_TIME_SEC AS elapsed,
  ACTIVITY_STATE AS astate,
  ACTIVITY_TYPE AS atype,
  TOTAL_CPU_TIME AS cpu_time,
  SUBSTR(STMT_TEXT,1,500) AS stmt_text
FROM SYSIBMADM.MON_CURRENT_SQL
ORDER BY COALESCE(elapsed_time_sec,0) DESC;

WITH datum(hndl, metric_name, total_time, total_count, parent_name) AS (
    SELECT q.application_handle AS hndl,
           f.metric_name,
           COALESCE(SUM(f.total_time_value), 0),
           COALESCE(SUM(f.count), 0),
           f.parent_metric_name
    FROM SYSIBMADM.MON_CURRENT_SQL q,
         TABLE(WLM_GET_WORKLOAD_OCCURRENCE_ACTIVITIES_V97(q.application_handle, -2)) o,
         TABLE(MON_GET_ACTIVITY_DETAILS(o.application_handle, o.uow_id, o.activity_id, o.dbpartitionnum)) x,
         TABLE(MON_FORMAT_XML_TIMES_BY_ROW(x.details)) AS f
    WHERE q.elapsed_time_sec > 10
    GROUP BY q.application_handle, f.metric_name, f.parent_metric_name
), recq(hndl, metric_name, total_time, total_count, cur_percent, lvl, chain) AS (
    (SELECT hndl, metric_name, total_time, total_count, 100.0, 1, metric_name
     FROM datum WHERE COALESCE(parent_name,'')='')
    UNION ALL
    (SELECT d.hndl, d.metric_name, d.total_time, d.total_count,
            100.0 * d.total_time / NULLIF(r.total_time, 0),
            r.lvl+1, r.chain||'|'||d.metric_name
     FROM datum d, recq r
     WHERE d.parent_name=r.metric_name AND d.hndl=r.hndl)
) SELECT hndl, lvl, total_time,
         SUBSTR(VARCHAR_FORMAT(COALESCE(cur_percent,0), '990.0'),1,6) AS "percent",
         SUBSTR(REPEAT('  ', lvl) || metric_name, 1, 60) AS metric_name
  FROM recq ORDER BY hndl, chain;
