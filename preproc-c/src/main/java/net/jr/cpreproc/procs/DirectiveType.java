package net.jr.cpreproc.procs;

import net.jr.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * List of preprocessor directives
 */
public enum DirectiveType {

    Define,
    Elif,
    Else,
    Endif,
    Error,
    If,
    Ifdef,
    Ifndef,
    Include,
    Info,
    Line,
    Pragma,
    Undef,
    Warning;

    private static final Pattern PDirective;

    private static TreeMap<String, DirectiveType> byNames = new TreeMap<>();

    static {
        String regex = "^\\p{Blank}*#\\p{Blank}*(";
        for (DirectiveType d : values()) {
            String l = d.name().toLowerCase();
            byNames.put(l, d);
            regex += l + "|";
        }
        regex = regex.substring(0, regex.length() - 1) + ")(.*)$";
        PDirective = Pattern.compile(regex);
    }

    public static Pair<DirectiveType, String> detectDirective(String line) {
        Matcher m = PDirective.matcher(line);
        if (m.matches()) {
            DirectiveType directiveType = byNames.get(m.group(1));
            if(directiveType != null) {
                return Pair.of(directiveType, StringUtil.ltrim(m.group(2)));
            }
        }
        return null;
    }

}
