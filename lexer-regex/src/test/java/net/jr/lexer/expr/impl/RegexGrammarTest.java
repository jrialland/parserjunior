package net.jr.lexer.expr.impl;

import net.jr.parser.impl.ActionTable;
import org.junit.BeforeClass;
import org.junit.Test;

public class RegexGrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Test
    public void testActionTable() {
        ActionTable actionTable = ActionTable.lalr1(new RegexGrammar());
        System.out.println(actionTable);
    }

    @Test
    public void testSequence() {
        new RegexGrammar().createParser().parse("'test'");
    }

    @Test
    public void testRange() {
        RegexGrammar g = new RegexGrammar();
        g.createParser().parse("'a'..'z'");
    }

    @Test
    public void testAnyChar() {
        new RegexGrammar().createParser().parse(".");
    }

    @Test
    public void testOptional() {
        new RegexGrammar().createParser().parse("'a'?");
    }

    @Test
    public void testZeroOrMore() {
        new RegexGrammar().createParser().parse("'x'*");
    }

    @Test
    public void testOneOrMore() {
        new RegexGrammar().createParser().parse("'x'+");
    }

    @Test
    public void testOr() {
        new RegexGrammar().createParser().parse("'a'|'b'");
    }
}
