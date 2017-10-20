package net.jr.lexer;

public class Token {

    private Lexeme tokenType;

    private int offset;

    private String matchedText;

    public Token(Lexeme tokenType, int offset, String matchedText) {
        this.tokenType = tokenType;
        this.offset = offset;
        this.matchedText = matchedText;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public Lexeme getTokenType() {
        return tokenType;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return tokenType.toString()+"@"+offset;
    }
}
