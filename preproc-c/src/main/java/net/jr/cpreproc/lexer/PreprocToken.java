package net.jr.cpreproc.lexer;

import net.jr.cpreproc.procs.PreprocessorLine;
import net.jr.lexer.Terminal;
import net.jr.lexer.Token;

public class PreprocToken extends Token {

    private PreprocessorLine preprocessorLine;

    private int startIndex;

    private int endIndex;

    public PreprocToken(Terminal tokenType) {
        super(tokenType, null, null);
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
