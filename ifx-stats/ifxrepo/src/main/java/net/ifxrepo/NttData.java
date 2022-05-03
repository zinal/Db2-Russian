package net.ifxrepo;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author zinal
 */
public class NttData {

    public static NttData load(ZipFile zf, ZipEntry ze) {
        final NttData ntt = new NttData();
        for (String line : ZipTools.readFile(zf, ze)) {

        }
        return ntt;
    }
/*
    public static final class Entry {
        final long sid;
        final String openTime;
        final String readTime;
        final String writeTime;

        public Entry(String v) {

        }
    }
*/
}
