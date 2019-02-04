package net.jr.lexer.impl;

import org.junit.Assert;
import org.junit.Test;

public class CharConstraintTest {

    @Test
    public void testBuildAny() {
        CharConstraint cc = CharConstraint.Builder.any().build();
        Assert.assertEquals("true", cc.getExpr());
        for(char c = 0; c < Character.MAX_VALUE; c++) {
            Assert.assertTrue(cc.apply(c));
        }
    }

    @Test
    public void testBuildEq() {
        CharConstraint cc = CharConstraint.Builder.eq('\'').build();
        Assert.assertEquals("c == 39", cc.getExpr());
        Assert.assertTrue(cc.apply('\''));
        Assert.assertFalse(cc.apply('3'));
    }

    @Test
    public void testBuildInRange() {
        CharConstraint cc = CharConstraint.Builder.inRange('a', 'z').build();
        Assert.assertEquals("(c >= 'a' && c <= 'z')", cc.getExpr());
        Assert.assertTrue(cc.apply('e'));
        Assert.assertFalse(cc.apply('R'));
    }

    @Test
    public void testBuildInList() {
        CharConstraint cc = CharConstraint.Builder.inList("abcD").build();
        Assert.assertEquals("\"abcD\".indexOf((char)c)>-1", cc.getExpr());
        Assert.assertTrue(cc.apply('a'));
        Assert.assertTrue(cc.apply('b'));
        Assert.assertTrue(cc.apply('c'));
        Assert.assertTrue(cc.apply('D'));
        Assert.assertFalse(cc.apply('d'));
    }


    @Test
    public void testBuildNot() {
        CharConstraint cc = CharConstraint.Builder.not(CharConstraint.Builder.eq('A')).build();
        Assert.assertEquals("!(c == 65)", cc.getExpr());
        Assert.assertTrue(cc.apply('e'));
        Assert.assertFalse(cc.apply('A'));
    }


    @Test
    public void testBuildOr() {
        CharConstraint.Builder b1 = CharConstraint.Builder.eq('A');
        CharConstraint.Builder b2 = CharConstraint.Builder.inList("BC");
        CharConstraint cc = CharConstraint.Builder.or(b1, b2).build();
        Assert.assertEquals("((c == 65)||(\"BC\".indexOf((char)c)>-1))", cc.getExpr());
        Assert.assertTrue(cc.apply('A'));
        Assert.assertTrue(cc.apply('C'));
        Assert.assertFalse(cc.apply('D'));
    }

    @Test
    public void testBuildAnd() {
        CharConstraint.Builder b1 = CharConstraint.Builder.eq('A');
        CharConstraint.Builder b2 = CharConstraint.Builder.inList("ABC");
        CharConstraint cc = CharConstraint.Builder.and(b1, b2).build();
        Assert.assertEquals("((c == 65)&&(\"ABC\".indexOf((char)c)>-1))", cc.getExpr());
        Assert.assertTrue(cc.apply('A'));
        Assert.assertFalse(cc.apply('B'));
    }


}
