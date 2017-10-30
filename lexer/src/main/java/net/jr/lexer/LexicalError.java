package net.jr.lexer;

import net.jr.util.StringUtil;
import org.apache.commons.lang3.StringEscapeUtils;

public class LexicalError extends RuntimeException {

    private int offendingChar;

    private int position;

    public LexicalError(int offendingChar, int position) {
        super(String.format("offending char : '%s' (0x%s)", StringEscapeUtils.escapeJava(""+(char)offendingChar), Integer.toHexString(offendingChar)));
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
