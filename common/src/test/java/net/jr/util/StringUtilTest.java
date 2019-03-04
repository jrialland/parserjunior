package net.jr.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testToSubscript() {
        Assert.assertEquals("₁", StringUtil.toSubscript(1));
        Assert.assertEquals("₋₁₅₆₂", StringUtil.toSubscript(-1562));
    }

    @Test
    public void testSuperscript() {
        Assert.assertEquals("¹²³⁴⁵⁶⁷⁸⁹", StringUtil.toSuperscript(123456789));
        Assert.assertEquals("⁻¹²³⁸⁴¹", StringUtil.toSuperscript(-123841));
    }

    @Test
    public void testRepeat() {
        Assert.assertEquals("xxx", StringUtil.repeatUntilSize("x", 3));
        Assert.assertEquals("nahna", StringUtil.repeatUntilSize("nah", 5));
    }

    @Test
    public void testCenter() {
        Assert.assertEquals("  .  ", StringUtil.center(".", 5));
        Assert.assertEquals(" ... ", StringUtil.center("...", 5));
        Assert.assertEquals(".....", StringUtil.center(".........", 5));

        Assert.assertEquals("  .   ", StringUtil.center(".", 6));
        Assert.assertEquals(" ...  ", StringUtil.center("...", 6));
        Assert.assertEquals(" .... ", StringUtil.center("....", 6));
    }

    @Test
    public void testHighlight() {
        Assert.assertEquals("this is the [te]rminal [Te]st", StringUtil.highlight("this is the terminal Test", "te", "[", "]"));
    }

    @Test
    public void testEllipsis() {
        Assert.assertEquals("AVeryLong...", StringUtil.ellipsis("AVeryLongWord", 12, "..."));
        Assert.assertEquals("a complete …", StringUtil.ellipsis("a complete sentence with separate words", 12, " \u2026"));
    }

}
