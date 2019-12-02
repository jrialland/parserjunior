package net.jr.jrc.qvm.asm;

import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.lexer.basicterminals.Word;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;
import net.jr.parser.ast.annotations.After;

public class ExpressionGrammar extends Grammar {

    private static NonTerminal Expr = new NonTerminal("Expr");

    private static NonTerminal AddrRef = new NonTerminal("AddrRef");

    private static NonTerminal Number = new NonTerminal("Number");

    private static Terminal At = Lexemes.singleChar('@');

    private static Terminal Identifier = new Word(Lexemes.Alpha + "_.$", Lexemes.AlphaNum + "_.$");

    private static Terminal Minus = Lexemes.singleChar('-');

    private static Terminal Int = Lexemes.cInteger();

    private static Terminal Float = Lexemes.cFloatingPoint();

    private static ExpressionGrammar instance = new ExpressionGrammar();

    private Parser parser;

    private ExpressionGrammar() {
        setName("expression");
        setTargetRule(target(Expr).def(oneOf(AddrRef, Number)).get());
        target(AddrRef).def(At, Identifier);
        target(Number).def(optional(Minus), oneOf(Int, Float));
        parser = createParser();
        parser.getLexer().setFilteredOut(Lexemes.whitespace());
    }

    public static Expression parse(String txt) {
        Expression expression = new Expression(txt);
        AstNode ast = instance.parser.parse(txt);

        VisitorHelper.visit(ast, new Object() {

            @After("AddrRef")
            public void visitAddRef(AstNode node) {
                String symbol = node.repr().replaceFirst("@ *", "");
                expression.getRefs().add(symbol);
            }

            @After("Number")
            public void visitInt(AstNode node) {

            }


        });

        return expression;
    }

}
