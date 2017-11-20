package net.jr.lexer.expr;

import net.jr.lexer.impl.CharConstraint;
import org.junit.Assert;
import org.junit.Test;

public class CharConstraintTest {

    @Test
    public void testEq() {
        CharConstraint cs = CharConstraint.Builder.eq(0x20).build();
        System.out.println(cs.toString());
    }

    @Test
    public void testInList() {
        CharConstraint cs = CharConstraint.Builder.inList("abcd").build();
        System.out.println(cs.toString());
    }

    @Test
    public void testInList2() {
        CharConstraint cs = CharConstraint.Builder.inList(new char[]{'a', 'b', 'c', 'd'}).build();
        Assert.assertTrue(cs.apply('a'));
        Assert.assertTrue(cs.apply('b'));
        Assert.assertTrue(cs.apply('c'));
        Assert.assertTrue(cs.apply('d'));
        Assert.assertFalse(cs.apply('e'));

        System.out.println(cs.toString());
    }

    @Test
    public void testInRange() {
        CharConstraint cs = CharConstraint.Builder.inRange('a', 'z').build();
        System.out.println(cs.toString());
    }

    @Test
    public void testNot() {
        CharConstraint cs = CharConstraint.Builder.not(CharConstraint.Builder.inRange('a', 'z')).build();
        System.out.println(cs.toString());
    }

    @Test
    public void testOr() {
        CharConstraint.Builder b1 = CharConstraint.Builder.inRange('a', 'z');
        CharConstraint.Builder b2 = CharConstraint.Builder.inRange('A', 'Z');
        CharConstraint cs = CharConstraint.Builder.or(b1, b2).build();

        Assert.assertTrue(cs.apply('a'));
        Assert.assertTrue(cs.apply('b'));
        Assert.assertTrue(cs.apply('C'));
        Assert.assertTrue(cs.apply('D'));
        Assert.assertTrue(cs.apply('Z'));
        Assert.assertFalse(cs.apply('!'));

        System.out.println(cs.toString());
    }
}
