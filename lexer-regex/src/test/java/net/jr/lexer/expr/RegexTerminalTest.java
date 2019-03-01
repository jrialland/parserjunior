package net.jr.lexer.expr;

import net.jr.lexer.*;
import net.jr.lexer.expr.impl.RegexAutomaton;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class RegexTerminalTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    protected void show(RegexTerminal t) {
        try {
            String graph = ((RegexAutomaton) t.getAutomaton()).toGraphviz();
            System.out.println(graph);
            GraphvizViewer viewer = GraphvizViewer.show(graph);
            final Semaphore semaphore = new Semaphore(0);
            viewer.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    semaphore.release();
                }
            });
            semaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSequence() {
        RegexTerminal regexLexeme = new RegexTerminal("'test'");
        List<Token> tokens = Lexer.forLexemes(regexLexeme).tokenize("test");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(regexLexeme));
    }

    @Test(expected = LexicalError.class)
    public void testSequence2() {
        RegexTerminal regexLexeme = new RegexTerminal("'longTextSequence'");
        Lexer.forLexemes(regexLexeme).tokenize("longT");
    }

    @Test
    public void testRange() {
        RegexTerminal rangeLexeme = new RegexTerminal("'a'..'f'");
        List<Token> tokens = Lexer.forLexemes(rangeLexeme).tokenize("a");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(rangeLexeme));
    }

    @Test(expected = LexicalError.class)
    public void testRangeFail() {
        RegexTerminal rangeLexeme = new RegexTerminal("'a'..'f'");
        Lexer.forLexemes(rangeLexeme).tokenize("g");
    }

    @Test
    public void testOr() {
        RegexTerminal choice = new RegexTerminal("'A'|'B'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("B");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test
    public void testOr2() {
        RegexTerminal choice = new RegexTerminal("'daddy'|'mommy'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("daddy");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test
    public void testOr3() {
        RegexTerminal choice = new RegexTerminal("'a'..'z'|'A'..'Z'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("X");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test(expected = LexicalError.class)
    public void testOrFail() {
        RegexTerminal choice = new RegexTerminal("'daddy'|'mommy'");
        Lexer.forLexemes(choice).tokenize("B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        RegexTerminal opt = new RegexTerminal("(((((('nancy'?))))))");
    }

    @Test
    public void testOptional2() {

        RegexTerminal opt = new RegexTerminal("'a''b'?");
        //show(opt);
        List<Token> tokens = Lexer.forLexemes(opt).tokenize("ab");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals("ab", tokens.get(0).getText());
        Assert.assertEquals(opt, tokens.get(0).getTokenType());
        Assert.assertEquals(Lexemes.eof(), tokens.get(1).getTokenType());


        tokens = Lexer.forLexemes(opt).tokenize("a");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals("a", tokens.get(0).getText());
        Assert.assertEquals(opt, tokens.get(0).getTokenType());
        Assert.assertEquals(Lexemes.eof(), tokens.get(1).getTokenType());

    }

    @Test
    public void testOptional3() {

        RegexTerminal opt = new RegexTerminal("'a''b'?");
        Terminal c = Lexemes.singleChar('c');

        Lexer lexer = Lexer.forLexemes(opt, c);
        lexer.setFilteredOut(Lexemes.whitespace());

        List<Token> tokens = lexer.tokenize("c a ab ca a c ab");
        Assert.assertEquals("c", tokens.get(0).getText());
        Assert.assertEquals("a", tokens.get(1).getText());
        Assert.assertEquals("ab", tokens.get(2).getText());
        Assert.assertEquals("c", tokens.get(3).getText());
        Assert.assertEquals("a", tokens.get(4).getText());
        Assert.assertEquals("a", tokens.get(5).getText());
        Assert.assertEquals("c", tokens.get(6).getText());
        Assert.assertEquals("ab", tokens.get(7).getText());
        Assert.assertEquals(Lexemes.eof(), tokens.get(8).getTokenType());
    }

    @Test
    public void testGroup1() {
        RegexTerminal g = new RegexTerminal("('a''b''c''d')");
        Lexer.forLexemes(g).tokenize("abcd");
    }

    @Test
    public void testGroup() {
        RegexTerminal g = new RegexTerminal("(('a')|('b'))");
        List<Token> tokens = Lexer.forLexemes(g).tokenize("ab");
        Assert.assertEquals(3, tokens.size());
        Assert.assertEquals(g, tokens.get(0).getTokenType());
        Assert.assertEquals("a", tokens.get(0).getText());
        Assert.assertEquals(g, tokens.get(1).getTokenType());
        Assert.assertEquals("b", tokens.get(1).getText());
    }

    @Test
    public void testZeroOrMore() {
        RegexTerminal choice = new RegexTerminal("('ta'|'da')*");
        Lexer.forLexemes(choice).tokenize("");
        Lexer.forLexemes(choice).tokenize("da");
        Lexer.forLexemes(choice).tokenize("tadadatadadata");
    }

    @Test
    public void testOneOrMore() {
        RegexTerminal choice = new RegexTerminal("'nah'+");
        Lexer.forLexemes(choice).tokenize("nah");
        Lexer.forLexemes(choice).tokenize("nahnahnah");
    }

    @Test
    public void testAnyChar() {
        RegexTerminal anyCharLexeme = new RegexTerminal(".");
        List<Token> tokens = Lexer.forLexemes(anyCharLexeme).tokenize("a");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(anyCharLexeme));
    }

    @Test
    public void testHex() {
        RegexTerminal hex = new RegexTerminal("'0x'(('0'..'9'|'a'..'f'|'A'..'F')+)");
        List<Token> tokens = Lexer.forLexemes(hex).tokenize("0xdeadbeef");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(hex));
        Assert.assertTrue(tokens.get(0).getText().equals("0xdeadbeef"));
    }

    @Test
    public void testNumber() {
        RegexTerminal number = new RegexTerminal("'-'?'0'|(('1'..'9')('0'..'9')*)");
        Lexer lexer = Lexer.forLexemes(number);
        lexer.tokenize("0");
        lexer.tokenize("-0");
        lexer.tokenize("1");
        lexer.tokenize("-1");
        lexer.tokenize("861893544");
    }

}
