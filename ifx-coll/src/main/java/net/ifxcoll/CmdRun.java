package net.ifxcoll;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author zinal
 */
public class CmdRun implements Runnable {

    private final FileSaver saver;
    private final FileData fd = new FileData();
    private final List<String> command;

    public CmdRun(FileSaver saver, long stamp, String code, List<String> command) {
        this.saver = saver;
        this.command = command;
        fd.stamp = stamp;
        fd.code = code;
        fd.data = new ArrayList<>();
    }

    public CmdRun(FileSaver saver, long stamp, String code, String... command) {
        this(saver, stamp, code, Arrays.asList(command));
    }

    @Override
    public void run() {
        try {
            final Process proc = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = br.readLine())!=null) {
                    fd.data.add(line);
                }
            }
            proc.waitFor();
            saver.add(fd);
        } catch(Exception ex) {
            System.err.println("FATAL: cannot run " + command);
            ex.printStackTrace(System.err);
        }
    }

}
