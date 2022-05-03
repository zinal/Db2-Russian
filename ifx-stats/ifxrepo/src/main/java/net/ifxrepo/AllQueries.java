package net.ifxrepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Полный перечень всех обнаруженных запросов.
 * Группировка по запросу и сессии, затем упорядочено по времени снимка.
 * @author zinal
 */
public class AllQueries {

    // query hash -> records for a single query type
    public final Map<String, SessQueries> data = new HashMap<>();

    public void add(Single q) {
        if (q==null)
            return;
        SessQueries s = data.get(q.getKey());
        if (s==null) {
            s = new SessQueries(q.getSqlNorm());
            data.put(q.getKey(), s);
        }
        List<Single> i = s.data.get(q.getSess().getSessNo());
        if (i==null) {
            i = new ArrayList<>();
            s.data.put(q.getSess().getSessNo(), i);
        }
        i.add(q);
    }

    public static class SessQueries {

        public final String normSql;
        public final Map<String, List<Single>> data = new HashMap<>();

        public SessQueries(String normSql) {
            this.normSql = normSql;
        }

    }

    public static class Single extends KeyQuery {

        private final RecSess sess;
        private int seconds = 0;

        public Single(RecSess sess, String sql) {
            super(sql);
            this.sess = sess;
        }

        public Single(RecSess sess, KeyQuery x) {
            super(x);
            this.sess = sess;
        }

        public RecSess getSess() {
            return sess;
        }

        public int getSeconds() {
            return seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public void setSeconds(String seconds) {
            if (seconds.contains(":")) {
                String[] parts = seconds.split(":");
                int x = Integer.parseInt(parts[0]);
                x *= 60;
                x += Integer.parseInt(parts[1]);
                x *= 60;
                x += Integer.parseInt(parts[2]);
                this.seconds = x;
            } else {
                this.seconds = Integer.parseInt(seconds);
            }
        }
    }

}
