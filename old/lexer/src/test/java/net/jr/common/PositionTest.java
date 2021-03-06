package net.jr.common;

import net.jr.marshalling.MarshallingUtil;
import org.junit.Assert;
import org.junit.Test;

public class PositionTest {

    @Test
    public void test() {
        Position p = new Position(12, 21);
        Assert.assertEquals(12, p.getLine());
        Assert.assertEquals(21, p.getColumn());
        Assert.assertEquals(22, p.nextColumn().getColumn());
        Assert.assertEquals(13, p.nextLine().getLine());
        Assert.assertEquals(1, p.nextLine().getColumn());
        Assert.assertEquals("12:21", p.toString());
        Position p2 = new Position(12, 21);
        Assert.assertEquals(p.hashCode(), p2.hashCode());
        Assert.assertFalse(p.equals(new Position(13, 21)));
        Assert.assertFalse(p.equals(new Position(12, 20)));
        Assert.assertFalse(p.equals(null));
        Assert.assertFalse(p.equals(new Object()));
        Assert.assertTrue(p.equals(p2));
    }

    @Test
    public void testUpdate() {
        Position p = Position.start();


        p = p.updated('c');

        Assert.assertEquals(1, p.getLine());
        Assert.assertEquals(2, p.getColumn());

        p = p.updated('\n');
        Assert.assertEquals(2, p.getLine());
        Assert.assertEquals(1, p.getColumn());

    }

    @Test
    public void testMarshall() {
        Position p = new Position(54, 151);
        byte[] bytes = MarshallingUtil.toByteArray(p, false);
        Position p2 = MarshallingUtil.fromByteArray(bytes, false);
        Assert.assertEquals(p, p2);
    }
}
