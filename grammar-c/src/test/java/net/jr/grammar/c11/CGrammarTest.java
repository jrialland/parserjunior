package net.jr.grammar.c11;


import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexer;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


public class CGrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void testPrintln() {
        CGrammar grammar = new CGrammar();
        List<String> lines = new ArrayList<>(Arrays.asList(grammar.toString().split("\n")));
        lines = lines.stream().map(l -> l.replaceFirst("\\([0-9]+\\)", "")).collect(Collectors.toList());
        Collections.sort(lines);
        lines = lines.subList(0, lines.size() - 2);
        int i = 1;
        for (String line : lines) {
            System.out.println(Integer.toString(i++) + ".  \t" + line);
        }
    }

    @Test
    public void testLexer() {
        Lexer lexer = new CGrammar().createParser().getDefaultLexer();
        Lexeme l = lexer.tokenize("int").get(0).getTokenType();
        Assert.assertEquals(CGrammar.Tokens.Int, l);
    }

    @Test
    public void testSimple() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser();
        AstNode ast = parser.parse("int main(int argc, char **argv) { return EXIT_SUCCESS;}");
        System.out.println(ast);
    }

    @Test
    public void testIfElse() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser();
        AstNode ast = parser.parse("int main(int argc, char **argv) { if(argc==0) { return false;} else {return true;}}");
        System.out.println(ast);
    }

    @Test
    public void testFunc() {
        AstNode node  =new CGrammar().createParser().parse("int fibo(n) { return n==0||n==1?1: n * fibo(n-1); }");

        AtomicBoolean called = new AtomicBoolean(false);

        CGrammarHelper.visit(node, new Object() {

            public void visitFunctionDefinition(AstNode node) {
                String methodName = node.getChildOfType(CGrammar.Declarator).getChildOfType(CGrammar.DirectDeclarator).getChildren().get(0).asToken().getMatchedText();
                Assert.assertEquals("fibo", methodName);
                called.set(true);
            }

        });


        Assert.assertTrue(called.get());
    }

}


