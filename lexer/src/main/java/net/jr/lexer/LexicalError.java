package net.jr.lexer;

public class LexicalError extends RuntimeException {

    private int position;

    public LexicalError(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
