package net.jr.lexer;

import net.jr.common.Position;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Error that is raised when some text cannot be recognized
 */
public class LexicalError extends RuntimeException {

    private static final long serialVersionUID = 511461615156L;

    private int offendingChar;

    private Position position;

    public LexicalError(int offendingChar, Position position) {
        super(String.format("(%s) - Offending char : '%s' (0x%s)", position, StringEscapeUtils.escapeJava("" + (char) offendingChar), Integer.toHexString(offendingChar)));
        this.offendingChar = offendingChar;
        this.position = position;
    }

    public int getOffendingChar() {
        return offendingChar;
    }

    public Position getPosition() {
        return position;
    }
}
