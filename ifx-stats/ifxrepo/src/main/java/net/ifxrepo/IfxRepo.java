package net.ifxrepo;

import java.io.File;

/**
 *
 * @author zinal
 */
public class IfxRepo implements Runnable {

    private final Settings settings;

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
    }

}
