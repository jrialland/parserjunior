package net.jr.codegen.java;

import net.jr.grammar.c.CGrammar;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.parser.Associativity;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;
import org.junit.Test;

import java.io.StringWriter;

public class ParserGeneratorTest {

    static class FourOpsGrammar extends Grammar {

        private static final Terminal Number = Lexemes.cInteger();

        private static final Terminal Plus = Lexemes.singleChar('+');

        private static final Terminal Minus = Lexemes.singleChar('-');

        private static final Terminal Mult = Lexemes.singleChar('*');

        private static final Terminal Div = Lexemes.singleChar('/');

        private static final NonTerminal Expr = new NonTerminal("Expr");

        public FourOpsGrammar() {

            //an expression can be just a number
            target(Expr)
                    .def(Number).withName("number");

            //an expression can be an addition, or a substraction
            target(Expr)
                    .def(Expr, oneOf(Plus, Minus), Expr)
                    .withAssociativity(Associativity.Left)
                    .withName("additiveExpression");

            //an expression can also be a multiplication or a division
            target(Expr)
                    .def(Expr, oneOf(Mult, Div), Expr)
                    .withAssociativity(Associativity.Left)
                    .withName("multiplicativeExpression");

            //multiplications are always computed before additions
            setPrecedenceLevel(20, Mult, Div);
            setPrecedenceLevel(10, Plus, Minus);
        }
    }

    ;

    @Test
    public void testCodeGeneration() {

        ParserGenerator generator = new ParserGenerator();

        StringWriter sw = new StringWriter();
        generator.generate(new CGrammar(), sw);
        String code = sw.toString();

        System.out.println(code);

        //Class<?> parserClass = Compiler.compile("CParser", new StringReader(code));

    }
}
