package net.ifxcoll;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class SqlCollRxTest {

    @Test
    public void check() {
        final Pattern pattern = Pattern.compile(SqlColl.RX);
        String v;
        Matcher m;

        v = "101        -              mydb1              CR  Not Wait   0    0    9.28  Off";
        m = pattern.matcher(v);
        Assert.assertTrue("Pattern should match", m.matches());
        Assert.assertEquals("101", m.group(1));

        v = "33                        sysadmin           DR  Wait 5     0    0    -     Off";
        m = pattern.matcher(v);
        Assert.assertTrue("Pattern should match", m.matches());
        Assert.assertEquals("33", m.group(1));
    }

}
