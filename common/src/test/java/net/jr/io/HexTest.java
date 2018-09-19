package net.jr.io;

import net.jr.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class HexTest {

    @Test
    public void testToHex() {
        for(int i=0; i<256; i++) {
            String h = Hex.toHex(new byte[]{(byte)i});
            int e = Integer.parseInt(h, 16);
            Assert.assertEquals(i, e);
        }
        byte[] all = new byte[256];
        for(int i=0; i<256; i++) {
            all[i] = (byte)i;
        }
        String s = Hex.toHex(all);
        Assert.assertEquals(512, s.length());
        Assert.assertTrue(s.startsWith("0001020304"));
        Assert.assertTrue(s.endsWith("fafbfcfdfeff"));
    }

    @Test
    public void testFromHex() {
        for(int i=0; i<256; i++) {
            String s = StringUtil.lpad(Integer.toString(i, 16), "0",2);
            byte b = Hex.fromHex(s)[0];
            Assert.assertEquals((byte)i, b);
        }
        Assert.assertArrayEquals(new byte[]{(byte)0xde, (byte)0xad, (byte)0xbe, (byte)0xef}, Hex.fromHex("deadBeef"));
    }

}
