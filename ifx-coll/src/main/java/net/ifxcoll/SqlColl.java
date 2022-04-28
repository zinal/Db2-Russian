package net.ifxcoll;

import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zinal
 */
public class SqlColl implements FileHandler {

    public static final String RX = "^([1-9][0-9]*) .*";

    private final FileHandler handler;
    private final ExecutorService executor;
    private final Pattern pattern;

    public SqlColl(FileHandler handler, ExecutorService executor) {
        this.handler = handler;
        this.executor = executor;
        this.pattern = Pattern.compile(RX);
    }

    @Override
    public void add(FileData fd) {
        // "onstat -g sql" output expected
        // first we send this output to be stored
        handler.add(fd);
        // next for each session we grab its current SQL data
        for (String v : fd.data) {
            if (v.trim().length()==0)
                continue;
            final Matcher m = pattern.matcher(v);
            if (m.matches()) {
                final String x = m.group(1);
                executor.submit(new CmdRun(handler, fd.stamp,
                        "ses_" + x, "onstat", "-g", "ses", x));
            }
        }
    }

}
