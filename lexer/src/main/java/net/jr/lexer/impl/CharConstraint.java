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
            return new Builder("c==" + "'" + StringEscapeUtils.escapeJava(Character.toString((char) c)) + "'", x -> x == c);
        }

        public static Builder inRange(int min, int max) {
            return new Builder(String.format("c>=%d && c<=%d", min, max), x -> x >= min && x <= max);
        }

        public static Builder inList(String possibleChars) {
            String expression = String.format("\"%s\".contains(Character.toString(c))", StringEscapeUtils.escapeJava(possibleChars));
            return new Builder(expression, x -> possibleChars.contains(Character.toString(x)));
        }

        public static Builder inList(char[] possibleChars) {
            List<Character> list = new ArrayList<>();
            for (char c : possibleChars) {
                list.add(c);
            }
            String str = "[" + String.join(", ", list.stream().map(x -> Integer.toString(x)).collect(Collectors.toList())) + "]";
            return new Builder(String.format("Arrays.binarySearch(%s, c)>-1", str), x -> Arrays.binarySearch(possibleChars, x) > -1);
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
