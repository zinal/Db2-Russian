package net.ifxrepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public final Map<String, Grouped> data = new HashMap<>();

    public void add(Single q) {
        if (q==null)
            return;
        Grouped s = data.get(q.getKey());
        if (s==null) {
            s = new Grouped(q.getKey(), q.getSqlNorm());
            data.put(q.getKey(), s);
        }
        List<Single> i = s.data.get(q.getSess().getSessNo());
        if (i==null) {
            i = new ArrayList<>();
            s.data.put(q.getSess().getSessNo(), i);
        }
        i.add(q);
    }

    /**
     * Расчёт статистик времени и количества выполнения.
     */
    public void compute() {
        for (Grouped sq : data.values())
            sq.compute();
    }

    public List<Grouped> sort() {
        List<Grouped> retval = new ArrayList<>(data.values());
        Collections.sort(retval, (Grouped o1, Grouped o2) -> {
            if (o1==o2)
                return 0;
            if (o1.totalTime != o2.totalTime) {
                return (o1.totalTime > o2.totalTime) ? 1 : -1;
            }
            if (o1.execCount != o2.execCount) {
                return (o1.execCount > o2.execCount) ? 1 : -1;
            }
            return o1.key.compareTo(o2.key);
        });
        return retval;
    }

    public static class Grouped {

        public final String key;
        public final String normSql;
        public final Map<String, List<Single>> data = new HashMap<>();

        public int execCount = 0;
        public int totalTime = 0;

        public Grouped(String key, String normSql) {
            this.key = key;
            this.normSql = normSql;
        }

        private void compute() {
            execCount = 0;
            totalTime = 0;
            for (List<Single> items : data.values()) {
                Single prev = null;
                for (Single cur : items) {
                    if (prev!=null) {
                        boolean handlePrev = false;
                        if (prev.getSqlSample().equals(cur.getSqlSample())) {
                            // Одинаковый SQL
                            int diff = cur.getSeconds() - prev.getSeconds();
                            long stamp = cur.getSess().getSnap().getStamp()
                                    - prev.getSess().getSnap().getStamp();
                            if (diff < stamp / 1000L) {
                                // Разные запуски последовательно
                                handlePrev = true;
                            }
                        } else {
                            // Разный SQL
                            handlePrev = true;
                        }
                        if (handlePrev) {
                            execCount += 1;
                            totalTime += prev.getSeconds();
                        }
                    }
                    prev = cur;
                }
                if (prev!=null) {
                    // Последний всегда надо обрабатывать
                    execCount += 1;
                    totalTime += prev.getSeconds();
                }
            }
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
