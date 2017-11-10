package net.jr.lexer;

import net.jr.common.Position;

/**
 * A {@link Token} is the basic 'word' of a particular program.
 */
public class Token {

    private Lexeme tokenType;

    private Position position;

    private String matchedText;

    public Token(Lexeme tokenType, Position position, String matchedText) {
        this.tokenType = tokenType;
        this.position = position;
        this.matchedText = matchedText;
    }

    /**
     * @return The matched text
     */
    public String getText() {
        return matchedText;
    }

    /**
     * @return the type of this token
     */
    public Lexeme getTokenType() {
        return tokenType;
    }

    /**
     * The position where this token in the original stream
     *
     * @return
     */
    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return tokenType.toString() + "@" + position.toString();
    }
}
