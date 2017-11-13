package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.annotations.Target;
import net.jr.parser.ast.VisitorHelper;
import org.junit.Assert;
import org.junit.Test;

public class IfElseTest {

    Parser parser;

    Lexeme If = Lexemes.literal("if");
    Lexeme Else = Lexemes.literal("else");
    Lexeme LeftBrace = Lexemes.singleChar('(');
    Lexeme RightBrace = Lexemes.singleChar(')');
    Lexeme Expression = Lexemes.singleChar('E');

    Symbol All = new Forward("All");
    Symbol Statement = new Forward("Statement");
    Symbol SelectionStatement = new Forward("SelectionStatement");

    Grammar g = new Grammar();

    {
        g.addRule(All, g.oneOrMore(Statement));
        g.addRule(Statement, SelectionStatement);
        g.addRule(Statement, Lexemes.literal("S1"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S2"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S3"), Lexemes.singleChar(';'));
        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement);
        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement, Else, Statement);
        parser = g.createParser(All);
        parser.getDefaultLexer().setFilteredOut(Lexemes.whitespace());
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

            @Target("SelectionStatement")
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
