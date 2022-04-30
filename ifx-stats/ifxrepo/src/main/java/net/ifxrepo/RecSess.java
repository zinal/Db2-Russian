package net.ifxrepo;

import java.util.zip.ZipEntry;

/**
 *
 * @author zinal
 */
public class RecSess {

    private final RecSnap snap;
    private final ZipEntry entry;
    private final String sessNo;

    public RecSess(RecSnap snap, ZipEntry entry, String sessNo) {
        this.snap = snap;
        this.entry = entry;
        this.sessNo = sessNo;
    }

    public RecSnap getSnap() {
        return snap;
    }

    public ZipEntry getEntry() {
        return entry;
    }

    public String getSessNo() {
        return sessNo;
    }

}
