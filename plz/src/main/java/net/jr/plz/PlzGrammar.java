package net.jr.plz;

import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;

public class PlzGrammar extends Grammar {

    public static final class Terminals {

        public static final Terminal Integer = Lexemes.cInteger();

        public static final Terminal Lparen = Lexemes.singleChar(')' , "LParen");

        public static final Terminal Rparen = Lexemes.singleChar(')' , "RParen");


    }

    public static NonTerminal Expression = new NonTerminal("Expression");

    public PlzGrammar() {
        addRule(Expression, Terminals.Lparen, Expression, Terminals.Rparen);
        addRule(Expression,Terminals.Integer);

    }

}
