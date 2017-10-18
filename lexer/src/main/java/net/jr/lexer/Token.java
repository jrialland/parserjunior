package net.jr.lexer;

public class Token {

    private Lexeme tokenType;

    private int position;

    private String matchedText;

    public Token(Lexeme tokenType, int position, String matchedText) {
        this.tokenType = tokenType;
        this.position = position;
        this.matchedText = matchedText;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public Lexeme getTokenType() {
        return tokenType;
    }

}
