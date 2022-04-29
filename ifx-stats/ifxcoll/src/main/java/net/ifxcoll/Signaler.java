package net.ifxcoll;

import java.util.concurrent.atomic.AtomicInteger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 *
 * @author zinal
 */
public class Signaler {

    private static volatile boolean SETUP = true;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static void setup() {
        if (!SETUP)
            return;
        COUNTER.getAndSet(0);
        Signal.handle(new Signal("INT"), new SignalHandler() {
            @Override
            public void handle(Signal sig) {
                COUNTER.incrementAndGet();
            }
        });
        SETUP = false;
    }

    public static void reset() {
        COUNTER.getAndSet(0);
    }

    public static boolean isShutdown() {
        return COUNTER.addAndGet(0) > 0;
    }

}
