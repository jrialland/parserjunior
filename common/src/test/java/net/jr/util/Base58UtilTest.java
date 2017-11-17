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

}
