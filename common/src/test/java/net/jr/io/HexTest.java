package net.jr.io;

import net.jr.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @Test
    public void testFromHex2() throws IOException  {
        String s = "44147212030000000800000010000000200000008b0d000002082e1600000100dcffffffd0030000c3fcffff1d00000048656c6c6f20776f726c64";
        InputStream is = Hex.hexInputStream(new ByteArrayInputStream(s.getBytes()));
        byte[] buf = new byte[4];
        Assert.assertEquals(4, is.read(buf));
        Assert.assertEquals(0x44, buf[0]);
        Assert.assertEquals(0x14, buf[1]);
        Assert.assertEquals(0x72, buf[2]);
        Assert.assertEquals(0x12, buf[3]);

    }
}
