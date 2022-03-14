-- Dummy workload implementation

CREATE OR REPLACE PROCEDURE ifxdemo_work1(p_id INTEGER)
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
    d = v_d
  WHERE a=p_id;
  UPDATE ifxdemo2 SET
    b = '2$' || v_b_two || '$' || v_b_one,
    c = '2$' || v_c_one || '$' || v_c_two,
    d = v_d
  WHERE a=p_id;
  UPDATE ifxdemo3 SET
    b = '3$' || v_b_one || '$' || v_b_two,
    c = '3$' || v_c_two || '$' || v_c_one,
    d = v_d
  WHERE a=p_id;
  UPDATE ifxdemo4 SET
    b = '4$' || v_b_two || '$' || v_b_one,
    c = '4$' || v_c_two || '$' || v_c_one,
    d = v_d
  WHERE a=p_id;
END PROCEDURE; @

EXECUTE PROCEDURE ifxdemo_work1(1000) @

SELECT * FROM ifxdemo1 WHERE SUBSTR(b,1,2)='1$';

-- End Of File
