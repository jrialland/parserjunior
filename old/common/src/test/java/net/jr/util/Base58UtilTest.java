package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class Base58UtilTest {


    private Random random = new Random();

    @Test
    public void testEncode() {
        byte[] data = new byte[4096];
        random.nextBytes(data);
        String s = Base58Util.encode(data);
        byte[] decoded = Base58Util.decode(s);
        Assert.assertTrue(Arrays.equals(data, decoded));
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals("", Base58Util.encode(new byte[]{}));
        byte[] empty = Base58Util.decode("");
        Assert.assertEquals(0, empty.length);
    }

    @Test
    public void testLeadingZeros() {
        String encoded;
        Assert.assertEquals("111D", encoded = Base58Util.encode(new byte[]{0, 0, 0, 12}));
        byte[] decoded = Base58Util.decode(encoded);
        Assert.assertEquals(0, decoded[0]);
        Assert.assertEquals(0, decoded[1]);
        Assert.assertEquals(0, decoded[2]);
        Assert.assertEquals(12, decoded[3]);
    }
}
