package net.jr.grammar.c;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CStringUtil {

    private static final Pattern cStringPattern = Pattern
            .compile("(\\\\[bfnrtav\"?'\\\\])|(\\\\x[0-9A-Fa-f]{1,2})|(\\\\[0-7]{1,2})|(.)");

    private static Map<Character, Byte> escapeChars = new HashMap<>();

    private static Map<Byte, Character> unescapeChars = new HashMap<>();


    static {
        escapeChars.put('\\', (byte) '\\');
        escapeChars.put('\'', (byte) '\'');
        escapeChars.put('"', (byte) '"');
        escapeChars.put('b', (byte) '\b');
        escapeChars.put('f', (byte) '\f');
        escapeChars.put('n', (byte) '\n');
        escapeChars.put('r', (byte) '\r');
        escapeChars.put('t', (byte) '\t');
        escapeChars.put('a', (byte) 7);
        escapeChars.put('v', (byte) 0x0b);
        escapeChars.put('?', (byte) '?');

        for (Map.Entry<Character, Byte> entry : escapeChars.entrySet()) {
            unescapeChars.put(entry.getValue(), entry.getKey());
        }

    }


    public static String escapeC(byte[] cBytes) {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (byte b : cBytes) {
            if (b == 0)
                break;
            Character c = unescapeChars.get(b);
            sb.append(c == null ? Character.valueOf((char) b) : "\\" + c);
        }
        sb.append("\"");
        return sb.toString();
    }

    public static byte[] unescapeC(String cString, final boolean addTrailingNUL) {
        // if the parameter has quotes, remove them
        final char firstChar = cString.charAt(0);
        if (firstChar == '"' || firstChar == '\'') {
            cString = cString.substring(1, cString.length() - 1);
        }

        final Matcher m = cStringPattern.matcher(cString);
        String s;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(cString.length());
        while (m.find()) {
            // escape char
            if ((s = m.group(1)) != null) {
                baos.write(escapeChars.get(s.charAt(1)));
            }

            // hex
            else if ((s = m.group(2)) != null) {
                baos.write(Integer.parseInt(s.substring(2), 16));
            }

            // octal
            else if ((s = m.group(3)) != null) {
                baos.write(Integer.parseInt(s.substring(1), 8));
            }

            // normal characters
            else {
                baos.write(m.group(4).getBytes()[0]);
            }
        }
        if (addTrailingNUL) {
            baos.write(0);
        }
        return baos.toByteArray();
    }
}
