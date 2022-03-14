package net.ifxrand;

import org.junit.Assert;
import org.junit.Test;

public class IfxRandTest {

    @Test
    public void test1() throws Exception {
        String v;
        v = IfxRand.rand_str(10);
        System.out.println("Random string 10: " + v);
        Assert.assertEquals(10, v.length());
        v = IfxRand.rand_str(20);
        System.out.println("Random string 20: " + v);
        Assert.assertEquals(20, v.length());
    }

}
