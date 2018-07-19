package net.jr.lexer.impl;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CharConstraint implements Function<Character, Boolean> {

    private String expr;

    private Function<Character, Boolean> fn;

    private CharConstraint() {

    }

    @Override
    public Boolean apply(Character character) {
        return fn.apply(character);
    }

    @Override
    public int hashCode() {
        return expr.hashCode();
    }

    @Override
    public String toString() {
        return expr;
    }

    public static class Builder {

        private String expr = "c";

        private Function<Character, Boolean> fn;

        private Builder(String expr, Function<Character, Boolean> fn) {
            this.expr = expr;
            this.fn = fn;
        }

        public static Builder any() {
            return new Builder(String.format("true"), x -> true);
        }

        public static Builder eq(int c) {
            String s;
            if(c == '\'') {
                s = "\\'";
            } else {
                s = StringEscapeUtils.escapeJava(Character.toString((char) c));
            }
            return new Builder("c==" + "'" + s + "'", x -> x == c);
        }

        public static Builder inRange(int min, int max) {
            return new Builder(String.format("c>=%d && c<=%d", min, max), x -> x >= min && x <= max);
        }

        public static Builder inList(String possibleChars) {
            String expression = String.format("\"%s\".indexOf((char)c)>-1", StringEscapeUtils.escapeJava(possibleChars));
            return new Builder(expression, x -> possibleChars.contains(Character.toString(x)));
        }

        public static Builder inList(char[] possibleChars) {
            return inList(new String(possibleChars));
        }

        public static Builder not(Builder builder) {
            return new Builder("!(" + builder.expr.toString() + ")", x -> !builder.fn.apply(x));
        }

        public static Builder or(Builder b1, Builder b2) {
            return new Builder("(" + b1.expr.toString() + ")||(" + b2.expr.toString() + ")", x -> b1.fn.apply(x) || b2.fn.apply(x));
        }

        public static Builder and(Builder b1, Builder b2) {
            return new Builder("(" + b1.expr.toString() + ")&&(" + b2.expr.toString() + ")", x -> b1.fn.apply(x) && b2.fn.apply(x));
        }

        public CharConstraint build() {
            CharConstraint charConstraint = new CharConstraint();
            charConstraint.expr = expr;
            charConstraint.fn = fn;
            return charConstraint;
        }

    }

}
