package net.jr.lexer;

import net.jr.lexer.impl.DefaultAutomaton;
import net.jr.lexer.impl.LexemeImpl;
import net.jr.lexer.impl.QuotedString;
import net.jr.lexer.impl.Word;

public class Lexemes {

    public static final String LowercaseLetters = "abcdefghiklmnopqrstuvwxyz";

    public static final String UppercaseLetters = LowercaseLetters.toUpperCase();

    public static final String Zero = "0";

    public static final String NumbersExceptZero = "123456789";

    public static final String Numbers = Zero + NumbersExceptZero;

    public static final String Alpha = LowercaseLetters + UppercaseLetters;

    public static final String AlphaNum = Alpha + Numbers;

    public static final String WhitespacesNonNewLine = " \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r";

    public static final String HexDigit = Numbers + "abcdef" + "ABCDEF";

    private static final Lexeme cIdentifier = new Word("_" + Alpha, "_" + AlphaNum, "CIdentifier");

    private static final Lexeme whitespaces = new Word(WhitespacesNonNewLine);

    private static final Lexeme lowercaseWord = new Word(LowercaseLetters);

    private static final Lexeme cString = new QuotedString('\"', '\"', '\\', new char[]{'\r', '\n'}) {
        @Override
        public String toString() {
            return "CString";
        }
    };

    private static final Lexeme Eof = new Lexeme() {

        @Override
        public String toString() {
            return "ᵉᵒᶠ";
        }
    };

    public static final Lexeme cIdentifier() {
        return cIdentifier;
    }

    public static final Lexeme cString() {
        return cString;
    }

    public static final Lexeme whitespace() {
        return whitespaces;
    }

    private static Lexeme Number = null;

    public static final Lexeme number() {
        if (Number == null) {
            Number = new Word(NumbersExceptZero, Numbers) {
                @Override
                public String toString() {
                    return "Number";
                }
            };
        }
        return Number;
    }

    public static final Lexeme lowercaseWord() {
        return lowercaseWord;
    }

    public static final Lexeme eof() {
        return Eof;
    }

    public static final Lexeme lineComment(String commentStart) {
        LexemeImpl lexeme = new LexemeImpl();
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(lexeme);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(ch -> ch == c).goTo(next);
            currentState = next;
        }
        currentState.when(c -> c != '\n').goTo(currentState);
        currentState.when(c -> c == '\n').goTo(builder.newFinalState());
        lexeme.setAutomaton(builder.build());
        return lexeme;
    }

    public static final Lexeme multilineComment(String commentStart, String commentEnd) {
        LexemeImpl lexeme = new LexemeImpl();
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(lexeme);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(ch -> ch == c).goTo(next);
            currentState = next;
        }
        DefaultAutomaton.Builder.BuilderState inComment = currentState;
        char[] end = commentEnd.toCharArray();
        for (int i = 0; i < end.length; i++) {
            final char c = end[i];
            DefaultAutomaton.Builder.BuilderState nextState = i == end.length - 1 ? builder.newFinalState() : builder.newNonFinalState();
            currentState.when(ch -> ch == c).goTo(nextState);
            currentState.when(ch -> ch != c).goTo(inComment);
            currentState = nextState;
        }
        lexeme.setAutomaton(builder.build());
        return lexeme;
    }

    private static LexemeImpl HexNumber = null;

    /**
     * 0x[0-9A-Fa-f]+
     */
    public static final Lexeme hexNumber() {
        if (HexNumber == null) {

            HexNumber = new LexemeImpl() {

                @Override
                public int getPriority() {
                    return 1;
                }

                public String toString() {
                    return "HexNumber";
                }

            };

            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(HexNumber);
            DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
            DefaultAutomaton.Builder.BuilderState nextState;

            nextState = builder.newNonFinalState();
            currentState.when(c -> c == '0').goTo(nextState);
            currentState = nextState;

            nextState = builder.newNonFinalState();
            currentState.when(c -> c == 'x').goTo(nextState);
            currentState = nextState;

            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            currentState.when(c -> HexDigit.contains("" + c)).goTo(finalState);
            finalState.when(c -> HexDigit.contains("" + c)).goTo(finalState);
            HexNumber.setAutomaton(builder.build());

        }
        return HexNumber;
    }

    private static LexemeImpl SimpleFloat;

    /**
     * [0-9]+ DOT [0-9]* or DOT [0-9]+
     */
    public static Lexeme simpleFloat() {
        if (SimpleFloat == null) {
            SimpleFloat = new LexemeImpl() {

                @Override
                public int getPriority() {
                    return 1;
                }

                @Override
                public String toString() {
                    return "Float";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(SimpleFloat);
            DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
            DefaultAutomaton.Builder.BuilderState beforeDot = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            DefaultAutomaton.Builder.BuilderState nonFinalDot = builder.newNonFinalState();

            // [0-9]+ DOT [0-9]*
            initialState.when(Character::isDigit).goTo(beforeDot);
            beforeDot.when(Character::isDigit).goTo(beforeDot);
            beforeDot.when(c -> c == '.').goTo(finalState);
            finalState.when(Character::isDigit).goTo(finalState);

            // DOT [0-9]+
            initialState.when(c -> c == '.').goTo(nonFinalDot);
            nonFinalDot.when(Character::isDigit).goTo(finalState);


            SimpleFloat.setAutomaton(builder.build());

        }
        return SimpleFloat;
    }

    private static LexemeImpl NewLine;

    public static Lexeme newLine() {
        if(NewLine == null) {
            NewLine = new LexemeImpl() {
                @Override
                public String toString() {
                    return "NewLine";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(NewLine);
            DefaultAutomaton.Builder.BuilderState init = builder.initialState();
            DefaultAutomaton.Builder.BuilderState gotCR = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            init.when(c -> c=='\r').goTo(gotCR);
            gotCR.when(c-> c=='\n').goTo(finalState);
            init.when(c -> c=='\n').goTo(finalState);
            NewLine.setAutomaton(builder.build());
        }
        return NewLine;
    }
}
