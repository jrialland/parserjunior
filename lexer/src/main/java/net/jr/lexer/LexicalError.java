package net.jr.lexer;

public class LexicalError extends RuntimeException {

    private int offendingChar;

    private int position;

    public LexicalError(int offendingChar, int position) {
        super("offending char : 0x" + Integer.toHexString(offendingChar));
        this.offendingChar = offendingChar;
        this.position = position;
    }

    public int getOffendingChar() {
        return offendingChar;
    }

    public int getPosition() {
        return position;
    }
}
