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
    public void testWhitespace() {
        Lexer lexer = Lexer.forLexemes(Lexemes.whitespace(), new SingleChar('X'));
        for (int i = 0; i < 100; i++) {
            lexer.tokenize("      ");
            lexer.tokenize("  X  X    ");
        }
        lexer.filterOut(Lexemes.whitespace());
        lexer.tokenize("  X  X    ");
    }

    @Test
    public void testSingleCharOk() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new SingleChar('X')));
        for (int i = 0; i < 100; i++) {
            lexer.tokenize("X");
        }
    }

    @Test(expected = LexicalError.class)
    public void testSingleCharFail() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new SingleChar('X')));
        lexer.tokenize("Y");
    }

    @Test
    public void testLiteralOk() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new Literal("abc")));
        lexer.tokenize("abc");
    }

    @Test(expected = LexicalError.class)
    public void testLiteralFail() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new Literal("abc")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testWordOk() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new Word("_", "0123456789")));
        lexer.tokenize("_1235");
        lexer.tokenize("_");

        Lexer lexer2 = Lexer.forLexemes(Arrays.asList(new Word("ACTG")));
        lexer2.tokenize("GATTACA");

    }

    @Test(expected = LexicalError.class)
    public void testWordFail() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new Word("012345689")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testMultipleWords() {
        Lexer lexer = Lexer.forLexemes(Arrays.asList(new Word(" \t"), new Word("abcdefghijklmnopqrstuvwxyz")));
        lexer.tokenize("this is a       complete phrase with words in lowercase characters that are sometimes separated by           large \t\tspaces");
    }

    protected Set<Lexeme> getMixedTokenTypes() {
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
        return tokenTypes;
    }

    @Test
    public void testMixed() throws IOException {
        Lexer.forLexemes(getMixedTokenTypes()).tokenize(new StringReader("if(a==b)then{c==thenabc}"));
    }

    @Test
    public void testFiltersWhitespaces() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cInteger(), Lexemes.whitespace());
        lexer.filterOut(Lexemes.whitespace());
        lexer.filterOut(Lexemes.eof());


        List<Integer> integerList = new ArrayList<>();

        lexer.tokenListener(token -> {
            boolean whitespace = token.getTokenType().equals(Lexemes.whitespace());
            if(whitespace) {
                Assert.fail();
            }
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
        Lexer lexer = Lexer.forLexemes(new Literal("id"), Lexemes.cIdentifier());
        List<Token> tokenList = new ArrayList<>();
        lexer.tokenListener(t -> tokenList.add(t));
        lexer.tokenize("identify");
        Assert.assertFalse(tokenList.isEmpty());
        Assert.assertEquals("identify", tokenList.get(0).getMatchedText());
    }

    @Test
    public void testNoMatch() {
        Lexer lexer = Lexer.forLexemes(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words EXCEPT this one");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(23, l.getPosition());
        }
    }

    @Test
    public void testNoMatchAtEnd() {
        Lexer lexer = Lexer.forLexemes(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words except this ONE!");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(35, l.getPosition());
        }
    }

    @Test
    public void testCString() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cString(), Lexemes.whitespace());
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
        Iterator<Token> iterator = Lexer.forLexemes(getMixedTokenTypes()).iterator(new StringReader("if(a==b)then{c==thenabc}"));
        while(iterator.hasNext()) {
            token = iterator.next();
            i++;
            //System.out.println(nextToken);
        }
        Assert.assertEquals(13, i);
        Assert.assertEquals(Lexemes.eof(), token.getTokenType());
    }

    @Test
    public void testIteratorWithFilteredOut() {
        int i=0;
        Token token = null;

        Set<Lexeme> tokenTypes = getMixedTokenTypes();
        tokenTypes.add(Lexemes.whitespace());

        Lexer lexer = Lexer.forLexemes(tokenTypes);

        Iterator<Token> iterator = lexer
                .filterOut(Lexemes.whitespace())
                .iterator(new StringReader("if (a == b) then \t { c == thenabc}"));

        while(iterator.hasNext()) {
            token = iterator.next();
            i++;
        }

        Assert.assertEquals(13, i);
        Assert.assertEquals(Lexemes.eof(), token.getTokenType());
    }

    @Test
    public void testWhitespace2() {
        SingleChar x = new SingleChar('x');
        SingleChar eq = new SingleChar('=');
        SingleChar star = new SingleChar('*');
        Lexer lexer = Lexer.forLexemes(x, eq, star);
        lexer.filterOut(Lexemes.whitespace());
        List<Token> list;
        list = asList(lexer.iterator(new StringReader("x=*x")));
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(x, list.get(0).getTokenType());
        Assert.assertEquals(eq, list.get(1).getTokenType());
        Assert.assertEquals(star, list.get(2).getTokenType());
        Assert.assertEquals(x, list.get(3).getTokenType());
        Assert.assertEquals(Lexemes.eof(), list.get(4).getTokenType());

        list = asList(lexer.iterator(new StringReader("x = *    x")));
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(x, list.get(0).getTokenType());
        Assert.assertEquals(eq, list.get(1).getTokenType());
        Assert.assertEquals(star, list.get(2).getTokenType());
        Assert.assertEquals(x, list.get(3).getTokenType());
        Assert.assertEquals(Lexemes.eof(), list.get(4).getTokenType());
    }

    @Test
    public void testHexNumber() {


        Lexer lexer = Lexer.forLexemes(Lexemes.cHexNumber(), Lexemes.whitespace());
        lexer.tokenListener(new TokenListener() {
            @Override
            public void onToken(Token token) {
                System.out.println(token);
            }
        });
        lexer.tokenize("0xdeadbeef 0x15615 0x1223 0x1 0xff");

    }


    private static List<Token> asList(Iterator<Token> it) {
        List<Token> list = new ArrayList<>();
        while(it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    @Test
    public void testSingleChar() {
        String txt = "()))())()()())((";
        Lexer lexer = Lexer.forLexemes(new SingleChar('('), new SingleChar(')'));
        List<Token> tokens = lexer.tokenize(txt);

        int i=0;
        for(Token token : tokens) {
            if(i<txt.length()) {
                Assert.assertEquals(txt.toCharArray()[i++], token.getMatchedText().charAt(0));
            }
        }
        Assert.assertTrue(tokens.get(tokens.size()-1).getTokenType().equals(Lexemes.eof()));
    }

    @Test
    public void testPreferLiteralOverWord() {
        Lexer lexer = Lexer.forLexemes(new Word(Lexemes.Alpha), new Literal("test"));
        List<Token> tokens = lexer.tokenize("test");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals(Lexemes.eof(), tokens.get(1).getTokenType());
        Assert.assertEquals(Literal.class, tokens.get(0).getTokenType().getClass());
    }

    @Test
    public void testPosition() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cHexNumber());
        lexer.filterOut(Lexemes.newLine());
        lexer.filterOut(Lexemes.whitespace());
        List<Token> tokens = lexer.tokenize("0xdead\n0xbeef\n\n    0xcafe 0xbabe");

        Assert.assertEquals("0xdead", tokens.get(0).getMatchedText());
        Assert.assertEquals(1, tokens.get(0).getPosition().getLine());
        Assert.assertEquals(1, tokens.get(0).getPosition().getColumn());

        Assert.assertEquals("0xbeef", tokens.get(1).getMatchedText());
        Assert.assertEquals(2, tokens.get(1).getPosition().getLine());
        Assert.assertEquals(1, tokens.get(1).getPosition().getColumn());

        Assert.assertEquals("0xcafe", tokens.get(2).getMatchedText());
        Assert.assertEquals(4, tokens.get(2).getPosition().getLine());
        Assert.assertEquals(5, tokens.get(2).getPosition().getColumn());

        Assert.assertEquals("0xbabe", tokens.get(3).getMatchedText());
        Assert.assertEquals(4, tokens.get(3).getPosition().getLine());
        Assert.assertEquals(12, tokens.get(3).getPosition().getColumn());
    }

    @Test
    public void testCChar() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cCharacter());
        lexer.filterOut(Lexemes.newLine());
        lexer.filterOut(Lexemes.whitespace());
        List<Token> tokens = lexer.tokenize("'a' 'b' '\\t' '\\n' 'C'");
    }

    @Test
    public void testCOctal() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cOctal());
        lexer.tokenize("05135745211");
    }

    @Test
    public void testCBinary() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cOctal(), Lexemes.cBinary());
        lexer.tokenize("0b1011011");
    }

    @Test
    public void testCInteger() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cInteger());
        lexer.tokenize("0");
    }

    @Test
    public void testNumber() {
        Lexer lexer = Lexer.forLexemes(Lexemes.cOctal(), Lexemes.cBinary(), Lexemes.cInteger(), Lexemes.cHexNumber());
        lexer.tokenize("0");
    }

    @Test
    public void testInt() {
        Lexer lexer = Lexer.forLexemes(Lexemes.literal("int"), Lexemes.cIdentifier());
        Lexeme l = lexer.tokenize("int").get(0).getTokenType();
        Assert.assertEquals(l, Lexemes.literal("int"));
        Assert.assertFalse(l.equals(Lexemes.cIdentifier()));
    }

    @Test
    public void testInt2() {
        Lexer lexer = Lexer.forLexemes(Lexemes.literal("int"), Lexemes.cIdentifier());

        //give a low priority to cIdentifier
        lexer.setPriority(Lexemes.cIdentifier(), 100);
        Lexeme l = lexer.tokenize("int").get(0).getTokenType();
        Assert.assertEquals(l, Lexemes.cIdentifier());
        Assert.assertFalse(l.equals(Lexemes.literal("int")));
    }

}
