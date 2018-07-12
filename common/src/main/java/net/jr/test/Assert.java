package net.jr.test;

import java.util.regex.Pattern;

public class Assert {

    public static void notNull(Object obj, String... msg) {
        if (obj == null) {
            throw new IllegalStateException(String.join(" ", msg));
        }
    }

    public static void isTrue(boolean assertion, String... msg) {
        if (!assertion) {
            throw new IllegalStateException(String.join(" ", msg));
        }
    }

    public static void isFalse(boolean assertion, String... msg) {
        if (assertion) {
            throw new IllegalStateException(String.join(" ", msg));
        }
    }

    public static void eq(Object a, Object b, String... msg) {
        isTrue((a == null && b == null) || (a != null && a.equals(b)), msg);
    }

    public static void notEq(Object a, Object b, String... msg) {
        isTrue((a == null && b != null) || (a != null && (b == null || !a.equals(b))), msg);
    }

    public static void matches(String expr, CharSequence s, String... msg) {
        matches(Pattern.compile(expr), s, msg);
    }

    public static void matches(Pattern pattern, CharSequence s, String... msg) {
        isTrue(pattern.matcher(s).matches(), msg);
    }
}
