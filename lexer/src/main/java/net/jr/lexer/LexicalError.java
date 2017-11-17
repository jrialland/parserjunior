package net.jr.lexer;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Error that is raised when some text cannot be recognized
 */
public class LexicalError extends RuntimeException {

    private static final long serialVersionUID = 511461615156L;

    private int offendingChar;

    private int position;

    public LexicalError(int offendingChar, int position) {
        super(String.format("offending char : '%s' (0x%s)", StringEscapeUtils.escapeJava("" + (char) offendingChar), Integer.toHexString(offendingChar)));
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
