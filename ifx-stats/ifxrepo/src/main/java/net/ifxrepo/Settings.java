package net.ifxrepo;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author zinal
 */
public class Settings {

    public static final String PROP_FILE = "file.";
    public static final String PROP_TIME_MIN = "time.min";
    public static final String PROP_TIME_MAX = "time.max";
    public static final String PROP_MODE = "mode";

    private final List<File> files = new ArrayList<>();
    private long timeMin = 0L;
    private long timeMax = Long.MAX_VALUE;
    private Mode mode = null;

    public Settings() {
    }

    public Settings(Properties props) {
        this.mode = parseMode(props.getProperty(PROP_MODE, ""));
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String v = props.getProperty(PROP_TIME_MIN);
        if (v==null) {
            this.timeMin = 0L;
        } else {
            this.timeMin = parseDate(v, dtf);
        }
        v = props.getProperty(PROP_TIME_MAX);
        if (v==null) {
            this.timeMax = Long.MAX_VALUE;
        } else {
            this.timeMax = parseDate(v, dtf);
        }
        int fileNum = 0;
        while (true) {
            v = props.getProperty(PROP_FILE + String.valueOf(fileNum));
            if (v==null)
                break;
            files.add(new File(v));
            ++fileNum;
        }
    }

    public Settings(File file) {
        this(load(file));
    }

    public List<File> getFiles() {
        return files;
    }

    public long getTimeMin() {
        return timeMin;
    }

    public void setTimeMin(long timeMin) {
        this.timeMin = timeMin;
    }

    public long getTimeMax() {
        return timeMax;
    }

    public void setTimeMax(long timeMax) {
        this.timeMax = timeMax;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public static Properties load(File file) {
        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.loadFromXML(fis);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read file " + file, ex);
        }
        return props;
    }

    public static Mode parseMode(String v) {
        v = (v==null) ? "" : v.trim();
        if (v.length()==0)
            return Mode.TopSql;
        for (Mode m : Mode.values()) {
            if (m.name().equalsIgnoreCase(v))
                return m;
        }
        throw new IllegalArgumentException("Incorrect value for property ["
                + PROP_MODE + "]: " + v);
    }

    public static long parseDate(String v, DateTimeFormatter dtf) {
        return  java.util.Date.from(LocalDateTime.parse(v, dtf)
            .atZone(ZoneId.systemDefault())
            .toInstant()).getTime();
    }

    public static enum Mode {
        TopSql
    }

}
