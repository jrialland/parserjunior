package net.jr.lexer;

import net.jr.lexer.impl.QuotedString;
import net.jr.lexer.impl.Word;

public class CommonTokenTypes {
    public static final String LowercaseLetters = "abcdefghiklmnopqrstuvwxyz";

    public static final String UppercaseLetters = LowercaseLetters.toUpperCase();

    public static final String Zero = "0";

    public static final String NumbersExceptZero = "123456789";

    public static final String Numbers = Zero + NumbersExceptZero;

    public static final String Alpha = LowercaseLetters + UppercaseLetters;

    public static final String AlphaNum = Alpha + Numbers;

    public static final String WhitespacesNonNewLine = " \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r";

    private static final Lexeme cIdentifier = new Word("_" + Alpha, "_" + AlphaNum);

    private static final Lexeme whitespaces = new Word(WhitespacesNonNewLine);

    private static final Lexeme lowercaseWord = new Word(LowercaseLetters);

    private static final Lexeme cString = new QuotedString('\"', '\"', '\\', new char[]{'\r', '\n'});

    public static final Lexeme cIdentifier() {
        return cIdentifier;
    }

    public static final Lexeme cString() {
        return cString;
    }

    public static final Lexeme whitespace() {
        return whitespaces;
    }

    public static final Lexeme number() {
        return new Word(NumbersExceptZero, Numbers);
    }

    public static final Lexeme lowercaseWord() {
        return lowercaseWord;
    }

    public static final Lexeme eof() {
        return Lexeme.Eof;
    }

}
