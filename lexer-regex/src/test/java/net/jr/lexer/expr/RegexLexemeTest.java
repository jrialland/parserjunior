package net.jr.lexer.expr;

import net.jr.lexer.Lexer;
import net.jr.lexer.LexicalError;
import net.jr.lexer.Token;
import net.jr.lexer.expr.impl.RegexAutomaton;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.Semaphore;

public class RegexLexemeTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Test
    public void testSequence() {
        RegexLexeme regexLexeme = new RegexLexeme("'test'");
        List<Token> tokens = Lexer.forLexemes(regexLexeme).tokenize("test");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(regexLexeme));
    }

    @Test(expected = LexicalError.class)
    public void testSequence2() {
        RegexLexeme regexLexeme = new RegexLexeme("'longTextSequence'");
        Lexer.forLexemes(regexLexeme).tokenize("longT");
    }

    @Test
    public void testRange() {
        RegexLexeme rangeLexeme = new RegexLexeme("'a'..'f'");
        List<Token> tokens = Lexer.forLexemes(rangeLexeme).tokenize("a");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(rangeLexeme));
    }

    @Test(expected = LexicalError.class)
    public void testRangeFail() {
        RegexLexeme rangeLexeme = new RegexLexeme("'a'..'f'");
        Lexer.forLexemes(rangeLexeme).tokenize("g");
    }

    @Test
    public void testOr() {
        RegexLexeme choice = new RegexLexeme("'A'|'B'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("B");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test
    public void testOr2() {
        RegexLexeme choice = new RegexLexeme("'daddy'|'mommy'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("daddy");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test
    public void testOr3() {
        RegexLexeme choice = new RegexLexeme("'a'..'z'|'A'..'Z'");
        List<Token> tokens = Lexer.forLexemes(choice).tokenize("X");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(choice));
    }

    @Test(expected = LexicalError.class)
    public void testOrFail() {
        RegexLexeme choice = new RegexLexeme("'daddy'|'mommy'");
        Lexer.forLexemes(choice).tokenize("B");
    }

    @Test
    public void testOptional() {
        RegexLexeme opt = new RegexLexeme("(((((('nancy'?))))))");
        Lexer.forLexemes(opt).tokenize("");
        Lexer.forLexemes(opt).tokenize("nancy");

        try {
            Lexer.forLexemes(opt).tokenize("bob");
            Assert.fail();
        } catch (LexicalError e) {
            //ok!
        }

        List<Token> tokens = Lexer.forLexemes(opt).tokenize("nancynancy");
        Assert.assertEquals(3, tokens.size());
        Assert.assertTrue(tokens.get(0).getTokenType().equals(opt));
    }

    @Test
    public void testGroup1() {
        RegexLexeme g = new RegexLexeme("('a''b''c''d')");
        Lexer.forLexemes(g).tokenize("abcd");
    }

    @Test
    public void testGroup() {
        RegexLexeme g = new RegexLexeme("(('a')|('b'))");
        List<Token> tokens = Lexer.forLexemes(g).tokenize("ab");
        Assert.assertEquals(3, tokens.size());
        Assert.assertEquals(g, tokens.get(0).getTokenType());
        Assert.assertEquals("a", tokens.get(0).getText());
        Assert.assertEquals(g, tokens.get(1).getTokenType());
        Assert.assertEquals("b", tokens.get(1).getText());
    }

    @Test
    public void testZeroOrMore() {
        RegexLexeme choice = new RegexLexeme("('ta'|'da')*");
        Lexer.forLexemes(choice).tokenize("");
        Lexer.forLexemes(choice).tokenize("da");
        Lexer.forLexemes(choice).tokenize("tadadatadadata");
    }

    @Test
    public void testOneOrMore() {
        RegexLexeme choice = new RegexLexeme("'nah'+");
        Lexer.forLexemes(choice).tokenize("nah");
        Lexer.forLexemes(choice).tokenize("nahnahnah");
    }

    @Test
    public void testAnyChar() {
        RegexLexeme anyCharLexeme = new RegexLexeme(".");
        List<Token> tokens = Lexer.forLexemes(anyCharLexeme).tokenize("a");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(anyCharLexeme));
    }

    @Test
    public void testHex() {
        RegexLexeme hex = new RegexLexeme("'0x'(('0'..'9'|'a'..'f'|'A'..'F')+)");
        List<Token> tokens = Lexer.forLexemes(hex).tokenize("0xdeadbeef");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(hex));
        Assert.assertTrue(tokens.get(0).getText().equals("0xdeadbeef"));
    }

    @Ignore
    @Test
    public void testGraph() throws Exception {
        RegexLexeme rangeLexeme = new RegexLexeme("'0x'(('0'..'9'|'a'..'f'|'A'..'F')+)");
        RegexAutomaton ra = (RegexAutomaton) rangeLexeme.getAutomaton();
        System.out.println(ra.toGraphviz());
        GraphvizViewer viewer = GraphvizViewer.show(ra.toGraphviz());

        final Semaphore semaphore = new Semaphore(0);

        viewer.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                semaphore.release();
            }
        });

        semaphore.acquire();
    }

}
