package net.jr.cpreproc.lexer;

import net.jr.common.Position;
import net.jr.cpreproc.procs.PreprocessorLine;
import net.jr.lexer.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PreprocLexer {


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
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            state = State.StringLiteral;
                            break;
                        case '\u00a0':
                        case '\t':
                        case ' ':
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            state = State.WhiteSpace;
                            break;
                        case '#':
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            state = State.SharpOperator;
                            break;
                        case '(':
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, PreprocToken.LeftParen, consumer);
                            break;
                        case ')':
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, PreprocToken.RightParen, consumer);
                            break;
                        case ',':
                            startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
                            startIndex = push(line, startIndex, i + 1, PreprocToken.Comma, consumer);
                            break;
                        default:
                            if (isIdentifierStart(c)) {
                                startIndex = push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
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
                            startIndex = push(line, startIndex, i + 1, PreprocToken.StringLiteral, consumer);
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
                            startIndex = push(line, startIndex, i, PreprocToken.WhiteSpace, consumer);
                            state = State.Default;
                            i--;
                            break;
                    }
                    break;
                case Identifier:
                    if (!isIdentifierChar(c)) {
                        startIndex = push(line, startIndex, i, PreprocToken.Identifier, consumer);
                        state = State.Default;
                        i--;
                    }
                    break;
                case SharpOperator:
                    switch (c) {
                        case '#':
                            //double sharp = concat operator(remove whitespaces)
                            startIndex = push(line, startIndex, i + 1, PreprocToken.ConcatOperator, consumer);
                            state = State.Default;
                            break;
                        default:
                            startIndex = push(line, startIndex, i, PreprocToken.StringifyOperator, consumer);
                            state = State.Default;
                            i--;
                            break;
                    }
                    break;
            }
        }

        if (startIndex < i) {
            if (state == State.Identifier) {
                push(line, startIndex, i, PreprocToken.Identifier, consumer);
            } else if (state == State.WhiteSpace) {
                push(line, startIndex, i, PreprocToken.WhiteSpace, consumer);
            } else {
                char c = txt.charAt(i - 1);
                switch (c) {
                    case '\u00a0':
                    case '\t':
                    case ' ':
                        push(line, startIndex, i - 1, PreprocToken.NoMeaning, consumer);
                        push(line, i - 1, i, PreprocToken.WhiteSpace, consumer);
                    case '(':
                        push(line, startIndex, i - 1, PreprocToken.NoMeaning, consumer);
                        push(line, i - 1, i, PreprocToken.LeftParen, consumer);
                        break;
                    case ')':
                        push(line, startIndex, i - 1, PreprocToken.NoMeaning, consumer);
                        push(line, i - 1, i, PreprocToken.RightParen, consumer);
                        break;
                    case ',':
                        push(line, startIndex, i - 1, PreprocToken.NoMeaning, consumer);
                        push(line, i - 1, i, PreprocToken.Comma, consumer);
                        break;
                    case '#':
                        push(line, startIndex, i - 1, PreprocToken.NoMeaning, consumer);
                        push(line, i - 1, i, PreprocToken.StringifyOperator, consumer);
                        break;
                    default:
                        push(line, startIndex, i, PreprocToken.NoMeaning, consumer);
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

    private enum State {
        Default,
        StringLiteral,
        StringEscape,
        WhiteSpace,
        Identifier,
        SharpOperator
    }

}
