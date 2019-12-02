package net.jr.lexer.expr.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.Terminal;
import net.jr.lexer.Token;
import net.jr.lexer.basicterminals.Literal;
import net.jr.lexer.basicterminals.QuotedString;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;
import net.jr.parser.Parser;

/**
 * Character sequence must be enclosed in '' quotes. For example, this lexer rule matches salutation 'Hello': <code>'hello'</code>
 * <p>
 * Use '|' to express alternatives. Lexer rule matching either salutation 'Hello' or 'Bye': <code>'Hello'|'Bye'</code>
 * </p>
 * <p>
 * The dot '.' represents any character
 * </p>
 * <p>
 * Use '..' to express interval. Lexer rule matching either 0 or 1 or 2 or 3 or 4 or 5 or 6 or 7 or 8 or 9: '0'..'9'
 * </p>
 * <p>
 * Use '+' to express repeated one or more times. Lexer rule matching any integer : ('0'..'9')+
 * </p>
 */
public class RegexGrammar extends Grammar {

    public static final NonTerminal OneOrMoreExpr = new NonTerminal("OneOrMoreExpr");
    public static final NonTerminal Regex = new NonTerminal("Regex");
    public static final NonTerminal Expr = new NonTerminal("Expr");
    public static final NonTerminal Sequence = new NonTerminal("Sequence");
    public static final NonTerminal CharacterRange = new NonTerminal("CharacterRange");
    private Lexer lexer;

    public RegexGrammar() {

        addRule(OneOrMoreExpr, oneOrMore(Expr));

        setTargetRule(addRule(Regex, OneOrMoreExpr).withName("Regex").get());

        addRule(Expr, Tokens.LeftBrace, OneOrMoreExpr, Tokens.RightBrace).withName("Group");

        addRule(Expr, Sequence).withName("CharSequence");

        addRule(Expr, CharacterRange).withName("Range");

        addRule(Expr, Tokens.Char).withName("Char");

        addRule(Expr, Tokens.Dot).withName("AnyChar");

        addRule(Expr, Expr, Tokens.QuestionMark).withName("Optional");

        addRule(Expr, Expr, Tokens.Star).withName("ZeroOrMore");

        addRule(Expr, Expr, Tokens.Plus).withName("OneOrMore");

        addRule(Expr, Expr, Tokens.Pipe, Expr).withName("Or");

        target(CharacterRange)
                .def(Tokens.Char, Tokens.ThreePoints, Tokens.Char);

        target(Sequence)
                .def(Tokens.SingleQuotedString);

        lexer = Lexer.forLexemes(getTerminals());
        lexer.setFilteredOut(Lexemes.whitespace());
        lexer.setTokenListener(token -> {
            if (token.getTokenType().equals(Tokens.SingleQuotedString) && token.getText().length() == 3) {
                return new Token(Tokens.Char, token.getPosition(), token.getText());
            } else {
                return token;
            }
        });

    }

    @Override
    public Parser createParser(Symbol symbol, boolean useActionTableCache) {
        Parser parser = super.createParser(symbol, useActionTableCache);
        parser.setLexer(lexer);
        return parser;
    }

    public static final class Tokens {

        public static final Terminal LeftBrace = Lexemes.singleChar('(');

        public static final Terminal RightBrace = Lexemes.singleChar(')');

        public static final Terminal Dot = Lexemes.singleChar('.');

        public static final Terminal Plus = Lexemes.singleChar('+');

        public static final Terminal Pipe = Lexemes.singleChar('|');

        public static final Terminal QuestionMark = Lexemes.singleChar('?');

        public static final Terminal Star = Lexemes.singleChar('*');

        public static final Terminal ThreePoints = new Literal("..");

        public static final Terminal Char = Lexemes.cCharacter();

        public static final Terminal SingleQuotedString = new QuotedString('\'', '\'', '\\', "\n\r".toCharArray());

    }

}
