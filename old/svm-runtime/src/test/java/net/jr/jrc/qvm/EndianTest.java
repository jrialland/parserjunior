package net.jr.jrc.qvm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class EndianTest {

    @Test
    public void test4Le() {
        byte[] tmp = new byte[4];
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int a = r.nextInt();
            Endian.write4Le(a, tmp);
            Assert.assertEquals(a, Endian.read4Le(tmp));
        }
    }

    @Test
    public void test4ULe() {
        byte[] tmp = new byte[4];
        Endian.write4ULe(27, tmp);
        long l = Endian.read4ULe(tmp);
        Assert.assertEquals(27, l);
    }

    @Test
    public void test4ULe_bigVal() {
        byte[] tmp = new byte[4];
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            long initial = Math.abs(r.nextLong() % 2147483670L);
            Endian.write4ULe(initial, tmp);
            long l = Endian.read4ULe(tmp);
            Assert.assertEquals(initial, l);
        }
    }

    @Test
    public void test4Lef() {
        byte[] tmp = new byte[4];
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            float f = r.nextFloat();
            Endian.write4Lef(f, tmp);
            Assert.assertEquals(f, Endian.read4Lef(tmp), 0.0);
        }
    }
}
