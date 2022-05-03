package net.ifxrepo;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class RecQueryTest {

    @Test
    public void normalizeTest() {
        String in, out;

        in = "SELECT x.a, y.b, z.c FROM x, y, z   WHERE x.a=y.a AND x.b=y.b AND y.d=60\n "
                + "  AND z.f='Bamboleyo!'";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("SELECT x.a, y.b, z.c FROM x, y, z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=? AND z.f='?'", out);

        in = "SELECT x.a, y.b, z.c   FROM x, y, \"ah'govno\" z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=60\n "
                + "AND z.f='Bam''boleyo!'''";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("SELECT x.a, y.b, z.c FROM x, y, \"ah'govno\" z "
                + "WHERE x.a=y.a AND x.b=y.b AND y.d=? AND z.f='?'", out);

        in = "unbalanced   ' \"***\"";
        out = KeyQuery.normalize(in);
        Assert.assertEquals("unbalanced '?'", out);
    }

}
