package net.jr.util;

import java.io.StringWriter;

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
                    sw.append(c);
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
                    sw.append(c);
                    break;
                case '1':
                    sw.append('\u00B9');
                    break;
                case '2':
                    sw.append('\u00b2');
                    break;
                case '3':
                    sw.append('\u00b3');
                    break;
                default:
                    sw.append((char) (SUPERSCRIPT_BLOCK_START + (c - '0')));
            }
        }
        return sw.toString();
    }

    public static String repeat(String s, int size) {
        assert s != null;
        assert s.length() > 0;
        String result = "";
        while(result.length() < size) {
            result += s;
        }
        return result.substring(0, size);
    }

    public static String center(String s, int size) {
        if(s.length() > size) {
            return s.substring(0, size);
        }
        String result = repeat(" ", (size - s.length()) /2) + s;
        return result + repeat(" ", size - result.length());
    }
}
