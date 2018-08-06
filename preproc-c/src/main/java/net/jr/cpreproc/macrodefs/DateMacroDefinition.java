package net.jr.cpreproc.macrodefs;

import net.jr.cpreproc.lexer.PreprocToken;

import java.text.SimpleDateFormat;
import java.util.*;

public class DateMacroDefinition extends NoArgsMacroDefinition {

    public static final String TimeStamp = "__TIMESTAMP__";

    public static final String Time = "__TIME__";

    public static final String Date = "__DATE__";

    public static final DateMacroDefinition TimeStampDefinition = new DateMacroDefinition(TimeStamp, "EEE MMM dd hh:mm:ss yyyy");

    public static final DateMacroDefinition TimeDefinition = new DateMacroDefinition(Time, "hh:mm:ss");

    public static final DateMacroDefinition DateDefinition = new DateMacroDefinition(Date, "MMM dd yyyy");

    private String pattern;

    public static Map<String, MacroDefinition> addDefinitions(Map<String, MacroDefinition> defs) {
        defs.put(TimeStamp, TimeStampDefinition);
        defs.put(Time, TimeDefinition);
        defs.put(Date, DateDefinition);
        return defs;
    }

    private DateMacroDefinition(String name, String pattern) {
        super(name);
        this.pattern = pattern;
    }

    public Date getDate() {
        return new Date();
    }

    @Override
    public List<PreprocToken> getReplacement(PreprocToken originalToken) {
        String value = new SimpleDateFormat(pattern, Locale.US).format(getDate());
        PreprocToken t = new PreprocToken(PreprocToken.NoMeaning, value);
        t.setPosition(originalToken.getPosition());
        return Arrays.asList(t);
    }
}
