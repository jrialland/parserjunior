package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;
import net.jr.parser.ast.annotations.After;
import org.junit.Assert;
import org.junit.Test;

public class IfElseTest {

    Parser parser;

    Terminal If = Lexemes.literal("if");
    Terminal Else = Lexemes.literal("else");
    Terminal LeftBrace = Lexemes.singleChar('(');
    Terminal RightBrace = Lexemes.singleChar(')');
    Terminal Expression = Lexemes.singleChar('E');

    Symbol All = new NonTerminal("All");
    Symbol Statement = new NonTerminal("Statement");
    Symbol SelectionStatement = new NonTerminal("SelectionStatement");

    Grammar g = new Grammar();

    {
        g.addRule(All, g.oneOrMore(Statement));
        g.addRule(Statement, SelectionStatement);
        g.addRule(Statement, Lexemes.literal("S1"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S2"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S3"), Lexemes.singleChar(';'));
        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement);
        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement, Else, Statement);
        parser = g.createParser();
        parser.getLexer().setFilteredOut(Lexemes.whitespace());
    }

    @Test
    public void testParseSimple() {
        parser.parse("if(E) S1;");
    }

    @Test
    public void testParseElse() {
        parser.parse("if(E) S1; else S2;");
    }

    @Test
    public void testParseNestedElse() {
        parser.parse("if(E) S1; else if (E) S2; else S3;");
    }

    @Test
    public void testParseNonNestedElse() {
        parser.parse("if(E) S1; if (E) S2; else S3;");
    }

    @Test
    public void testVisitorHelper() {
        int[] counter = new int[]{0};
        AstNode node = parser.parse("if(E) S1; if (E) S2; else S3;");
        VisitorHelper.visit(node, new Object() {

            @After("SelectionStatement")
            public void selectionStmt(AstNode node) {
                switch (counter[0]) {
                    case 0:
                        Assert.assertEquals("if ( E ) S1 ;", node.repr());
                        counter[0] = 1;
                        break;
                    case 1:
                        Assert.assertEquals("if ( E ) S2 ; else S3 ;", node.repr());
                        counter[0] = 2;
                        break;
                    default:
                        Assert.fail();
                }
            }
        });
        Assert.assertEquals(2, counter[0]);
    }
}
