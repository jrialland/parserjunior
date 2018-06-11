package net.jr.parser.ast;

import net.jr.common.Symbol;
import net.jr.lexer.basicterminals.SingleChar;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;
import net.jr.parser.ast.annotations.After;
import net.jr.parser.ast.annotations.Before;
import net.jr.parser.ast.annotations.BeforeEachNode;
import net.jr.util.HashUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class VisitorHelperTest {

    Symbol S = new NonTerminal("S");
    Symbol N = new NonTerminal("N");
    Symbol E = new NonTerminal("E");
    Symbol V = new NonTerminal("V");
    SingleChar mult = new SingleChar('*');
    SingleChar eq = new SingleChar('=');
    SingleChar x = new SingleChar('x');

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Test
    public void test() {

        Grammar grammar = new Grammar();

        //1. S → N
        grammar.addRule(S, N);
        //2. N → V = E
        grammar.addRule(N, V, eq, E);
        //3. N → E
        grammar.addRule(N, E);
        //4. E → V
        grammar.addRule(E, V);
        //5. V → x
        grammar.addRule(V, x);
        //6. V → * E
        grammar.addRule(V, mult, E);


        AstNode root = grammar.createParser().parse("x=*x");

        Visitor visitor = new Visitor();
        VisitorHelper.visit(root, visitor);

        Assert.assertEquals("eb36bbf7f4d374f75061b6ba587a7509", HashUtil.md5Hex(visitor.getActions().getBytes()));
    }

    public static class Visitor {

        List<String> actions = new ArrayList<>();

        @BeforeEachNode
        @SuppressWarnings("unused")
        public void beforeEachNode(AstNode node) {
            actions.add("beforeEachNode " + node.repr());
        }

        @Before("S")
        @SuppressWarnings("unused")
        public void beforeS(AstNode node) {
            actions.add("beforeS " + node.repr());
        }

        @Before("N")
        @SuppressWarnings("unused")
        public void beforeN(AstNode node) {
            actions.add("beforeN " + node.repr());
        }

        @Before("E")
        @SuppressWarnings("unused")
        public void beforeE(AstNode node) {
            actions.add("beforeE " + node.repr());
        }

        @Before("V")
        @SuppressWarnings("unused")
        public void beforeV(AstNode node) {
            actions.add("beforeV " + node.repr());
        }

        @After("S")
        @SuppressWarnings("unused")
        public void afterS(AstNode node) {
            actions.add("afterS " + node.repr());
        }

        @After("N")
        @SuppressWarnings("unused")
        public void afterN(AstNode node) {
            actions.add("afterN " + node.repr());
        }

        @After("E")
        @SuppressWarnings("unused")
        public void afterE(AstNode node) {
            actions.add("afterE " + node.repr());
        }

        @After("V")
        @SuppressWarnings("unused")
        public void afterV(AstNode node) {
            actions.add("afterV " + node.repr());
        }

        public String getActions() {
            StringWriter sw = new StringWriter();
            for (String action : actions) {
                sw.append(action);
                sw.append("\n");
            }
            return sw.toString();
        }
    }
}
