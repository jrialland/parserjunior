package net.jr.cpreproc.lexer;

import net.jr.lexer.Terminal;
import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PreprocLexerTest {

    private static void checkToken(Terminal expectedType, String expectedText, int expectedStartIndex, PreprocToken token) {
        Assert.assertEquals(expectedType, token.getTokenType());
        Assert.assertEquals(expectedText, token.getText());
        Assert.assertEquals(expectedStartIndex, token.getStartIndex());
    }

    @Test
    public void basicTest() {
        List<? extends Token> tokens = PreprocLexer.tokenize("function(x,y)");
        Assert.assertEquals("[cIdentifier@1:1, '('@1:9, cIdentifier@1:10, ','@1:11, cIdentifier@1:12, ')'@1:13]", tokens.toString());
    }

    @Test
    public void testSharp() {
        List<? extends Token> tokens = PreprocLexer.tokenize("#define STR           \"str\"");
        Assert.assertEquals(6, tokens.size());
        Assert.assertEquals("StringifyOperator@1:1", tokens.get(0).toString());
        Assert.assertEquals("cIdentifier@1:2", tokens.get(1).toString());
        Assert.assertEquals("Whitespace@1:8", tokens.get(2).toString());
        Assert.assertEquals("cIdentifier@1:9", tokens.get(3).toString());
        Assert.assertEquals("Whitespace@1:12", tokens.get(4).toString());
        Assert.assertEquals("cString@1:23", tokens.get(5).toString());
    }

    @Test
    public void testNoMeaning() {
        List<? extends Token> tokens = PreprocLexer.tokenize("$$24_");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals("NoMeaning@1:1", tokens.get(0).toString());
        Assert.assertEquals("$$24", tokens.get(0).getText());
        Assert.assertEquals("cIdentifier@1:5", tokens.get(1).toString());
        Assert.assertEquals("_", tokens.get(1).getText());
    }

    @Test
    public void test() {
        String txt = "concat(x,y) \t  x##y";
        List<PreprocToken> tokens = PreprocLexer.tokenize(txt);
        String t = tokens.toString();
        Assert.assertEquals("[cIdentifier@1:1, '('@1:7, cIdentifier@1:8, ','@1:9, cIdentifier@1:10, ')'@1:11, Whitespace@1:12, cIdentifier@1:16, ConcatOperator@1:17, cIdentifier@1:19]", t);
    }

    @Test
    public void testRandom() {
        List<PreprocToken> tokens = PreprocLexer.tokenize("y@7HKcyw=ZU^24VkXsYG#pv=GVZG-GEZnPuEK=VTmW32^^hvg!&b4SeeW%%FCUCwS2vkjBM_*Fw^DKJ5H24TQ3E?Qf+LvhdfQuZc");
        Assert.assertEquals(29, tokens.size());

        checkToken(PreprocToken.Identifier, "y", 0, tokens.get(0));
        checkToken(PreprocToken.NoMeaning, "@7", 1, tokens.get(1));
        checkToken(PreprocToken.Identifier, "HKcyw", 3, tokens.get(2));
        checkToken(PreprocToken.NoMeaning, "=", 8, tokens.get(3));
        checkToken(PreprocToken.Identifier, "ZU", 9, tokens.get(4));
        checkToken(PreprocToken.NoMeaning, "^24", 11, tokens.get(5));
        checkToken(PreprocToken.Identifier, "VkXsYG", 14, tokens.get(6));
        checkToken(PreprocToken.StringifyOperator, "#", 20, tokens.get(7));
        checkToken(PreprocToken.Identifier, "pv", 21, tokens.get(8));
        checkToken(PreprocToken.NoMeaning, "=", 23, tokens.get(9));
        checkToken(PreprocToken.Identifier, "GVZG", 24, tokens.get(10));
        checkToken(PreprocToken.NoMeaning, "-", 28, tokens.get(11));
        checkToken(PreprocToken.Identifier, "GEZnPuEK", 29, tokens.get(12));
        checkToken(PreprocToken.NoMeaning, "=", 37, tokens.get(13));
        checkToken(PreprocToken.Identifier, "VTmW32", 38, tokens.get(14));
        checkToken(PreprocToken.NoMeaning, "^^", 44, tokens.get(15));
        checkToken(PreprocToken.Identifier, "hvg", 46, tokens.get(16));
        checkToken(PreprocToken.NoMeaning, "!&", 49, tokens.get(17));
        checkToken(PreprocToken.Identifier, "b4SeeW", 51, tokens.get(18));
        checkToken(PreprocToken.NoMeaning, "%%", 57, tokens.get(19));
        checkToken(PreprocToken.Identifier, "FCUCwS2vkjBM_", 59, tokens.get(20));
        checkToken(PreprocToken.NoMeaning, "*", 72, tokens.get(21));
        checkToken(PreprocToken.Identifier, "Fw", 73, tokens.get(22));
        checkToken(PreprocToken.NoMeaning, "^", 75, tokens.get(23));
        checkToken(PreprocToken.Identifier, "DKJ5H24TQ3E", 76, tokens.get(24));
        checkToken(PreprocToken.NoMeaning, "?", 87, tokens.get(25));
        checkToken(PreprocToken.Identifier, "Qf", 88, tokens.get(26));
        checkToken(PreprocToken.NoMeaning, "+", 90, tokens.get(27));
        checkToken(PreprocToken.Identifier, "LvhdfQuZc", 91, tokens.get(28));
    }
}
