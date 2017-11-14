package net.jr.lexer;

import net.jr.lexer.impl.*;

import java.util.Map;
import java.util.TreeMap;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

/**
 * Various method to create most common types of Lexemes that one might encounter.
 */
public class Lexemes {

    public static final String LowercaseLetters = "abcdefghiklmnopqrstuvwxyz";

    public static final String UppercaseLetters = LowercaseLetters.toUpperCase();

    public static final String Zero = "0";

    public static final String NumbersExceptZero = "123456789";

    public static final String Numbers = Zero + NumbersExceptZero;

    public static final String OctalDigit = "01234567";

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

    private static final Map<String, Lexeme> Artificials = new TreeMap<>();

    /**
     * returns a new lexeme that matches nothing, ie a lexeme that will never be encountered.
     * This is useful when using artificial lexeme when dealing with context-aware grammars (for exemple the C typedef-name issue)
     */
    public static Lexeme artificial(String name) {
        assert name != null;
        return Artificials.computeIfAbsent(name, k -> {
            final LexemeImpl l = new LexemeImpl() {
                @Override
                public String toString() {
                    return name;
                }
            };
            l.setAutomaton(failAutomaton(l));
            return l;
        });
    }

    private static final Lexeme Eof = artificial("ᵉᵒᶠ");

    private static final Lexeme Empty = artificial("ε");

    /*
     * @return The lexeme for a C identifier
     */
    public static final Lexeme cIdentifier() {
        return cIdentifier;
    }

    /**
     * @return The Lexeme for a C string (with double-quotes)
     */
    public static final Lexeme cString() {
        return cString;
    }

    /**
     * @return a whitespace
     */
    public static final Lexeme whitespace() {
        return whitespaces;
    }

    public static final Lexeme lowercaseWord() {
        return lowercaseWord;
    }

    /**
     * The "End of File" lexeme, with is always the last lexeme produced by a Lexer.
     * This lexeme is 'artificial'.
     *
     * @return
     */
    public static final Lexeme eof() {
        return Eof;
    }


    /**
     * The 'empty' Lexeme may be used when referencing the absence of any symbol in a grammar.
     * This lexeme is 'artificial' and may therefore not be emitted by a practical lexer.
     *
     * @return
     */
    public static final Lexeme empty() {
        return Empty;
    }

    /**
     * A C++ style comment (which is lineComment("//") by the way)
     *
     * @param commentStart
     * @return
     */
    public static final Lexeme lineComment(String commentStart) {
        LexemeImpl lexeme = new LexemeImpl();
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(lexeme);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(eq(c)).goTo(next);
            currentState = next;
        }
        currentState.when(not(eq('\n'))).goTo(currentState);
        currentState.when(eq('\n')).goTo(builder.newFinalState());
        lexeme.setAutomaton(builder.build());
        return lexeme;
    }

    /**
     * A C-style multiline comment
     *
     * @param commentStart comment start ("&#47;*" by example)
     * @param commentEnd   comment end ("*&#47;" by example)
     * @return
     */
    public static final Lexeme multilineComment(String commentStart, String commentEnd) {
        LexemeImpl lexeme = new LexemeImpl();
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(lexeme);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(eq(c)).goTo(next);
            currentState = next;
        }
        DefaultAutomaton.Builder.BuilderState inComment = currentState;
        char[] end = commentEnd.toCharArray();
        for (int i = 0; i < end.length; i++) {
            final char c = end[i];
            DefaultAutomaton.Builder.BuilderState nextState = i == end.length - 1 ? builder.newFinalState() : builder.newNonFinalState();
            currentState.when(eq(c)).goTo(nextState);
            currentState.when(not(eq(c))).goTo(inComment);
            currentState = nextState;
        }
        lexeme.setAutomaton(builder.build());
        return lexeme;
    }

    private static LexemeImpl CHexNumber = null;

    /**
     * '0x'- prefixed hexadecimal number : 0x[0-9A-Fa-f]+
     */
    public static final Lexeme cHexNumber() {
        if (CHexNumber == null) {

            CHexNumber = new LexemeImpl() {

                @Override
                public int getPriority() {
                    return 1;
                }

                public String toString() {
                    return "cHexNumber";
                }

            };

            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(CHexNumber);
            DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
            DefaultAutomaton.Builder.BuilderState nextState;

            nextState = builder.newNonFinalState();
            currentState.when(eq('0')).goTo(nextState);
            currentState = nextState;

            nextState = builder.newNonFinalState();
            currentState.when(eq('x')).goTo(nextState);
            currentState = nextState;

            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            currentState.when(inList(HexDigit)).goTo(finalState);
            finalState.when(inList(HexDigit)).goTo(finalState);
            addIntegerSuffix(builder, finalState);
            CHexNumber.setAutomaton(builder.build());

        }
        return CHexNumber;
    }

    private static void addIntegerSuffix(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState state) {
        DefaultAutomaton.Builder.BuilderState suffixU = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState suffixL = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        state.when(or(eq('U'), eq('u'))).goTo(suffixU);
        suffixU.when(or(eq('L'), eq('l'))).goTo(finalState);
        state.when(or(eq('L'), eq('l'))).goTo(suffixL);
        suffixL.when(or(eq('U'), eq('u'))).goTo(finalState);
    }


    private static LexemeImpl CSimpleFloat;

    /**
     * [0-9]+ DOT [0-9]* or DOT [0-9]+ (fF|lL)?
     */
    public static Lexeme cFloatingPoint() {
        if (CSimpleFloat == null) {
            CSimpleFloat = new LexemeImpl() {

                @Override
                public int getPriority() {
                    return 1;
                }

                @Override
                public String toString() {
                    return "cFloatingPoint";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(CSimpleFloat);
            DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
            DefaultAutomaton.Builder.BuilderState beforeDot = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            DefaultAutomaton.Builder.BuilderState gotSuffix = builder.newFinalState();
            DefaultAutomaton.Builder.BuilderState gotExponent;
            DefaultAutomaton.Builder.BuilderState nonFinalDot = builder.newNonFinalState();

            // [0-9]+ DOT [0-9]*
            initialState.when(inList(Numbers)).goTo(beforeDot);
            beforeDot.when(inList(Numbers)).goTo(beforeDot);
            beforeDot.when(eq('.')).goTo(finalState);
            finalState.when(inList(Numbers)).goTo(finalState);

            // DOT [0-9]+
            initialState.when(eq('.')).goTo(nonFinalDot);
            nonFinalDot.when(inList(Numbers)).goTo(finalState);

            gotExponent = addExponent(builder, finalState);
            gotExponent.when(inList("lfLF")).goTo(builder.newFinalState());

            finalState.when(inList("lfLF")).goTo(gotSuffix);
            addExponent(builder, gotSuffix);

            CSimpleFloat.setAutomaton(builder.build());

        }
        return CSimpleFloat;
    }

    private static DefaultAutomaton.Builder.BuilderState addExponent(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState state) {
        DefaultAutomaton.Builder.BuilderState gotExp = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState gotSign = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        state.when(or(eq('E'), eq('e'))).goTo(gotExp);
        gotExp.when(inList(NumbersExceptZero)).goTo(finalState);
        gotExp.when(or(eq('+'), eq('-'))).goTo(gotSign);
        gotSign.when(inList(NumbersExceptZero)).goTo(finalState);
        finalState.when(inList(Numbers)).goTo(finalState);
        return finalState;
    }

    private static LexemeImpl NewLine;

    public static Lexeme newLine() {
        if (NewLine == null) {
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
            init.when(eq('\r')).goTo(gotCR);
            gotCR.when(eq('\n')).goTo(finalState);
            init.when(eq('\n')).goTo(finalState);
            NewLine.setAutomaton(builder.build());
        }
        return NewLine;
    }

    private static Map<Character, SingleChar> SingleChars = new TreeMap<>();

    public static Lexeme singleChar(char c) {
        return SingleChars.computeIfAbsent(c, SingleChar::new);
    }

    private static Map<String, Literal> Literals = new TreeMap<>();

    public static Lexeme literal(String s) {
        return Literals.computeIfAbsent(s, c -> new Literal(s));
    }

    private static LexemeImpl CInteger;

    public static Lexeme cInteger() {
        if (CInteger == null) {
            CInteger = new LexemeImpl() {
                @Override
                public String toString() {
                    return "cInteger";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(CInteger);
            DefaultAutomaton.Builder.BuilderState init = builder.initialState();
            DefaultAutomaton.Builder.BuilderState got0 = builder.newFinalState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();

            init.when(eq('0')).goTo(got0);

            init.when(inList(NumbersExceptZero)).goTo(finalState);
            finalState.when(inList(Numbers)).goTo(finalState);

            addIntegerSuffix(builder, finalState);
            CInteger.setAutomaton(builder.build());
        }
        return CInteger;
    }

    private static LexemeImpl COctal;

    public static Lexeme cOctal() {
        if (COctal == null) {
            COctal = new LexemeImpl() {
                @Override
                public String toString() {
                    return "cOctal";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(COctal);
            DefaultAutomaton.Builder.BuilderState init = builder.initialState();
            DefaultAutomaton.Builder.BuilderState got0 = builder.initialState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            init.when(eq('0')).goTo(got0);
            got0.when(inList(OctalDigit)).goTo(finalState);
            finalState.when(inList(OctalDigit)).goTo(finalState);
            addIntegerSuffix(builder, finalState);
            COctal.setAutomaton(builder.build());
        }
        return COctal;
    }

    private static LexemeImpl CBinary;

    /**
     * C-style binary constant  ('0' [bB] [0-1]+)
     *
     * @return
     */
    public static Lexeme cBinary() {
        if (CBinary == null) {
            CBinary = new LexemeImpl() {
                @Override
                public String toString() {
                    return "cBinary";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(CBinary);
            DefaultAutomaton.Builder.BuilderState init = builder.initialState();
            DefaultAutomaton.Builder.BuilderState got0 = builder.initialState();
            DefaultAutomaton.Builder.BuilderState gotB = builder.initialState();
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            init.when(eq('0')).goTo(got0);
            got0.when(or(eq('B'), eq('b'))).goTo(gotB);
            gotB.when(or(eq('0'), eq('1'))).goTo(finalState);
            finalState.when(or(eq('0'), eq('1'))).goTo(finalState);
            CBinary.setAutomaton(builder.build());
        }
        return CBinary;
    }

    private static LexemeImpl CCharacter;

    private static boolean isCEscapeChar(char c) {
        return "\"?abfnrtv\\".contains("" + c);
    }

    public static Lexeme cCharacter() {
        if (CCharacter == null) {
            CCharacter = new LexemeImpl() {
                @Override
                public String toString() {
                    return "cCharacter";
                }
            };
            DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(CCharacter);
            DefaultAutomaton.Builder.BuilderState init = builder.initialState();
            DefaultAutomaton.Builder.BuilderState gotFirstQuote = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState escaped = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState octalEscape = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState hexEscape = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState octalEscape2 = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState gotChar = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState universalEscape = builder.newNonFinalState();
            DefaultAutomaton.Builder.BuilderState gotHexQuad;
            DefaultAutomaton.Builder.BuilderState gotHexQuad2;
            DefaultAutomaton.Builder.BuilderState done = builder.newFinalState();

            init.when(eq('\'')).goTo(gotFirstQuote);
            gotFirstQuote.when(eq('\\')).goTo(escaped);
            gotFirstQuote.when(and(inRange(0x20, 128), not(eq('\\')))).goTo(gotChar);
            escaped.when(inList("\"?abfnrtv\\")).goTo(gotChar);

            escaped.when(inList(OctalDigit)).goTo(octalEscape);
            octalEscape.when(inList(OctalDigit)).goTo(octalEscape2);
            octalEscape.when(eq('\'')).goTo(done);
            octalEscape2.when(inList(OctalDigit)).goTo(gotChar);
            octalEscape2.when(eq('\'')).goTo(done);

            escaped.when(eq('x')).goTo(hexEscape);
            escaped.when(inList(HexDigit)).goTo(hexEscape);
            hexEscape.when(eq('\'')).goTo(done);

            escaped.when(or(eq('u'), eq('U'))).goTo(universalEscape);
            gotHexQuad = addHexQuad(builder, universalEscape);
            gotHexQuad.when(eq('\'')).goTo(done);
            gotHexQuad2 = addHexQuad(builder, gotHexQuad);
            gotHexQuad2.when(eq('\'')).goTo(done);

            gotChar.when(eq('\'')).goTo(done);
            CCharacter.setAutomaton(builder.build());
        }
        return CCharacter;
    }

    private static DefaultAutomaton.Builder.BuilderState addHexQuad(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState origin) {
        DefaultAutomaton.Builder.BuilderState current = origin;
        for (int i = 0; i < 4; i++) {
            DefaultAutomaton.Builder.BuilderState from = current;
            current = builder.newNonFinalState();
            from.when(CharConstraint.Builder.inList(HexDigit)).goTo(current);
        }
        return current;
    }

    static Automaton failAutomaton(final Lexeme lexeme) {
        return FailAutomaton.get(lexeme);
    }

}
