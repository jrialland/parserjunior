package net.jr.lexer.expr;

import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void testRange() {
        RegexLexeme rangeLexeme = new RegexLexeme("'a'...'z'");
        List<Token> tokens = Lexer.forLexemes(rangeLexeme).tokenize("a");
        Assert.assertTrue(tokens.size() == 2);
        Assert.assertTrue(tokens.get(0).getTokenType().equals(rangeLexeme));
    }

}
