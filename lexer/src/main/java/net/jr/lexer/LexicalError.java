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

    private static String getMessage(int r, Position position) {
        if(r != -1) {
            return String.format("(%s) - Offending char : '%s' (0x%s)", position, StringEscapeUtils.escapeJava("" + (char) r), Integer.toHexString(r));
        } else {
            return String.format("(%s) - No match at end of input", position);
        }
    }

    public LexicalError(int offendingChar, Position position) {
        super(getMessage(offendingChar, position));
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
