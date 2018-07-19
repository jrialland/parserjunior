package net.jr.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtil {

    private static final int SUBSCRIPT_BLOCK_START = 0x2080;

    private static final int SUPERSCRIPT_BLOCK_START = 0x2070;

    /**
     * Convert a number to subscript (similar to what the &lt;sub&gt; tag does in html)
     *
     * @param number
     * @return
     */
    public static String toSubscript(long number) {
        StringWriter sw = new StringWriter();
        for (char c : Long.toString(number).toCharArray()) {
            switch (c) {
                case '-':
                    sw.append('\u208B');
                    break;
                default:
                    sw.append((char) (SUBSCRIPT_BLOCK_START + (c - '0')));
            }
        }
        return sw.toString();
    }

    /**
     * Convert a number to superscript (similar to what the &lt;sup&gt; tag does in html)
     *
     * @param number
     * @return
     */
    public static String toSuperscript(long number) {
        StringWriter sw = new StringWriter();
        for (char c : Long.toString(number).toCharArray()) {
            switch (c) {
                case '-':
                    sw.append('\u207B');
                    break;
                case '1':
                    sw.append('\u00B9');
                    break;
                case '2':
                    sw.append('\u00B2');
                    break;
                case '3':
                    sw.append('\u00B3');
                    break;
                default:
                    sw.append((char) (SUPERSCRIPT_BLOCK_START + (c - '0')));
            }
        }
        return sw.toString();
    }

    public static String repeatUntilSize(String s, int size) {
        assert s != null;
        assert s.length() > 0;
        String result = "";
        while (result.length() < size) {
            result += s;
        }
        return result.substring(0, size);
    }

    public static String repeatTimes(String s, int times) {
        assert s != null;
        assert times >= 0;
        String result = "";
        while (times-- > 0) {
            result += s;
        }
        return result;
    }

    public static String center(String s, int size) {
        if (s.length() > size) {
            return s.substring(0, size);
        }
        String result = repeatUntilSize(" ", (size - s.length()) / 2) + s;
        return result + repeatUntilSize(" ", size - result.length());
    }

    /**
     * replace accentuated chars
     *
     * @param s string with accents
     * @return the same string without accents
     */
    public static String removeAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        return s.replaceAll("\\p{M}", "");
    }

    /**
     * highlight occurences of a word in a text
     *
     * @param s           string to highlight
     * @param highlighted le term that has to be higlighted
     * @param hStart      how to mark the beginning of the highlighting (for example "&lt;strong&gt;")
     * @param hEnd        how to mark the end of the highlighting (for example"&lt;/strong&gt;")
     * @return the highlighted string
     */
    public static String highlight(String s, String highlighted, String hStart, String hEnd) {
        String h = removeAccents(highlighted).toLowerCase();
        List<String> terms = Arrays.asList(h.split("\\p{Space}+")).stream().map(Pattern::quote).collect(Collectors.toList());
        String regex = "(?:" + String.join("|", terms.toArray(new String[]{})) + ")";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher m = pattern.matcher(StringUtil.removeAccents(s));
        StringWriter sw = new StringWriter();
        int offset = 0;
        while (m.find()) {
            sw.append(s.substring(offset, m.start()));
            sw.append(hStart);
            sw.append(s.substring(m.start(), m.end()));
            sw.append(hEnd);
            offset = m.end();
        }
        sw.append(s.substring(offset));
        return sw.toString();
    }

    public static String ltrim(String txt) {
        return txt.replaceFirst("^\\p{Blank}*", "");
    }

    public static String rtrim(String txt) {
        return txt.replaceFirst("\\p{Blank}*$", "");
    }

    public static String lpad(String txt, String padding, int len) {
        int missing = len - txt.length();
        if (missing > 0) {
            return repeatUntilSize(padding, missing) + txt;
        } else {
            return txt.substring(-1 * missing, txt.length());
        }
    }

    public static String rpad(String txt, String padding, int len) {
        int missing = len - txt.length();
        if (missing > 0) {
            return txt + repeatUntilSize(padding, missing);
        } else {
            return txt.substring(0, len);
        }
    }

    public static void nl(String txt, Writer out) throws IOException {
        String[] parts = txt.split("\n");
        int padLen = Integer.toString(parts.length).length();
        int i = 1;
        for (String part : parts) {
            out.write(rpad(Integer.toString(i++), " ", padLen));
            out.write(". ");
            out.write(part);
            out.write('\n');
        }
        out.flush();
    }

    public static String nl(String txt) {
        StringWriter sw = new StringWriter();
        try {
            nl(txt, sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }
}
