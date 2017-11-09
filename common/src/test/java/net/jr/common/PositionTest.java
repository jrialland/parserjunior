package net.jr.common;

import org.junit.Assert;
import org.junit.Test;

public class PositionTest {

    @Test
    public void test() {
        Position p = new Position(12,21);
        Assert.assertEquals(12, p.getLine());
        Assert.assertEquals(21, p.getColumn());
        Assert.assertEquals(22, p.nextColumn().getColumn());
        Assert.assertEquals(13, p.nextLine().getLine());
        Assert.assertEquals(1, p.nextLine().getColumn());
        Assert.assertEquals("12:21", p.toString());
        Position p2 = new Position(12,21);
        Assert.assertEquals(p.hashCode(), p2.hashCode());
        Assert.assertFalse(p.equals(null));
        Assert.assertFalse(p.equals(new Object()));
        Assert.assertTrue(p.equals(p2));
    }
}
