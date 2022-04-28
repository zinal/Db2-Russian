package net.ifxcoll;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author zinal
 */
public class IfxColl implements AutoCloseable, Runnable {

    private final long delay;
    private final File file;
    private final long start;

    private final ZipOutputStream zos;

    private long stamp = 0L;
    private long counter = 0;

    public IfxColl(long delay, File file) throws Exception {
        this.delay = delay;
        this.file = file;
        this.start = System.currentTimeMillis();
        this.zos = new ZipOutputStream(new FileOutputStream(file));
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
        writeMetadata();
        try {
            zos.close();
        } catch(Exception ex) {
            System.err.println("FATAL: Failed to close the output file, data is probably damaged.");
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        Signaler.setup();
        while (! Signaler.isShutdown()) {
            stamp = System.currentTimeMillis();
            action();
            ++counter;
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

    private void writeMetadata() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.mm.dd HH:mm:ss");
        final StringBuilder sb = new StringBuilder();
        sb.append("ifx-coll 1.0 data").append("\r\n");
        sb.append("Delay:  ").append(String.valueOf(delay)).append(" msec.\r\n");
        sb.append("Items:  ").append(String.valueOf(counter)).append("\r\n");
        sb.append("Start:  ").append(sdf.format(new Date(start))).append("\r\n");
        sb.append("Finish: ").append(sdf.format(new Date(System.currentTimeMillis()))).append("\r\n");
        final byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        try {
            zos.putNextEntry(new ZipEntry("info.txt"));
            zos.write(data);
            zos.closeEntry();
        } catch(Exception ex) {
            System.err.println("FATAL: Failed to save the metadata.");
            ex.printStackTrace(System.err);
        }
    }

}
