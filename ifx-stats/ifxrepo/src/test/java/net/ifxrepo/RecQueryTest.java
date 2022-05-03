package net.ifxrepo;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class RecQueryTest {

    @Test
    public void normalizeTest1() {
        String in, out;

        in = "SELECT x.a, y.b, z.c FROM x, y, z   WHERE x.a=y.a AND x.b=y.b AND y.d=60\n "
                + "  AND z.f='Bamboleyo!'";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("SELECT x.a, y.b, z.c FROM x, y, z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=? AND z.f='?'", out);
    }

    @Test
    public void normalizeTest2() {
        String in, out;

        in = "SELECT x.a, y.b, z.c   FROM x, y, \"ah'govno\" z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=60\n "
                + "AND z.f='Bam''boleyo!'''";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("SELECT x.a, y.b, z.c FROM x, y, \"ah'govno\" z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=? AND z.f='?'", out);
    }

    @Test
    public void normalizeTest3() {
        String in, out;

        in = "unbalanced   ' \"***\"";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("unbalanced '?'", out);
    }

    @Test
    public void normalizedDefect1() {
        String in, out;

        in = "SELECT 'found' FROM u_group_user "
                + "WHERE user_group_code = ? AND user_code =\n    ?";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("SELECT '?' FROM u_group_user "
                + "WHERE user_group_code = ? AND user_code = ?", out);
    }

}
