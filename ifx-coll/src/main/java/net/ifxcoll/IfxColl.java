package net.ifxcoll;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author zinal
 */
public class IfxColl implements AutoCloseable, Runnable {

    private final long delay;

    private final FileSaver saver;
    private final ExecutorService executor;

    private long stamp = 0L;
    private int records = 0;

    public IfxColl(long delay, File file) throws Exception {
        this.delay = delay;
        this.saver = new FileSaver(delay, file);
        this.executor = Executors.newCachedThreadPool();
    }

    public static void main(String[] args) {
        long delay = 10000;
        File file = null;
        if (args.length > 0) {
            int v = Integer.parseInt(args[0]);
            if (v > 0)
                delay = v * 1000L;
            if (args.length > 1)
                file = new File(args[1]);
        }
        if (file==null)
            file = new File("ifxcoll.zip");
        try (IfxColl ic = new IfxColl(delay, file)) {
            System.err.println("ifx-coll 1.0 started, delay " + delay + " msec., target file "
                    + file.getAbsolutePath());
            ic.run();
            System.err.println("Collection stopped.");
        } catch(Exception ex) {
            System.err.println("FATAL: Execution error, terminating");
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void close() {
        saver.stop(records);
        records = 0;
    }

    @Override
    public void run() {
        Signaler.setup();
        saver.start();
        while (! Signaler.isShutdown()) {
            stamp = System.currentTimeMillis();
            action();
            ++records;
            final long dest = stamp + delay;
            while (true) {
                if (Signaler.isShutdown())
                    break;
                try { Thread.sleep(100L); } catch(InterruptedException ix) {}
                final long tv = System.currentTimeMillis();
                if (tv >= dest)
                    break;
            }
        }
    }

    public void action() {
        
    }

}
