package net.jr.lexer;

import net.jr.lexer.impl.*;
import net.jr.lexer.basiclexemes.*;

import java.util.Map;
import java.util.TreeMap;

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

    private static final Lexeme cString = new CString();

    private static final Map<String, Lexeme> Artificials = new TreeMap<>();

    /**
     * returns a new lexeme that matches nothing, ie a lexeme that will never be encountered.
     * This is useful when using artificial lexeme when dealing with context-aware grammars (for exemple the C typedef-name issue)
     */
    public static Lexeme artificial(String name) {
        assert name != null;
        return Artificials.computeIfAbsent(name, k -> new Artificial(k));
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
        return new LineComment(commentStart);
    }

    /**
     * A C-style multiline comment
     *
     * @param commentStart comment start ("&#47;*" by example)
     * @param commentEnd   comment end ("*&#47;" by example)
     * @return
     */
    public static final Lexeme multilineComment(String commentStart, String commentEnd) {
        return new MultilineComment(commentStart, commentEnd);
    }

    private static LexemeImpl CHexNumber = new CHexNumber();

    /**
     * '0x'- prefixed hexadecimal number : 0x[0-9A-Fa-f]+
     */
    public static final Lexeme cHexNumber() {
        return CHexNumber;
    }

    private static LexemeImpl CSimpleFloat = new CFloatingPoint();

    /**
     * [0-9]+ DOT [0-9]* or DOT [0-9]+ (fF|lL)?
     */
    public static Lexeme cFloatingPoint() {
        return CSimpleFloat;
    }

    private static LexemeImpl NewLine = new NewLine();

    public static Lexeme newLine() {
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

    private static LexemeImpl CInteger = new CInteger();

    public static Lexeme cInteger() {
        return CInteger;
    }

    private static LexemeImpl COctal = new COctal();

    public static Lexeme cOctal() {
        return COctal;
    }

    private static LexemeImpl CBinary = new CBinary();

    /**
     * C-style binary constant  ('0' [bB] [0-1]+)
     *
     * @return
     */
    public static Lexeme cBinary() {
        return CBinary;
    }

    private static LexemeImpl CCharacter = new CCharacter();

    public static Lexeme cCharacter() {
        return CCharacter;
    }

}
