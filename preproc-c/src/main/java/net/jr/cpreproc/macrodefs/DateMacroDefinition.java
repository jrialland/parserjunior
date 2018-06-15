package net.jr.cpreproc.macrodefs;

import net.jr.grammar.c.CStringUtil;
import net.jr.lexer.Token;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateMacroDefinition extends NoArgsMacroDefinition {

    public static final String TimeStamp = "__TIMESTAMP__";

    public static final String Time = "__TIME__";

    public static final String Date = "__DATE__";

    public static final DateMacroDefinition TimeStampDefinition = new DateMacroDefinition(TimeStamp, "EEE MMM dd hh:mm:ss yyyy");

    public static final DateMacroDefinition TimeDefinition = new DateMacroDefinition(Time, "hh:mm:ss");

    public static final DateMacroDefinition DateDefinition = new DateMacroDefinition(Date, "MMM dd yyyy");

    private String pattern;

    private DateMacroDefinition(String name, String pattern) {
        super(name);
        this.pattern = pattern;
    }

    @Override
    public List<Token> getReplacement() {
        String value = new SimpleDateFormat(pattern, Locale.US).format(new Date());
        Token t = null;//new Token(CStringUtil.escapeC(value.getBytes())); FIXME FIXME FIXME
        //t.setType(Token.Type.StringLiteral);
        return Arrays.asList(t);
    }
}
