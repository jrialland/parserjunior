package net.jr.lexer.expr;


import net.jr.common.Position;
import net.jr.common.Symbol;
import net.jr.lexer.*;
import net.jr.lexer.basiclexemes.Literal;
import net.jr.lexer.basiclexemes.MultilineComment;
import net.jr.lexer.basiclexemes.SingleChar;
import net.jr.lexer.basiclexemes.Word;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public abstract class AbstractLexerTestCases {

    protected abstract <L extends Symbol> Lexer getLexer(L... tokenTypes);

    protected <L extends Symbol> Lexer getLexer(Collection<L> tokenTypes) {
        return getLexer(tokenTypes.toArray(new Symbol[]{}));
    }

    @Test
    public void testWhitespace() {
        Lexeme x = new SingleChar('X');
        Lexer lexer = getLexer(Lexemes.whitespace(), x);
        for (int i = 0; i < 100; i++) {
            lexer.tokenize("      ");
            lexer.tokenize("  X  X    ");
        }
        lexer.setFilteredOut(Lexemes.whitespace());
        long count = lexer.tokenize("  X  X    ").stream().filter(t -> t.getTokenType().equals(x)).count();
        Assert.assertEquals(2L, count);
    }

    @Test
    public void testSingleCharOk() {
        Lexer lexer = getLexer(Arrays.asList(new SingleChar('X')));
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(2, lexer.tokenize("X").size());
        }
    }

    @Test(expected = LexicalError.class)
    public void testSingleCharFail() {
        Lexer lexer = getLexer(Arrays.asList(new SingleChar('X')));
        lexer.tokenize("Y");
    }

    @Test
    public void testLiteralOk() {
        Lexer lexer = getLexer(Arrays.asList(new Literal("abc")));
        List<Token> tokens = lexer.tokenize("abc");
        Assert.assertEquals(2, tokens.size());
    }

    @Test(expected = LexicalError.class)
    public void testLiteralFail() {
        Lexer lexer = getLexer(Arrays.asList(new Literal("abc")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testWordOk() {
        Lexer lexer = getLexer(Arrays.asList(new Word("_", "0123456789")));

        Token tok = lexer.tokenize("_1235").get(0);
        Assert.assertEquals("_1235", tok.getText());

        Token tok2 = lexer.tokenize("_").get(0);
        Assert.assertEquals("_", tok2.getText());

        Lexer lexer2 = getLexer(Arrays.asList(new Word("ACTG")));
        Token tok3 = lexer2.tokenize("GATTACA").get(0);
        Assert.assertEquals("GATTACA", tok3.getText());

    }

    @Test(expected = LexicalError.class)
    public void testWordFail() {
        Lexer lexer = getLexer(Arrays.asList(new Word("012345689")));
        lexer.tokenize("aBc");
    }

    @Test
    public void testMultipleWords() {
        Lexer lexer = getLexer(Arrays.asList(new Word(" \t"), new Word("abcdefghijklmnopqrstuvwxyz")));
        List<Token> tokens = lexer.tokenize("this is a       complete phrase with words in lowercase characters that are sometimes separated by           large \t\tspaces");
        Assert.assertEquals(34, tokens.size());
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
        List<Token> tokens = getLexer(getMixedTokenTypes()).tokenize(new StringReader("if(a==b)then{c==thenabc}"));
        Assert.assertEquals(13, tokens.size());
    }

    @Test
    public void testFiltersWhitespaces() {
        Lexer lexer = getLexer(Lexemes.cInteger(), Lexemes.whitespace());
        lexer.setFilteredOut(Lexemes.whitespace());
        lexer.setFilteredOut(Lexemes.eof());


        List<Integer> integerList = new ArrayList<>();

        lexer.setTokenListener(token -> {
            boolean whitespace = token.getTokenType().equals(Lexemes.whitespace());
            if (whitespace) {
                Assert.fail();
            }
            integerList.add(Integer.parseInt(token.getText()));
            return token;
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
        Lexer lexer = getLexer(new Literal("id"), Lexemes.cIdentifier());
        List<Token> tokenList = new ArrayList<>();
        lexer.setTokenListener(t -> {
            tokenList.add(t);
            return t;
        });
        lexer.tokenize("identify");
        Assert.assertFalse(tokenList.isEmpty());
        Assert.assertEquals("identify", tokenList.get(0).getText());
    }

    @Test
    public void testNoMatch() {
        Lexer lexer = getLexer(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words EXCEPT this one");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(new Position(1, 24), l.getPosition());
        }
    }

    @Test
    public void testNoMatchAtEnd() {
        Lexer lexer = getLexer(Lexemes.lowercaseWord(), Lexemes.whitespace());
        try {
            lexer.tokenize("mostly lowercase words except this ONE!");
            Assert.fail();
        } catch (LexicalError l) {
            Assert.assertEquals(new Position(1, 36), l.getPosition());
        }
    }

    @Test
    public void testCString() {
        Lexer lexer = getLexer(Lexemes.cString(), Lexemes.whitespace());
        List<Token> tokens = lexer.tokenize("\"Hello world\"\"Hello\\t\\tworld\" \"Hello\\nworld\"    \"Hello world\"");
        Assert.assertEquals(7, tokens.size());
    }

    @Test
    public void testIterator() {
        int i = 0;
        Token token = null;
        Iterator<Token> iterator = getLexer(getMixedTokenTypes()).iterator(new StringReader("if(a==b)then{c==thenabc}"));
        while (iterator.hasNext()) {
            token = iterator.next();
            i++;
        }
        Assert.assertEquals(13, i);
        Assert.assertEquals(Lexemes.eof(), token.getTokenType());
    }

    @Test
    public void testIteratorWithFilteredOut() {
        int i = 0;
        Token token = null;

        Set<Lexeme> tokenTypes = getMixedTokenTypes();
        tokenTypes.add(Lexemes.whitespace());

        Lexer lexer = getLexer(tokenTypes);
        lexer.setFilteredOut(Lexemes.whitespace());
        Iterator<Token> iterator = lexer
                .iterator(new StringReader("if (a == b) then \t { c == thenabc}"));

        while (iterator.hasNext()) {
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
        Lexer lexer = getLexer(x, eq, star);
        lexer.setFilteredOut(Lexemes.whitespace());
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
        Lexer lexer = getLexer(Lexemes.cHexNumber(), Lexemes.whitespace());
        List<Token> toks = lexer.tokenize("0xdeadbeef 0x15615 0x1223 0x1 0xff");
        Assert.assertEquals(10, toks.size());
    }


    private static List<Token> asList(Iterator<Token> it) {
        List<Token> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    @Test
    public void testSingleChar() {
        String txt = "()))())()()())((";
        Lexer lexer = getLexer(new SingleChar('('), new SingleChar(')'));
        List<Token> tokens = lexer.tokenize(txt);

        int i = 0;
        for (Token token : tokens) {
            if (i < txt.length()) {
                Assert.assertEquals(txt.toCharArray()[i++], token.getText().charAt(0));
            }
        }
        Assert.assertTrue(tokens.get(tokens.size() - 1).getTokenType().equals(Lexemes.eof()));
    }

    @Test
    public void testPreferLiteralOverWord() {
        Lexer lexer = getLexer(new Word(Lexemes.Alpha), new Literal("test"));
        List<Token> tokens = lexer.tokenize("test");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals(Lexemes.eof(), tokens.get(1).getTokenType());
        Assert.assertEquals(Literal.class, tokens.get(0).getTokenType().getClass());
    }

    @Test
    public void testPosition() {
        Lexer lexer = getLexer(Lexemes.cHexNumber());
        lexer.setFilteredOut(Lexemes.newLine());
        lexer.setFilteredOut(Lexemes.whitespace());
        List<Token> tokens = lexer.tokenize("0xdead\n0xbeef\n\n    0xcafe 0xbabe");

        Assert.assertEquals("0xdead", tokens.get(0).getText());
        Assert.assertEquals(1, tokens.get(0).getPosition().getLine());
        Assert.assertEquals(1, tokens.get(0).getPosition().getColumn());

        Assert.assertEquals("0xbeef", tokens.get(1).getText());
        Assert.assertEquals(2, tokens.get(1).getPosition().getLine());
        Assert.assertEquals(1, tokens.get(1).getPosition().getColumn());

        Assert.assertEquals("0xcafe", tokens.get(2).getText());
        Assert.assertEquals(4, tokens.get(2).getPosition().getLine());
        Assert.assertEquals(5, tokens.get(2).getPosition().getColumn());

        Assert.assertEquals("0xbabe", tokens.get(3).getText());
        Assert.assertEquals(4, tokens.get(3).getPosition().getLine());
        Assert.assertEquals(12, tokens.get(3).getPosition().getColumn());
    }

    @Test
    public void testCChar() {
        Lexer lexer = getLexer(Lexemes.cCharacter());
        lexer.setFilteredOut(Lexemes.newLine());
        lexer.setFilteredOut(Lexemes.whitespace());
        List<Token> tokens = lexer.tokenize("'a' 'b' '\\t' '\\n' 'C'");
        Assert.assertEquals(6, tokens.size());
        Assert.assertEquals(Lexemes.eof(), tokens.get(tokens.size()-1).getTokenType());
    }

    @Test
    public void testCOctal() {
        Lexer lexer = getLexer(Lexemes.cOctal());
        List<Token> tokens = lexer.tokenize("05135745211");
        Assert.assertEquals(2, tokens.size());
    }

    @Test
    public void testCBinary() {
        Lexer lexer = getLexer(Lexemes.cOctal(), Lexemes.cBinary());
        List<Token> tokens = lexer.tokenize("0b1011011");
        System.out.println(tokens);
        Assert.assertEquals(2, tokens.size());
    }

    @Test
    public void testCInteger() {
        Lexer lexer = getLexer(Lexemes.cInteger());
        List<Token> tokens = lexer.tokenize("0");
        Assert.assertEquals(2, tokens.size());
        Assert.assertEquals(Lexemes.eof(), tokens.get(tokens.size()-1).getTokenType());
        Assert.assertEquals("0", tokens.get(0).getText());
    }

    @Test
    public void testNumber() {
        Lexer lexer = getLexer(Lexemes.cOctal(), Lexemes.cBinary(), Lexemes.cInteger(), Lexemes.cHexNumber());
        String s = lexer.tokenize("1545447").get(0).getText();
        Assert.assertEquals(1545447, Integer.parseInt(s));
    }

    @Test
    public void testInt() {
        Lexer lexer = getLexer(Lexemes.literal("int"), Lexemes.cIdentifier());
        Lexeme l = lexer.tokenize("int").get(0).getTokenType();
        Assert.assertEquals(l, Lexemes.literal("int"));
        Assert.assertFalse(l.equals(Lexemes.cIdentifier()));
    }

    @Test
    public void testInt2() {
        Lexer lexer = getLexer(Lexemes.literal("int"), Lexemes.cIdentifier());

        //give a low priority to cIdentifier
        lexer.setPriority(Lexemes.cIdentifier(), 100);
        Lexeme l = lexer.tokenize("int").get(0).getTokenType();
        Assert.assertEquals(l, Lexemes.cIdentifier());
        Assert.assertFalse(l.equals(Lexemes.literal("int")));
    }

    @Test
    public void testMultilineComment() {
        Lexer l = getLexer(Lexemes.multilineComment("/*", "*/"), Lexemes.cIdentifier());
        l.setFilteredOut(Lexemes.singleChar(' '));
        List<Token> tokens = l.tokenize("This /* is a\n test */ works");
        Assert.assertEquals(4, tokens.size());
        Assert.assertEquals(MultilineComment.class, tokens.get(1).getTokenType().getClass());
        Assert.assertEquals("/* is a\n test */", tokens.get(1).getText());
        Assert.assertEquals("This", tokens.get(0).getText());
        Assert.assertEquals("works", tokens.get(2).getText());
        Assert.assertEquals(Lexemes.eof(), tokens.get(3).getTokenType());
    }
}
