package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author jrialland
 */
public class RleTest {

    @Test
    public void testEncodeEmpty() {
        Assert.assertArrayEquals(new int[]{}, Rle.encode(new int[]{}));
    }

    @Test
    public void testDecodeEmpty() {
        Assert.assertArrayEquals(new int[]{}, Rle.decode(new int[]{}));
    }


    @Test
    public void testEncodeSize1() {
        Assert.assertArrayEquals(new int[]{8, 1}, Rle.encode(new int[]{8}));
    }

    @Test
    public void testEncode() {
        Assert.assertArrayEquals(new int[]{8, 2, 5, 1, 2, 1}, Rle.encode(new int[]{8, 8, 5, 2}));
    }

    @Test
    public void testDecode() {
        Assert.assertArrayEquals(new int[]{8, 8, 5, 2}, Rle.decode(new int[]{8, 2, 5, 1, 2, 1}));
    }

    @Test
    public void testEncodeRepeat() {
        Assert.assertArrayEquals(new int[]{1, 7}, Rle.encode(new int[]{1, 1, 1, 1, 1, 1, 1}));
    }

    @Test
    public void testEncodeDecode() {
        Random random = new Random();
        int[] array = new int[1000];
        for (int i = 0; i < 1000; i++) {
            array[i] = random.nextInt();
        }
        int[] decoded = Rle.decode(Rle.encode(array));
        Assert.assertArrayEquals(array, decoded);
    }

}
