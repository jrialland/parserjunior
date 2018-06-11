package net.jr.lexer;

import net.jr.lexer.basicterminals.*;
import net.jr.lexer.impl.TerminalImpl;

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

    private static final Word cIdentifier = new Word("_" + Alpha, "_" + AlphaNum, "CIdentifier");

    private static final CString cString = new CString();
    private static final Terminal whitespaces = new Word(WhitespacesNonNewLine);
    private static final Terminal lowercaseWord = new Word(LowercaseLetters);
    private static final Map<String, Terminal> Artificials = new TreeMap<>();
    private static final Terminal Eof = artificial("ᵉᵒᶠ");
    private static final Terminal Empty = artificial("ε");
    private static TerminalImpl CHexNumber = new CHexNumber();
    private static TerminalImpl CSimpleFloat = new CFloatingPoint();
    private static TerminalImpl NewLine = new NewLine();
    private static Map<Character, SingleChar> SingleChars = new TreeMap<>();
    private static Map<String, Literal> Literals = new TreeMap<>();
    private static TerminalImpl CInteger = new CInteger();
    private static TerminalImpl COctal = new COctal();
    private static TerminalImpl CBinary = new CBinary();
    private static TerminalImpl CCharacter = new CCharacter();

    static {
        cIdentifier.setName("cIdentifier");
    }

    /**
     * returns a new lexeme that matches nothing, ie a lexeme that will never be encountered.
     * This is useful when using artificial lexeme when dealing with context-aware grammars (for exemple the C typedef-name issue)
     */
    public static Terminal artificial(String name) {
        assert name != null;
        return Artificials.computeIfAbsent(name, k -> {
            Artificial a = new Artificial(k);
            a.setName(k);
            return a;
        });
    }

    /*
     * @return The lexeme for a C identifier
     */
    public static final Terminal cIdentifier() {
        return cIdentifier;
    }

    /**
     * @return The Terminal for a C string (with double-quotes)
     */
    public static final Terminal cString() {
        return cString;
    }

    /**
     * @return a whitespace
     */
    public static final Terminal whitespace() {
        return whitespaces;
    }

    public static final Terminal lowercaseWord() {
        return lowercaseWord;
    }

    /**
     * The "End of File" lexeme, with is always the last lexeme produced by a Lexer.
     * This lexeme is 'artificial'.
     *
     * @return
     */
    public static final Terminal eof() {
        return Eof;
    }

    /**
     * The 'empty' Terminal may be used when referencing the absence of any symbol in a grammar.
     * This lexeme is 'artificial' and may therefore not be emitted by a practical lexer.
     *
     * @return
     */
    public static final Terminal empty() {
        return Empty;
    }

    /**
     * A C++ style comment (which is lineComment("//") by the way)
     *
     * @param commentStart
     * @return
     */
    public static final Terminal lineComment(String commentStart) {
        return new LineComment(commentStart);
    }

    /**
     * A C-style multiline comment
     *
     * @param commentStart comment start ("&#47;*" by example)
     * @param commentEnd   comment end ("*&#47;" by example)
     * @return
     */
    public static final Terminal multilineComment(String commentStart, String commentEnd) {
        return new MultilineComment(commentStart, commentEnd);
    }

    /**
     * '0x'- prefixed hexadecimal number : 0x[0-9A-Fa-f]+
     */
    public static final Terminal cHexNumber() {
        return CHexNumber;
    }

    /**
     * [0-9]+ DOT [0-9]* or DOT [0-9]+ (fF|lL)?
     */
    public static Terminal cFloatingPoint() {
        return CSimpleFloat;
    }

    public static Terminal newLine() {
        return NewLine;
    }

    public static Terminal singleChar(char c) {
        return SingleChars.computeIfAbsent(c, SingleChar::new);
    }

    public static Terminal singleChar(char c, String name) {
        return SingleChars.computeIfAbsent(c, (character) -> {
            SingleChar s = new SingleChar(character);
            s.setName(name);
            return s;
        });
    }

    public static Terminal literal(String s) {
        return Literals.computeIfAbsent(s, c -> new Literal(s));
    }

    public static Terminal literal(String s, String name) {
        return Literals.computeIfAbsent(s, c -> {
            Literal l = new Literal(s);
            l.setName(name);
            return l;
        });
    }

    public static Terminal cInteger() {
        return CInteger;
    }

    public static Terminal cOctal() {
        return COctal;
    }

    /**
     * C-style binary constant  ('0' [bB] [0-1]+)
     *
     * @return
     */
    public static Terminal cBinary() {
        return CBinary;
    }

    public static Terminal cCharacter() {
        return CCharacter;
    }

}
