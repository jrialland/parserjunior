package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testToSubscript() {
        String s  = StringUtil.toSubscript(-1562);
        Assert.assertEquals("-₁₅₆₂", s);
    }

    @Test
    public void testSuperScript() {
        String s  = StringUtil.toSuperScript(-123841);
        Assert.assertEquals("-¹²³⁸⁴¹", s);
    }

    @Test
    public void testRepeat() {
        Assert.assertEquals("xxx", StringUtil.repeat("x", 3));
        Assert.assertEquals("nahna", StringUtil.repeat("nah", 5));
    }

    @Test
    public void testCenter() {
        Assert.assertEquals("  .  ", StringUtil.center(".", 5));
        Assert.assertEquals(" ... ", StringUtil.center("...", 5));
        Assert.assertEquals(".....", StringUtil.center(".........", 5));
    }


}
