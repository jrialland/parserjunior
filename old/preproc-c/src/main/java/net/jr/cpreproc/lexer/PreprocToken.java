package net.jr.cpreproc.lexer;

import net.jr.common.Position;
import net.jr.cpreproc.procs.PreprocessorLine;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.lexer.Token;

public class PreprocToken extends Token {

    public static final Terminal NoMeaning = Lexemes.artificial("NoMeaning");
    public static final Terminal Comma = Lexemes.singleChar(',');
    public static final Terminal LeftParen = Lexemes.singleChar('(');
    public static final Terminal RightParen = Lexemes.singleChar(')');
    public static final Terminal StringLiteral = Lexemes.cString();
    public static final Terminal WhiteSpace = Lexemes.whitespace();
    public static final Terminal Identifier = Lexemes.cIdentifier();
    public static final Terminal ConcatOperator = Lexemes.literal("##", "ConcatOperator");
    public static final Terminal StringifyOperator = Lexemes.singleChar('#', "StringifyOperator");

    private PreprocessorLine preprocessorLine;

    private int startIndex;

    private int endIndex;

    public PreprocToken(Terminal tokenType, String txt) {
        this(tokenType, new PreprocessorLine(Position.unknown(), txt), 0, txt.length());
    }

    public PreprocToken(Terminal tokenType, PreprocessorLine line, int startIndex, int endIndex) {
        super(tokenType, line.getPosition(startIndex), null);
        this.preprocessorLine = line;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public String getText() {
        return preprocessorLine.getText().substring(startIndex, endIndex);
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
