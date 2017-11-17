package net.jr.types;

import org.junit.Assert;
import org.junit.Test;

public class TypeUtilTest {

    @Test
    public void testGetBytecodeTypename() {
        Assert.assertEquals("Lnet/jr/types/TypeUtilTest;", TypeUtil.getBytecodeTypename(TypeUtilTest.class));

        Assert.assertEquals("C", TypeUtil.getBytecodeTypename(Character.class));
        Assert.assertEquals("S", TypeUtil.getBytecodeTypename(Short.class));
        Assert.assertEquals("J", TypeUtil.getBytecodeTypename(Long.class));
        Assert.assertEquals("F", TypeUtil.getBytecodeTypename(Float.class));
        Assert.assertEquals("D", TypeUtil.getBytecodeTypename(Double.class));
        Assert.assertEquals("Z", TypeUtil.getBytecodeTypename(Boolean.class));

        Assert.assertEquals("C", TypeUtil.getBytecodeTypename(Character.TYPE));
        Assert.assertEquals("S", TypeUtil.getBytecodeTypename(Short.TYPE));
        Assert.assertEquals("J", TypeUtil.getBytecodeTypename(Long.TYPE));
        Assert.assertEquals("F", TypeUtil.getBytecodeTypename(Float.TYPE));
        Assert.assertEquals("D", TypeUtil.getBytecodeTypename(Double.TYPE));
        Assert.assertEquals("Z", TypeUtil.getBytecodeTypename(Boolean.TYPE));
    }

    @Test
    public void testForBytecodeTypename() {
        String name = TypeUtil.getBytecodeTypename(TypeUtilTest.class);
        Assert.assertEquals(TypeUtilTest.class, TypeUtil.forBytecodeTypename(name));
        Assert.assertEquals(Integer.class, TypeUtil.forBytecodeTypename("I"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailforBytecodeTypename() {
        TypeUtil.forBytecodeTypename(null);
    }
}
