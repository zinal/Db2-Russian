package net.ifxrepo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author zinal
 */
public class IfxRepo implements Runnable {

    private final Settings settings;

    private final TreeMap<Long, RecSnap> snaps = new TreeMap<>();

    public IfxRepo(Settings settings) {
        this.settings = settings;
    }

    public static void main(String[] args) {
        String jobFile = "ifxrepo-job.xml";
        if (args.length > 0) {
            jobFile = args[0];
        }
        try {
            new IfxRepo(new Settings(new File(jobFile))) . run();
        } catch(Exception ex) {
            System.err.println("FATAL: unexpected error " + ex);
            ex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("ifx-repo 1.0 " + settings.getMode());
        final List<ZipFile> zips = new ArrayList<>();
        try {
            // Fetch the basic session data from each of the files
            for (File f : settings.getFiles()) {
                System.out.println("Reading archive directory: " + f);
                final ZipFile zf = new ZipFile(f);
                zips.add(zf);
                grabSnapshots(zf);
            }
            // Print the overall statistics
            System.out.println("Snapshots to be processed: " + snaps.size());
        } catch(Exception ex) {
            throw new RuntimeException("", ex);
        } finally {
            for (ZipFile zf : zips) {
                try { zf.close(); } catch(IOException iox) {}
            }
        }
    }

    private void grabSnapshots(ZipFile zf) throws Exception {
        int entryCounter = 0;
        long progressUpdate = System.currentTimeMillis();
        final Pattern snapPatt = Pattern.compile("^[0-9a-fA-F]{10,15}$");
        final Pattern sessPatt = Pattern.compile("^([0-9a-fA-F]{10,15})/ses_([0-9]{1,30})[.]txt$");
        final Enumeration<? extends ZipEntry> elements = zf.entries();
        while (elements.hasMoreElements()) {
            ZipEntry ze = elements.nextElement();
            String name = ze.getName();
            if (name.endsWith("/"))
                name = name.substring(0, name.length()-1);
            if (ze.isDirectory()) {
                if (snapPatt.matcher(name).matches()) {
                    final long stamp = Long.parseLong(name, 16);
                    if (handleStamp(stamp)) {
                        if (snaps.containsKey(stamp)) {
                            System.err.println("WARNING: duplicate stamp " + name);
                        } else {
                            snaps.put(stamp, new RecSnap(zf, stamp));
                        }
                    }
                }
            } else if (name.endsWith("/ntt.txt")) {
                final String snapRef = name.substring(0, name.length()-8);
                final long stamp = Long.parseLong(snapRef, 16);
                if (handleStamp(stamp)) {
                    final RecSnap snap = snaps.get(stamp);
                    if (snap==null) {
                        System.err.println("WARNING: bad ntt " + name);
                    } else {
                        snap.setNttEntry(ze);
                    }
                }
            } else {
                final Matcher m = sessPatt.matcher(name);
                if (m.matches()) {
                    final String snapRef = m.group(1);
                    final String sessNo = m.group(2);
                    final long stamp = Long.parseLong(snapRef, 16);
                    if (handleStamp(stamp)) {
                        final RecSnap snap = snaps.get(stamp);
                        if (snap==null) {
                            System.err.println("WARNING: bad sess " + name);
                        } else {
                            snap.getSessions().put(sessNo, new RecSess(snap, ze, sessNo));
                        }
                    }
                }
            }
            if (++entryCounter % 100 == 0) {
                final long tv = System.currentTimeMillis();
                if (tv - progressUpdate > 5000L) {
                    System.out.println("\t" + entryCounter + " entries...");
                }
            }
        }
        System.out.println("\ttotal " + entryCounter + " entries.");
    }

    private boolean handleStamp(long stamp) {
        return stamp >= settings.getTimeMin() && stamp <= settings.getTimeMax();
    }

}
