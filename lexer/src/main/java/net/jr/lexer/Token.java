package net.jr.lexer;

import net.jr.common.Position;

public class Token {

    private Lexeme tokenType;

    private Position position;

    private String matchedText;

    public Token(Lexeme tokenType, Position position, String matchedText) {
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

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return tokenType.toString() + "@" + position.toString();
    }
}
