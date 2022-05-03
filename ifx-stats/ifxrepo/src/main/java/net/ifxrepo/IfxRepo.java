package net.ifxrepo;

import java.io.File;
import java.io.FileWriter;
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
            // Collect all SQL queries from snapshot data.
            final AllQueries aq = new AllQueries();
            for (RecSnap snap : snaps.values()) {
                for (RecSess sess : snap.getSessions().values()) {
                    final AllQueries.Single sql = extractSql(sess);
                    aq.add(sql);
                }
            }
            System.out.println("Unique queries found: " + aq.data.size());

            /*
            final List<String> qqs = new ArrayList<>();
            for (AllQueries.Grouped sq : aq.data.values()) {
                qqs.add(sq.normSql);
            }
            Collections.sort(qqs);
            try (FileWriter fw = new FileWriter("/tmp/zztop.txt")) {
                for (String s : qqs)
                    fw.append(s).append("\n");
            }
            */

            aq.compute();
            List<AllQueries.Grouped> sorted = aq.sort();
            try (FileWriter fw = new FileWriter("/tmp/zztop.txt")) {
                for (AllQueries.Grouped g : sorted) {
                    if (g.execCount < 2 && g.totalTime < 1)
                        continue;
                    String shortSql = g.normSql;
                    if (shortSql.length() > 50)
                        shortSql = shortSql.substring(0, 50);
                    fw
                            .append(String.valueOf(g.activeCount)).append('\t')
                            .append(String.valueOf(g.execCount)).append('\t')
                            .append(String.valueOf(g.totalTime)).append('\t')
                            .append(g.key).append('\t')
                            .append(shortSql).append('\n');
                }
                for (AllQueries.Grouped g : sorted) {
                    if (g.execCount < 2 && g.totalTime < 1)
                        continue;
                    fw.append("\n\n*** SQL ID: ").append(g.key).append('\n');
                    fw.append("Activations: ").append(String.valueOf(g.activeCount)).append('\n');
                    fw.append("Executions:  ").append(String.valueOf(g.execCount)).append('\n');
                    fw.append("Total time:  ").append(String.valueOf(g.totalTime)).append('\n');
                    fw.append("Normalized SQL text:").append('\n').append('\n');
                    fw.append(g.normSql).append('\n').append('\n');
                    fw.append("Example full SQL text:").append('\n').append('\n');
                    fw.append(g.getSampleSql()).append('\n').append('\n');
                    /*
                    fw.append("Execution details:").append('\n').append('\n');
                    final int maxLines = 200;
                    int printLines = 0;
                    for (List<AllQueries.Single> i : g.data.values()) {
                        for (AllQueries.Single s : i) {
                            fw.append('\t').append(s.getSess().getSessNo()).append('\t')
                                    .append(Long.toHexString(s.getSess().getSnap().getStamp()))
                                    .append('\n');
                            ++printLines;
                            if (printLines >= maxLines)
                                break;
                        }
                        if (printLines >= maxLines)
                            break;
                    }
                    */
                }
            }

        } catch(Exception ex) {
            throw new RuntimeException("", ex);
        } finally {
            for (ZipFile zf : zips) {
                try { zf.close(); } catch(IOException iox) {}
            }
        }
    }

    private boolean handleStamp(long stamp) {
        return stamp >= settings.getTimeMin() && stamp <= settings.getTimeMax();
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

        RecSnap snapPrev = null;
        for (RecSnap snap : snaps.values()) {
            snap.setSnapPrev(snapPrev);
            if (snapPrev!=null)
                snapPrev.setSnapNext(snap);
            snapPrev = snap;
        }
    }

    private AllQueries.Single extractSql(RecSess sess) throws Exception {
        final int
                STATE_NONE = 0,
                STATE_SQL_TEXT = 1,
                STATE_GETSTATE = 2;
        int state = STATE_NONE;
        StringBuilder accum = null;
        String knownWallTime = null;
        AllQueries.Single retval = null;
        boolean active = false;
        for (String line : ZipTools.readFile(sess.getSnap().getFile(), sess.getEntry())) {
            switch (state) {
                case STATE_NONE:
                    if (line.startsWith("Current SQL statement ")) {
                        accum = new StringBuilder();
                        int ix = line.indexOf(" in procedure ");
                        if (ix > 0) {
                            String start = line.substring(ix);
                            accum.append(start);
                        }
                        state = STATE_SQL_TEXT;
                    } else if (line.startsWith("tid ") && line.endsWith(" status")) {
                        state = STATE_GETSTATE; // для следующей строки
                    }
                    break;
                case STATE_GETSTATE:
                    state = STATE_NONE;
                    if (line.endsWith(" running-"))
                        active = true;
                    else
                        active = false;
                    break;
                case STATE_SQL_TEXT:
                    if (line.startsWith("  QUERY_TIMEOUT setting:")) {
                        /*noop*/
                    } else if (line.startsWith("  Clock time elapsed   : ")) {
                        knownWallTime = line.substring(25);
                    } else if (accum!=null) {
                        if (line.equals("Last parsed SQL statement :")
                                || line.equals("Host variables :")
                                || line.equals("Stored procedure stack :")) {
                            retval = new AllQueries.Single(sess, active, accum.toString());
                            if (knownWallTime != null)
                                retval.setSeconds(knownWallTime);
                            accum = null; knownWallTime = null;
                            state = STATE_NONE;
                        } else {
                            accum.append("\n").append(line);
                        }
                    }
                    break;
            }
        }
        if (accum != null && retval == null) {
            retval = new AllQueries.Single(sess, active, accum.toString());
            if (knownWallTime != null)
                retval.setSeconds(knownWallTime);
        }
        return retval;
    }

}
