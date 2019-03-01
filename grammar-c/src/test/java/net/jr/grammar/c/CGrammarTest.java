package net.jr.grammar.c;


import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.LexicalError;
import net.jr.lexer.Token;
import net.jr.parser.ParseError;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;
import net.jr.parser.ast.annotations.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class CGrammarTest {

    private boolean useCache = true;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    protected List<Token> testLex(String expr) {
        Lexer l = new CGrammar().createParser().getLexer();
        Iterator<Token> it = l.iterator(new StringReader(expr));
        List<Token> tokens = new ArrayList<>();
        while (it.hasNext()) {
            Token token = it.next();
            tokens.add(token);
        }
        return tokens;
    }

    @Test
    public void testLex1() {
        testLex("for (int i=0;str[i]!='\\0';i+=1) {}");
    }

    @Test
    public void testLex2() {
        testLex("int  main( void) { int i; i = 12; return i || 2; }");
    }

    @Test
    public void testLexVoid() {
        List<Token> tokens = testLex("void");
        Assert.assertFalse(tokens.isEmpty());
        Assert.assertEquals("void", tokens.get(0).getTokenType().getName());

        tokens = testLex("volatile");
        Assert.assertFalse(tokens.isEmpty());
        Assert.assertEquals("volatile", tokens.get(0).getTokenType().getName());
    }

    @Test
    public void testLexTypes() {
        Lexer l = new CGrammar().createParser().getLexer();
        List<Token> tokens = l.tokenize("int intish;");
        Assert.assertEquals(4, tokens.size());
        Assert.assertEquals(CGrammar.Tokens.Int, tokens.get(0).getTokenType());
        Assert.assertEquals(CGrammar.Tokens.Identifier, tokens.get(1).getTokenType());
        Assert.assertEquals(CGrammar.Tokens.DotComma, tokens.get(2).getTokenType());
        Assert.assertEquals(Lexemes.eof(), tokens.get(3).getTokenType());

    }


    @Test
    public void testEqAssociativity() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        new CGrammar().createParser(CGrammar.Statement, false).parse("res = res * 10 ;");
    }


    @Test
    public void testSimple() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        AstNode ast = parser.parse("int main(int argc, char **argv) { return EXIT_SUCCESS;}");
        System.out.println(ast);
    }

    @Test
    public void testIfElse() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        AstNode ast = parser.parse("int main(int argc, char **argv) { if(argc==0) { return false;} else {return true;}}");
        Assert.assertEquals(grammar.getTargetSymbol(), ast.getSymbol());
    }

    @Test
    public void testTypedef() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser(useCache);
        parser.parse("typedef unsigned int size_t, *ptr_size_t; ptr_size_t pointer = 0;");
    }

    @Test
    public void testFunc() {
        AstNode root = new CGrammar().createParser(useCache).parse("int recursivefactorial(int n) { return n==0||n==1?1: n * fibo(n-1); }");
        AtomicBoolean called = new AtomicBoolean(false);
        VisitorHelper.visit(root, new Object() {

            @After("FunctionDefinition")
            public void visitFunctionDef(AstNode node) {
                String methodName = node.getChildOfType(CGrammar.Declarator).getChildOfType(CGrammar.DirectDeclarator).getChildren().get(0).asToken().getText();
                Assert.assertEquals("recursivefactorial", methodName);
                called.set(true);
            }

        });
        Assert.assertTrue(called.get());
    }

    @Test(expected = LexicalError.class)
    public void testLexicalError() {
        new CGrammar().createParser(useCache).parse("int aç(void);");
    }

    @Test(expected = ParseError.class)
    public void testParseError() {
        new CGrammar().createParser(useCache).parse("int main(void) pefkpezofk;");
    }

    @Test
    public void testtoString() {
        System.out.println(new CGrammar().toString());
    }
}


