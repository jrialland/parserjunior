package net.jr.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HexdumpOutputStreamTest {


    private String dump(String s, long offset, boolean ascii) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HexdumpOutputStream h = new HexdumpOutputStream(out, offset, ascii);
        IOUtil.copy(new ByteArrayInputStream(s.getBytes()), h);
        h.close();
        return out.toString();
    }

    @Test
    public void test() throws Exception {
        String d = dump("Hello", 0, true);
        Assert.assertEquals("0x00000000  48 65 6c 6c 6f __ __ __  __ __ __ __ __ __ __ __  |Hello...........|\n", d);
    }

    @Test
    public void testNoAscii() throws Exception {
        String d = dump("Hello", 0, false);
        Assert.assertEquals("0x00000000  48 65 6c 6c 6f __ __ __  __ __ __ __ __ __ __ __\n", d);
    }

    @Test
    public void testOffset() throws Exception {
        String d = dump("Hello", 30, true);
        String expected = "0x00000010  __ __ __ __ __ __ __ __  __ __ __ __ __ __ 48 65  |..............He|\n"
                + "0x00000020  6c 6c 6f __ __ __ __ __  __ __ __ __ __ __ __ __  |llo.............|\n";
        Assert.assertEquals(expected, d);
    }

    @Test
    public void testAllChars() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HexdumpOutputStream h = new HexdumpOutputStream(out);
        for (int i = 0; i < 256; i++) {
            h.write(i);
        }
        h.close();
        System.out.println(out.toString());
    }

    @Test
    public void testReadFully() throws Exception {
        String dump = HexdumpOutputStream.readFully(new ByteArrayInputStream("this is a test".getBytes()));
    }

    @Test
    public void testAsHexDump() {
        String dump = HexdumpOutputStream.asHexDump(new byte[]{(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef});
        Assert.assertEquals("0x00000000  de ad be ef __ __ __ __  __ __ __ __ __ __ __ __  |................|\n", dump);
        Assert.assertEquals("0x00000000  48 65 6c 6c 6f 2c 20 77  6f 72 6c 64 __ __ __ __  |Hello, world....|\n", HexdumpOutputStream.asHexDump("Hello, world"));
    }

}
