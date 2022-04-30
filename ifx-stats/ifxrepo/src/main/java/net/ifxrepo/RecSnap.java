package net.ifxrepo;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author zinal
 */
public class RecSnap {

    private final ZipFile file;
    private final long stamp;
    private final Map<String, RecSess> sessions;

    private ZipEntry nttEntry;

    public RecSnap(ZipFile file, long stamp) {
        this.file = file;
        this.stamp = stamp;
        this.sessions = new HashMap<>();
    }

    public RecSnap(ZipFile file, String stamp) {
        this(file, Long.parseLong(stamp, 16));
    }

    public ZipFile getFile() {
        return file;
    }

    public long getStamp() {
        return stamp;
    }

    public Map<String, RecSess> getSessions() {
        return sessions;
    }

    public ZipEntry getNttEntry() {
        return nttEntry;
    }

    public void setNttEntry(ZipEntry nttEntry) {
        this.nttEntry = nttEntry;
    }

}
