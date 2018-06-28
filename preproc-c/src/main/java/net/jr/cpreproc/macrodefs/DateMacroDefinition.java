package net.jr.cpreproc.macrodefs;

import net.jr.grammar.c.CGrammar;
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

    public Date getDate() {
        return new Date();
    }

    @Override
    public List<Token> getReplacement(Token originalToken) {
        String value = new SimpleDateFormat(pattern, Locale.US).format(getDate());
        Token t = new Token(CGrammar.Tokens.String_literal, originalToken.getPosition(), value);
        return Arrays.asList(t);
    }
}
