package net.ifxcoll;

import java.util.TreeSet;

/**
 *
 * @author zinal
 */
public class FileKeys {

    public static final long MAX_SIZE = 100L;

    private final TreeSet<Long> knownKeys = new TreeSet<>();

    public boolean isKnown(long v) {
        return knownKeys.contains(v);
    }

    public void add(long v) {
        knownKeys.add(v);
        if (knownKeys.size() > MAX_SIZE) {
            knownKeys.remove(knownKeys.iterator().next());
        }
    }

}
