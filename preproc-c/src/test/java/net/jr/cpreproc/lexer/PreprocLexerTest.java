package net.jr.cpreproc.lexer;

import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PreprocLexerTest {

    @Test
    public void basicTest() {
        List<? extends Token> tokens = PreprocLexer.tokenize("function(x,y)");
        Assert.assertEquals("[CIdentifier@1:1, '('@1:9, CIdentifier@1:10, ','@1:11, CIdentifier@1:12, ')'@1:13]", tokens.toString());
    }

    @Test
    public void testSharp() {
        List<? extends Token> tokens = PreprocLexer.tokenize("#define STR           \"str\"");
        Assert.assertEquals(6, tokens.size());
        Assert.assertEquals("StringifyOperator@1:1", tokens.get(0).toString());
        Assert.assertEquals("CIdentifier@1:2", tokens.get(1).toString());
        Assert.assertEquals("Whitespace@1:8", tokens.get(2).toString());
        Assert.assertEquals("CIdentifier@1:9", tokens.get(3).toString());
        Assert.assertEquals("Whitespace@1:12", tokens.get(4).toString());
        Assert.assertEquals("CString@1:23", tokens.get(5).toString());
    }

    @Test
    public void testNoMeaning() {
        List<? extends Token> tokens = PreprocLexer.tokenize("$$24_");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals("NoMeaning@1:1", tokens.get(0).toString());
        Assert.assertEquals("$$24", tokens.get(0).getText());
        Assert.assertEquals("CIdentifier@1:5", tokens.get(1).toString());
        Assert.assertEquals("_", tokens.get(1).getText());
    }

    @Test
    public void test() {
        String txt ="concat(x,y) \t  x##y";
        List<PreprocToken> tokens = PreprocLexer.tokenize(txt);
        String t = tokens.toString();
        Assert.assertEquals("[CIdentifier@1:1, '('@1:7, CIdentifier@1:8, ','@1:9, CIdentifier@1:10, ')'@1:11, Whitespace@1:12, CIdentifier@1:16, ConcatOperator@1:17, CIdentifier@1:19]", t);

    }
}
