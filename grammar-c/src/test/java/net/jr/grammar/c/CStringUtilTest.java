package net.jr.grammar.c;

import org.junit.Assert;
import org.junit.Test;

public class CStringUtilTest {


    @Test
    public void testUnescapeC() {
        byte[] b = CStringUtil.unescapeC("\"Hello\\x0d\\12\\a\"", true);
        Assert.assertEquals(b[0], 'H');
        Assert.assertEquals(b[1], 'e');
        Assert.assertEquals(b[2], 'l');
        Assert.assertEquals(b[3], 'l');
        Assert.assertEquals(b[4], 'o');
        Assert.assertEquals(b[5], '\r');
        Assert.assertEquals(b[6], '\n');
        Assert.assertEquals(b[7], 7);
        Assert.assertEquals(b[8], 0);
    }


    @Test
    public void testEscapeC() {
        String s = CStringUtil.escapeC(new byte[]{'H', 'e', 'l', 'l', 'o', 0x0d, 0x0a, 0});
        Assert.assertEquals("\"Hello\\r\\n\"", s);
    }
}
