package net.jr.lexer.expr.impl;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.Token;
import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.QuotedString;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;
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

    public static final class Tokens {

        public static final Lexeme LeftBrace = Lexemes.singleChar('(');

        public static final Lexeme RightBrace = Lexemes.singleChar(')');

        public static final Lexeme Dot = Lexemes.singleChar('.');

        public static final Lexeme Plus = Lexemes.singleChar('+');

        public static final Lexeme Pipe = Lexemes.singleChar('|');

        public static final Lexeme QuestionMark = Lexemes.singleChar('?');

        public static final Lexeme Star = Lexemes.singleChar('*');

        public static final Lexeme ThreePoints = new Literal("..");

        public static final Lexeme Char = Lexemes.cCharacter();

        public static final Lexeme SingleQuotedString = new QuotedString('\'', '\'', '\\', "\n\r".toCharArray());

    }

    public static final Forward OneOrMoreExpr = new Forward("OneOrMoreExpr");

    public static final Forward Regex = new Forward("Regex");

    public static final Forward Expr = new Forward("Expr");

    public static final Forward Sequence = new Forward("Sequence");

    public static final Forward CharacterRange = new Forward("CharacterRange");

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

}
