package net.jr.lexer;


import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.SingleChar;
import net.jr.lexer.impl.Word;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class LexerTest {

    @Test
    public void testSingleCharOk() {
        Lexer lexer = new Lexer(Arrays.asList(new SingleChar('X')));
        for (int i = 0; i < 100; i++) {
            lexer.tokenize("X");
        }
    }

    @Test(expected = LexicalError.class)
    public void testSingleCharFail() {
        Lexer lexer = new Lexer(Arrays.asList(new SingleChar('X')));
        lexer.tokenize("Y");
    }

    @Test
    public void testLiteralOk() {
        Lexer lexer = new Lexer(Arrays.asList(new Literal("abc")));
        lexer.tokenize("abc");
    }

    @Test(expected = LexicalError.class)
    public void testLiteralFail() {
        Lexer lexer = new Lexer(Arrays.asList(new Literal("abc")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testWordOk() {
        Lexer lexer = new Lexer(Arrays.asList(new Word("_", "0123456789")));
        lexer.tokenize("_1235");
        lexer.tokenize("_");

        Lexer lexer2 = new Lexer(Arrays.asList(new Word("ACTG")));
        lexer2.tokenize("GATTACA");

    }

    @Test(expected = LexicalError.class)
    public void testWordFail() {
        Lexer lexer = new Lexer(Arrays.asList(new Word("012345689")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testMultipleWords() {
        Lexer lexer = new Lexer(Arrays.asList(new Word(" \t"), new Word("abcdefghijklmnopqrstuvwxyz")));
        lexer.tokenize("this is a       complete phrase with words in lowercase characters that are sometimes separated by           large \t\tspaces");
    }

    protected Lexer getMixed() {
        Set<Lexeme> tokenTypes = new HashSet<>();
        tokenTypes.add(new Literal("if"));
        tokenTypes.add(new Literal("then"));
        tokenTypes.add(new Literal("else"));
        tokenTypes.add(new Literal("=="));
        tokenTypes.add(new SingleChar('('));
        tokenTypes.add(new SingleChar(')'));
        tokenTypes.add(new SingleChar('{'));
        tokenTypes.add(new SingleChar('}'));
        tokenTypes.add(Lexemes.cIdentifier());
        Lexer lexer = new Lexer(tokenTypes);
        return lexer;
    }

    @Test
    public void testMixed() throws IOException {
        getMixed().tokenize(new StringReader("if(a==b)then{c==thenabc}"));
    }

    @Test
    public void testFiltersWhitespaces() {
        Lexer lexer = new Lexer(Lexemes.number(), Lexemes.whitespace());
        lexer.filterOut(Lexemes.whitespace());
        lexer.filterOut(Lexemes.eof());


        List<Integer> integerList = new ArrayList<>();

        lexer.setTokenListener(token -> {
            integerList.add(Integer.parseInt(token.getMatchedText()));
        });
        lexer.tokenize("254 75468 68468 144 4548 941 1");
        Assert.assertEquals(7, integerList.size());
        Assert.assertEquals(254, integerList.get(0).intValue());
        Assert.assertEquals(75468, integerList.get(1).intValue());
        Assert.assertEquals(68468, integerList.get(2).intValue());
        Assert.assertEquals(144, integerList.get(3).intValue());
        Assert.assertEquals(4548, integerList.get(4).intValue());
        Assert.assertEquals(941, integerList.get(5).intValue());
        Assert.assertEquals(1, integerList.get(6).intValue());
    }

    @Test
    public void testBestMatch() {
        Lexer lexer = new Lexer(new Literal("id"), Lexemes.cIdentifier());
        List<Token> tokenList = new ArrayList<>();
        lexer.setTokenListener(t -> tokenList.add(t));
        lexer.tokenize("identify");
        Assert.assertFalse(tokenList.isEmpty());
        Assert.assertEquals("identify", tokenList.get(0).getMatchedText());
    }

    @Test
    public void testNoMatch() {
        Lexer lexer = new Lexer(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words EXCEPT this one");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(23, l.getPosition());
        }
    }

    @Test
    public void testNoMatchAtEnd() {
        Lexer lexer = new Lexer(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words except this ONE!");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(35, l.getPosition());
        }
    }

    @Test
    public void testCString() {
        Lexer lexer = new Lexer(Lexemes.cString(), Lexemes.whitespace());
        lexer.tokenize("\"Hello world\"\"Hello\\t\\tworld\" \"Hello\\nworld\"    \"Hello world\"");
    }

    @Test
    public void testToString() {
        Lexemes.eof().toString();
        Lexemes.cIdentifier().toString();
        Lexemes.whitespace().toString();
        new Literal("else").toString();
    }

    @Test
    public void testIterator() {
        int i=0;
        Token token = null;
        Iterator<Token> iterator = getMixed().iterator(new StringReader("if(a==b)then{c==thenabc}"));
        while(iterator.hasNext()) {
            token = iterator.next();
            i++;
            //System.out.println(nextToken);
        }
        Assert.assertEquals(13, i);
        Assert.assertEquals(Lexemes.eof(), token.getTokenType());
    }
}
