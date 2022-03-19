-- Dummy workload implementation

/*
DROP FUNCTION j_rand_str(INTEGER) @

CREATE FUNCTION j_rand_str(p_len INTEGER) RETURNS LVARCHAR
  DEFINE v_src VARCHAR(70);
  DEFINE v_dst VARCHAR(200);
  DEFINE v_pos INTEGER;
  DEFINE v_char INTEGER;
  LET v_src = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  LET v_pos = 1;
  LET v_dst = '';
  WHILE v_pos <= p_len AND v_pos <= 200
    LET v_char = MOD(ABS(dbms_random_random()), 62) + 1;
    LET v_dst = v_dst || SUBSTR(v_src, v_char, 1);
    LET v_pos = v_pos + 1;
  END WHILE;
  RETURN v_dst;
END FUNCTION; @

SELECT j_rand_str(10) FROM TABLE(SET{1}) x @
*/

CREATE PROCEDURE ifxdemo_work1(p_id INTEGER)
  DEFINE v_b_one, v_b_two VARCHAR(100);
  DEFINE v_c_one, v_c_two VARCHAR(100);
  DEFINE v_d DATETIME YEAR TO FRACTION;
  LET v_b_one = j_rand_str(25);
  LET v_b_two = j_rand_str(25);
  LET v_c_one = j_rand_str(25);
  LET v_c_two = j_rand_str(25);
  LET v_d = CURRENT YEAR TO FRACTION;
  UPDATE ifxdemo1 SET
    b = '1$' || v_b_one || '$' || v_b_two,
    c = '1$' || v_c_one || '$' || v_c_two,
    d = v_d,
    e = dbms_random_random(),
    f = sin(mod(dbms_random_random(), 100) / 10.3) * 1000.0
  WHERE a=p_id;
  UPDATE ifxdemo2 SET
    b = '2$' || v_b_two || '$' || v_b_one,
    c = '2$' || v_c_one || '$' || v_c_two,
    d = v_d,
    e = dbms_random_random(),
    f = cos(mod(dbms_random_random(), 100) / 10.3) * 1000.0
  WHERE a=p_id;
  UPDATE ifxdemo3 SET
    b = '3$' || v_b_one || '$' || v_b_two,
    c = '3$' || v_c_two || '$' || v_c_one,
    d = v_d,
    e = dbms_random_random(),
    f = sin(mod(dbms_random_random(), 10) / 6.9) * 1000.0
  WHERE a=p_id;
  UPDATE ifxdemo4 SET
    b = '4$' || v_b_two || '$' || v_b_one,
    c = '4$' || v_c_two || '$' || v_c_one,
    d = v_d,
    e = dbms_random_random(),
    f = cos(mod(dbms_random_random(), 10) / 6.9) * 1000.0
  WHERE a=p_id;
END PROCEDURE; @

EXECUTE PROCEDURE ifxdemo_work1(1000) @

SELECT * FROM ifxdemo1 WHERE a=1000;

(SELECT 'ifxdemo1' AS tabname, COUNT(*) AS dfcnt FROM ifxdemo1 WHERE f>-10000.0) UNION ALL
(SELECT 'ifxdemo2' AS tabname, COUNT(*) AS dfcnt FROM ifxdemo2 WHERE f>-10000.0) UNION ALL
(SELECT 'ifxdemo3' AS tabname, COUNT(*) AS dfcnt FROM ifxdemo3 WHERE f>-10000.0) UNION ALL
(SELECT 'ifxdemo4' AS tabname, COUNT(*) AS dfcnt FROM ifxdemo4 WHERE f>-10000.0);

-- End Of File
