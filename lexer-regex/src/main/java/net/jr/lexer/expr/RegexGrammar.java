package net.jr.lexer.expr;

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
import net.jr.parser.ast.AstNode;

import java.util.List;

/**
 * Character sequence must be enclosed in '' quotes. For example, this lexer rule matches salutation 'Hello': <code>'hello'</code>
 * <p>
 * Use '|' to express alternatives. Lexer rule matching either salutation 'Hello' or 'Bye': <code>'Hello'|'Bye'</code>
 * <p>
 * The dot '.' represents any character
 * <p>
 * Use '..' to express interval. Lexer rule matching either 0 or 1 or 2 or 3 or 4 or 5 or 6 or 7 or 8 or 9: '0'..'9'
 * <p>
 * Use '+' to express repeated one or more times. Lexer rule matching any integer : ('0'..'9')+
 * <p>
 * Use '~' to express inverse of a set ~('0'..'9')
 * <p>
 * Use '{' and '}' to express the number of allowed repetitions :
 * 'a'{3}  => matches aaa
 * 'b'{2,4} matches bb, bbb and bbbb.
 */
public class RegexGrammar extends Grammar {

    public static final class Tokens {

        public static final Lexeme LeftBrace = Lexemes.singleChar('(');

        public static final Lexeme RightBrace = Lexemes.singleChar(')');

        public static final Lexeme LeftCurlyBrace = Lexemes.singleChar('{');

        public static final Lexeme RightCurlyBrace = Lexemes.singleChar('}');

        public static final Lexeme Number = Lexemes.cInteger();

        public static final Lexeme Dot = Lexemes.singleChar('.');

        public static final Lexeme Plus = Lexemes.singleChar('+');

        public static final Lexeme Comma = Lexemes.singleChar(',');

        public static final Lexeme Pipe = Lexemes.singleChar('|');

        public static final Lexeme QuestionMark = Lexemes.singleChar('?');

        public static final Lexeme Star = Lexemes.singleChar('*');

        public static final Lexeme ThreePoints = new Literal("...");

        public static final Lexeme Char = Lexemes.cCharacter();

        public static final Lexeme SingleQuotedString = new QuotedString('\'', '\'', '\\', "\n\r".toCharArray());

    }

    public static final Forward Regex = new Forward("Regex");

    public static final Forward Expr = new Forward("Expr");

    public static final Forward Sequence = new Forward("Sequence");

    public static final Forward CharacterRange = new Forward("CharacterRange");

    private Lexer lexer;

    public RegexGrammar() {

        setTargetRule(addRule(Regex, oneOrMore(Expr)).withName("Regex").get());

        addRule(Expr, Tokens.LeftBrace, Expr, Tokens.RightBrace).withName("Group");

        addRule(Expr, Sequence).withName("CharSequence");

        addRule(Expr, CharacterRange).withName("Range");

        addRule(Expr, Tokens.Char).withName("Char");

        addRule(Expr, Tokens.Dot).withName("AnyChar");

        addRule(Expr, Expr, Tokens.QuestionMark).withName("Optional");

        addRule(Expr, Expr, Tokens.Star).withName("ZeroOrMore");

        addRule(Expr, Expr, Tokens.Plus).withName("OneOrMore");

        addRule(Expr, Expr, Tokens.Pipe, Expr).withName("Or");

        addRule(Expr, Expr, Tokens.LeftCurlyBrace, Tokens.Number, Tokens.RightCurlyBrace).withName("Repetition");

        addRule(Expr, Expr, Tokens.LeftCurlyBrace, Tokens.Number, Tokens.Comma, Tokens.Number, Tokens.RightCurlyBrace)
                .withName("RepetitionWithBounds")
                .withAction(parsingContext -> {
                    List<AstNode> bounds = parsingContext.getAstNode().getChildrenOfType(Tokens.Number);
                    int lowerBound = Integer.parseInt(bounds.get(0).asToken().getText());
                    int upperBound = Integer.parseInt(bounds.get(1).asToken().getText());
                    if (lowerBound >= upperBound) {
                        throw new IllegalStateException("Lower bound must be smaller than upper bound");
                    }
                });

        target(CharacterRange)
                .def(Tokens.Char, Tokens.ThreePoints, Tokens.Char);

        target(Sequence)
                .def(Tokens.SingleQuotedString);

        lexer = Lexer.forLexemes(getTerminals());
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
