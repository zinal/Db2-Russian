package net.ifxcoll;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author zinal
 */
public class FileSaver implements Runnable {

    private static final byte[] EOL = "\n".getBytes(StandardCharsets.UTF_8);

    private final long delay;
    private final long start;
    private final ZipOutputStream zos;

    private volatile boolean shutdown = false;
    private volatile int records = 0;

    private volatile Thread thread = null;

    private final ConcurrentLinkedQueue<FileData> queue;
    private final FileKeys fileKeys = new FileKeys();
    private long bytesWritten = 0L;
    private long filesWritten = 0L;

    public FileSaver(long delay, File file) throws Exception {
        this.delay = delay;
        this.start = System.currentTimeMillis();
        this.zos = new ZipOutputStream(new FileOutputStream(file));
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void start() {
        if (thread==null) {
            thread = new Thread(this, "FileSaver");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop(int records) {
        if (thread==null)
            return;
        this.shutdown = true;
        this.records = records;
        while (true) {
            try {
                thread.join();
                thread = null;
                return;
            } catch(InterruptedException ix) {}
        }
    }

    public void add(FileData fd) {
        if (thread==null)
            throw new IllegalStateException();
        queue.add(fd);
    }

    @Override
    public void run() {
        while (true) {
            final FileData head = queue.poll();
            if (head!=null) {
                try {
                    writeEntry(head);
                } catch(Exception ex) {
                    System.err.println("FATAL: cannot write to output file");
                    ex.printStackTrace(System.err);
                    break;
                }
            } else {
                if (shutdown)
                    break;
                try { Thread.sleep(100L); } catch(InterruptedException ix) {}
            }
        }
        writeMetadata();
        try {
            zos.close();
        } catch(Exception ex) {
            System.err.println("FATAL: Failed to close the output file, data is probably damaged.");
            ex.printStackTrace(System.err);
        }
    }

    private void writeEntry(FileData fd) throws Exception {
        final String dirName = Long.toHexString(fd.stamp) + "/";
        if (! fileKeys.isKnown(fd.stamp)) {
            zos.putNextEntry(new ZipEntry(dirName));
            fileKeys.add(fd.stamp);
        }
        final String fileName = dirName + fd.code + ".txt";
        zos.putNextEntry(new ZipEntry(fileName));
        for (String v : fd.data) {
            byte[] data = v.getBytes(StandardCharsets.UTF_8);
            zos.write(data);
            zos.write(EOL);
            bytesWritten += data.length + EOL.length;
        }
        zos.closeEntry();
        ++filesWritten;
    }

    private void writeMetadata() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.mm.dd HH:mm:ss");
        final StringBuilder sb = new StringBuilder();
        sb.append("ifx-coll 1.0 data").append("\r\n");
        sb.append("Start:   ").append(sdf.format(new Date(start))).append("\r\n");
        sb.append("Finish:  ").append(sdf.format(new Date(System.currentTimeMillis()))).append("\r\n");
        sb.append("Delay:   ").append(String.valueOf(delay)).append(" msec.\r\n");
        sb.append("Records: ").append(String.valueOf(records)).append("\r\n");
        sb.append("Entries: ").append(String.valueOf(filesWritten)).append("\r\n");
        sb.append("Bytes:   ").append(String.valueOf(bytesWritten)).append("\r\n");
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
