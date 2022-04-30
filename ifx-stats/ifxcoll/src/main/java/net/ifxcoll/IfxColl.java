package net.ifxcoll;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author zinal
 */
public class IfxColl implements AutoCloseable, Runnable {

    private final long delay;

    private final ExecutorService executor;
    private final FileSaver saver;
    private final SqlColl sqlColl;

    private long stamp = 0L;

    public IfxColl(long delay, String dirname, String basename, int poolSize) throws Exception {
        this.delay = delay;
        this.executor = Executors.newFixedThreadPool((poolSize > 0) ? poolSize : 10);
        this.saver = new FileSaver(delay, dirname, basename);
        this.sqlColl = new SqlColl(saver, executor);
    }

    public static void main(String[] args) {
        long delay = 10000;
        String dirname = null;
        String basename = null;
        int poolSize = -1;
        try {
            if (args.length > 0) {
                int v = Integer.parseInt(args[0]);
                if (v > 0)
                    delay = v * 1000L;
                if (args.length > 1)
                    basename = args[1];
                if (args.length > 2)
                    dirname = args[2];
            }
            String vs = System.getProperty("ifxcoll.poolSize");
            if (vs!=null && vs.length() > 0)
                poolSize = Integer.parseInt(vs);
            if (dirname==null || dirname.length()==0)
                dirname = ".";
            if (basename==null || basename.length()==0)
                basename = "ifxcoll";
            dirname = Paths.get(dirname).toAbsolutePath().normalize().toString();
            try (IfxColl ic = new IfxColl(delay, dirname, basename, poolSize)) {
                System.err.println("ifx-coll 1.1 started, delay " + delay + " msec., output to "
                        + dirname + " : " + basename);
                ic.run();
                System.err.println("ifx-coll stopped.");
            }
        } catch(Exception ex) {
            System.err.println("FATAL: Execution error, terminating");
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        saver.stop();
    }

    @Override
    public void run() {
        Signaler.setup();
        saver.start();
        while (! Signaler.isShutdown()) {
            stamp = System.currentTimeMillis();
            action();
            long tv = System.currentTimeMillis();
            long dest = stamp + delay;
            while (dest < tv)
                dest += delay;
            while (true) {
                if (Signaler.isShutdown())
                    break;
                try { Thread.sleep(200L); } catch(InterruptedException ix) {}
                tv = System.currentTimeMillis();
                if (tv >= dest)
                    break;
            }
        }
    }

    public void action() {
        executor.submit(new CmdRun(saver, stamp, "aaa", "onstat"));
        executor.submit(new CmdRun(saver, stamp, "ntt", "onstat", "-g", "ntt"));
        executor.submit(new CmdRun(saver, stamp, "ses", "onstat", "-g", "ses"));
        executor.submit(new CmdRun(saver, stamp, "buf", "onstat", "-g", "buf"));
        executor.submit(new CmdRun(sqlColl, stamp, "sql", "onstat", "-g", "sql"));
    }

}
