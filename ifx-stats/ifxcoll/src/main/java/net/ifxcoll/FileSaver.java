package net.ifxcoll;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author zinal
 */
public class FileSaver implements Runnable, FileHandler {

    private static final byte[] EOL = "\n".getBytes(StandardCharsets.UTF_8);

    private final long delay;
    private final File dirname;
    private final String basename;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final long zipLifeTime;

    private long zosOpenStamp = 0L;
    private ZipOutputStream zos = null;

    private volatile boolean shutdown = false;
    private volatile Thread thread = null;

    private final ConcurrentLinkedQueue<FileData> queue;
    private final FileKeys fileKeys = new FileKeys();

    private long bytesWritten = 0L;
    private long filesWritten = 0L;
    private int timesKnown = 0;

    public FileSaver(long delay, String dirname, String basename) throws Exception {
        this.delay = delay;
        this.dirname = new File(dirname);
        this.basename = basename;
        this.queue = new ConcurrentLinkedQueue<>();
        String prop = System.getProperty("ifxcoll.ziplt");
        if (prop!=null && prop.length() > 0) {
            this.zipLifeTime = 1000L * Long.parseLong(prop);
        } else {
            this.zipLifeTime = 3600000L;
        }
    }

    public void start() {
        if (thread==null) {
            thread = new Thread(this, "FileSaver");
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop() {
        if (thread==null)
            return;
        this.shutdown = true;
        while (true) {
            try {
                thread.join();
                thread = null;
                return;
            } catch(InterruptedException ix) {}
        }
    }

    @Override
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
        closeZip();
    }

    private void writeEntry(FileData fd) throws Exception {
        if (zos!=null && (fd.stamp - zosOpenStamp) > zipLifeTime) {
            closeZip();
        }
        if (zos==null) {
            openZip(fd.stamp);
        }
        final String dirName = Long.toHexString(fd.stamp) + "/";
        if (! fileKeys.isKnown(fd.stamp)) {
            zos.putNextEntry(new ZipEntry(dirName));
            fileKeys.add(fd.stamp);
            ++timesKnown;
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
        sb.append("Start:   ").append(sdf.format(new Date(zosOpenStamp))).append("\r\n");
        sb.append("Finish:  ").append(sdf.format(new Date(System.currentTimeMillis()))).append("\r\n");
        sb.append("Delay:   ").append(String.valueOf(delay)).append(" msec.\r\n");
        sb.append("Records: ").append(String.valueOf(timesKnown)).append("\r\n");
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

    private void closeZip() {
        if (zos != null) {
            writeMetadata();
            try {
                zos.close();
            } catch(Exception ex) {
                System.err.println("FATAL: Failed to close the output file, data is probably damaged.");
                ex.printStackTrace(System.err);
            }
            zos = null;
        }
        zosOpenStamp = 0L;
        timesKnown = 0;
        bytesWritten = 0L;
        filesWritten = 0L;
        fileKeys.clear();
    }

    private void openZip(long stamp) throws Exception {
        if (zos!=null)
            return;
        String fname = basename + "_" + sdf.format(new Date(stamp)) + ".zip";
        File f = new File(dirname, fname);
        ZipOutputStream ret = new ZipOutputStream(new FileOutputStream(f));
        ret.setLevel(Deflater.BEST_COMPRESSION);
        zos = ret;
        zosOpenStamp = stamp;
    }

}
