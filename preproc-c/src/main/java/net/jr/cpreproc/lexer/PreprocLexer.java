package net.jr.cpreproc.lexer;

import net.jr.common.Position;
import net.jr.cpreproc.procs.PreprocessorLine;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PreprocLexer {

    public static class TokenType {
        public static Terminal NoMeaning = Lexemes.artificial("NoMeaning");
        public static Terminal Comma = Lexemes.singleChar(',');
        public static Terminal LeftParen = Lexemes.singleChar('(');
        public static Terminal RightParen = Lexemes.singleChar(')');
        public static Terminal StringLiteral = Lexemes.cString();
        public static Terminal WhiteSpace = Lexemes.whitespace();
        public static Terminal Identifier = Lexemes.cIdentifier();
        public static Terminal ConcatOperator = Lexemes.literal("##", "ConcatOperator");
        public static Terminal StringifyOperator = Lexemes.singleChar('#', "StringifyOperator");
    }

    private enum State {
        Default,
        StringLiteral,
        StringEscape,
        WhiteSpace,
        Identifier,
        SharpOperator
    }

    public static List<PreprocToken> tokenize(String txt) {
        return tokenize(new PreprocessorLine(Position.start(), txt));
    }

    public static List<PreprocToken> tokenize(PreprocessorLine line) {
        List<PreprocToken> tokens = new ArrayList<>();
        tokenize(line, t -> tokens.add(t));
        return tokens;
    }

    public static void tokenize(PreprocessorLine line, Consumer<PreprocToken> consumer) {
        State state = State.Default;
        String txt = line.getText();
        char[] chars = txt.toCharArray();
        int startIndex = 0, i;
        for (i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {
                case Default:
                    switch (c) {
                        case '"':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            state = State.StringLiteral;
                            break;
                        case '\u00a0':
                        case '\t':
                        case ' ':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            state = State.WhiteSpace;
                            break;
                        case '#':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            state = State.SharpOperator;
                            break;
                        case '(':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, TokenType.LeftParen, consumer);
                            break;
                        case ')':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, TokenType.RightParen, consumer);
                            break;
                        case ',':
                            startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, TokenType.Comma, consumer);
                            break;
                        default:
                            if (isIdentifierStart(c)) {
                                startIndex = push(line, startIndex, i, TokenType.NoMeaning, consumer);
                                state = State.Identifier;
                            }
                            break;
                    }
                    break;
                case StringLiteral:
                    switch (c) {
                        case '\\':
                            state = State.StringEscape;
                            break;
                        case '"':
                            startIndex = push(line, startIndex, i + 1, TokenType.StringLiteral, consumer);
                            state = State.Default;
                            break;
                    }
                    break;
                case StringEscape:
                    state = State.StringLiteral;
                    break;
                case WhiteSpace:
                    switch (c) {
                        case '\u00a0':
                        case '\t':
                        case ' ':
                            break;
                        default:
                            startIndex = push(line, startIndex, i, TokenType.WhiteSpace, consumer);
                            state = State.Default;
                            i--;
                            break;
                    }
                    break;
                case Identifier:
                    if (!isIdentifierChar(c)) {
                        startIndex = push(line, startIndex, i, TokenType.Identifier, consumer);
                        state = State.Default;
                        i--;
                    }
                    break;
                case SharpOperator:
                    switch (c) {
                        case '#':
                            //double sharp = concat operator(remove whitespaces)
                            startIndex = push(line, startIndex, i + 1, TokenType.ConcatOperator, consumer);
                            state = State.Default;
                            break;
                        default:
                            startIndex = push(line, startIndex, i, TokenType.StringifyOperator, consumer);
                            state = State.Default;
                            i--;
                            break;
                    }
                    break;
            }
        }

        if (startIndex < i) {
            if (state == State.Identifier) {
                push(line, startIndex, i, TokenType.Identifier, consumer);
            } else if (state == State.WhiteSpace) {
                push(line, startIndex, i, TokenType.WhiteSpace, consumer);
            } else {
                char c = txt.charAt(i - 1);
                switch (c) {
                    case '\u00a0':
                    case '\t':
                    case ' ':
                        push(line, startIndex, i - 1, TokenType.NoMeaning, consumer);
                        push(line, i - 1, i, TokenType.WhiteSpace, consumer);
                    case '(':
                        push(line, startIndex, i - 1, TokenType.NoMeaning, consumer);
                        push(line, i - 1, i, TokenType.LeftParen, consumer);
                        break;
                    case ')':
                        push(line, startIndex, i - 1, TokenType.NoMeaning, consumer);
                        push(line, i - 1, i, TokenType.RightParen, consumer);
                        break;
                    case ',':
                        push(line, startIndex, i - 1, TokenType.NoMeaning, consumer);
                        push(line, i - 1, i, TokenType.Comma, consumer);
                        break;
                    case '#':
                        push(line, startIndex, i - 1, TokenType.NoMeaning, consumer);
                        push(line, i - 1, i, TokenType.StringifyOperator, consumer);
                        break;
                    default:
                        push(line, startIndex, i, TokenType.NoMeaning, consumer);
                }
            }
        }
    }

    private static int push(PreprocessorLine line, int startIndex, int endIndex, Terminal tokenType, Consumer<PreprocToken> consumer) {
        if (startIndex < endIndex) {
            PreprocToken t = new PreprocToken(tokenType, line, startIndex, endIndex);
            consumer.accept(t);
        }
        return endIndex;
    }

    private static boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
    }

    private static boolean isIdentifierChar(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9');
    }

}
