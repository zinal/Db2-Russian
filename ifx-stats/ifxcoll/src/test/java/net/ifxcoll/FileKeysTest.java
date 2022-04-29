package net.ifxcoll;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class FileKeysTest {

    @Test
    public void check() {
        final FileKeys fk = new FileKeys();
        for (long v=0; v<FileKeys.MAX_SIZE; ++v) {
            fk.add(v);
        }
        for (long v=0; v<FileKeys.MAX_SIZE; ++v) {
            boolean result = fk.isKnown(v);
            Assert.assertTrue("Should be a known value: " + v, result);
        }
        for (long v=FileKeys.MAX_SIZE; v<FileKeys.MAX_SIZE+10; ++v) {
            boolean result = fk.isKnown(v);
            Assert.assertFalse("Should not be a known value: " + v, result);
        }

        fk.add(FileKeys.MAX_SIZE);
        fk.add(FileKeys.MAX_SIZE+1);

        long v = FileKeys.MAX_SIZE;
        boolean result = fk.isKnown(v);
        Assert.assertTrue("Should be a known value: " + v, result);

        v = FileKeys.MAX_SIZE + 1;
        result = fk.isKnown(v);
        Assert.assertTrue("Should be a known value: " + v, result);

        v = 0;
        result = fk.isKnown(v);
        Assert.assertFalse("Should not be a known value: " + v, result);

        v = 1;
        result = fk.isKnown(v);
        Assert.assertFalse("Should not be a known value: " + v, result);
    }

}
