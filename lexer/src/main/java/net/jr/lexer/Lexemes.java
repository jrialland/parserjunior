package net.jr.lexer;

import net.jr.lexer.impl.*;

import java.util.Map;
import java.util.TreeMap;

import static net.jr.lexer.CharConstraint.Builder.*;

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
            currentState.when(eq(c)).goTo(next);
            currentState = next;
        }
        currentState.when(not(eq('\n'))).goTo(currentState);
        currentState.when(eq('\n')).goTo(builder.newFinalState());
        lexeme.setAutomaton(builder.build());
        return lexeme;
    }

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
     * 0x[0-9A-Fa-f]+
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
     * [0-9]+ DOT [0-9]* or DOT [0-9]+
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
            DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
            init.when(c -> NumbersExceptZero.contains("" + c)).goTo(finalState);
            finalState.when(c -> Numbers.contains("" + c)).goTo(finalState);
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
            init.when(c -> c == '0').goTo(got0);
            got0.when(c -> OctalDigit.contains("" + c)).goTo(finalState);
            finalState.when(c -> OctalDigit.contains("" + c)).goTo(finalState);
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
            init.when(c -> c == '0').goTo(got0);
            got0.when(c -> c == 'B' || c == 'b').goTo(gotB);
            gotB.when(c -> c == '0' || c == '1').goTo(finalState);
            finalState.when(c -> c == '0' || c == '1').goTo(finalState);
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

            init.when(c -> c == '\'').goTo(gotFirstQuote);
            gotFirstQuote.when(c -> c == '\\').goTo(escaped);
            gotFirstQuote.when(c -> c >= 0x20 && c < 127 && c != '\\').goTo(gotChar);
            escaped.when(c -> isCEscapeChar(c)).goTo(gotChar);

            escaped.when(c -> OctalDigit.contains("" + c)).goTo(octalEscape);
            octalEscape.when(c -> OctalDigit.contains("" + c)).goTo(octalEscape2);
            octalEscape.when(c -> c == '\'').goTo(done);
            octalEscape2.when(c -> OctalDigit.contains("" + c)).goTo(gotChar);
            octalEscape2.when(c -> c == '\'').goTo(done);

            escaped.when(c -> c == 'x').goTo(hexEscape);
            escaped.when(c -> HexDigit.contains("" + c)).goTo(hexEscape);
            hexEscape.when(c -> c == '\'').goTo(done);

            escaped.when(c -> c == 'u' || c == 'U').goTo(universalEscape);
            gotHexQuad = addHexQuad(builder, universalEscape);
            gotHexQuad.when(c -> c == '\'').goTo(done);
            gotHexQuad2 = addHexQuad(builder, gotHexQuad);
            gotHexQuad2.when(c -> c == '\'').goTo(done);

            gotChar.when(c -> c == '\'').goTo(done);
            CCharacter.setAutomaton(builder.build());
        }
        return CCharacter;
    }

    private static DefaultAutomaton.Builder.BuilderState addHexQuad(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState origin) {
        DefaultAutomaton.Builder.BuilderState current = origin;
        for (int i = 0; i < 4; i++) {
            DefaultAutomaton.Builder.BuilderState from = current;
            current = builder.newNonFinalState();
            from.when(c -> HexDigit.contains("" + c)).goTo(current);
        }
        return current;
    }

}
