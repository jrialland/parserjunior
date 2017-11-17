package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class HexUtilTest {
    private Random random = new Random();

    @Test
    public void testEncode() {
        byte[] data = new byte[4096];
        random.nextBytes(data);

        String hex = HexUtil.bytesToHex(data);
        byte[] decoded = HexUtil.hexToBytes(hex);

        Assert.assertTrue(Arrays.equals(data, decoded));
    }
}
