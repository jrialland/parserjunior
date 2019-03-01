package net.jr.lexer.impl;

import net.jr.util.StringUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.function.Function;

public class CharConstraint implements Function<Character, Boolean> {

    private Function<Character, Boolean> fn;
    private Nature nature;
    private Object subject;

    private CharConstraint(Nature nature, Object subject) {
        this.nature = nature;
        this.subject = subject;
    }

    public String getExpr() {
        switch (nature) {
            case ANY:
                return "true";
            case EQ:
                String s = "" + (char) ((int) subject);
                return "c == '" + StringUtil.escapeJava(s) + "'";
            case INRANGE:
                char[] cSubject = (char[]) subject;
                String min = "'" + StringUtil.escapeJava("" + cSubject[0]) + "'";
                String max = "'" + StringUtil.escapeJava("" + cSubject[1]) + "'";
                return String.format("(c >= %s && c <= %s)", min, max);
            case INLIST:
                String possibleChars = new String((char[]) subject);
                return String.format("\"%s\".indexOf((char)c)>-1", StringUtil.escapeJava(possibleChars));
            case NOT:
                return "!(" + ((CharConstraint) subject).getExpr() + ")";
            case OR:
                CharConstraint[] or = (CharConstraint[]) subject;
                return String.format("((%s)||(%s))", or[0].getExpr(), or[1].getExpr());
            case AND:
                CharConstraint[] and = (CharConstraint[]) subject;
                return String.format("((%s)&&(%s))", and[0].getExpr(), and[1].getExpr());
        }
        throw new IllegalStateException("unknown nature : " + nature);
    }

    @Override
    public Boolean apply(Character character) {
        return fn.apply(character);
    }

    @Override
    public int hashCode() {
        return getExpr().hashCode();
    }

    @Override
    public String toString() {
        return getExpr();
    }

    public enum Nature {
        ANY, // true
        EQ, // c== subject
        INRANGE, // c >= subject[0] && c <= subject[1]
        INLIST, // subject.indexOf(c) > -1
        NOT, // ! subject
        OR, // subject[0] || subject[1]
        AND, // subject[0] && subject[1]
    }

    public static class Builder {

        private Nature nature;

        private Object subject;

        private Function<Character, Boolean> fn;

        private Builder(Function<Character, Boolean> fn, Nature nature, Object subject) {
            this.fn = fn;
            this.nature = nature;
            this.subject = subject;
        }

        public static Builder any() {
            return new Builder(x -> true, Nature.ANY, null);
        }

        public static Builder eq(int c) {
            return new Builder(x -> x == c, Nature.EQ, c);
        }

        public static Builder inRange(int min, int max) {
            return new Builder(x -> x >= min && x <= max, Nature.INRANGE, new char[]{(char) min, (char) max});
        }

        public static Builder inList(String possibleChars) {
            return new Builder(x -> possibleChars.contains(Character.toString(x)), Nature.INLIST, possibleChars.toCharArray());
        }

        public static Builder inList(char[] possibleChars) {
            return inList(new String(possibleChars));
        }

        public static Builder not(Builder builder) {
            return new Builder(x -> !builder.fn.apply(x), Nature.NOT, builder.build());
        }

        public static Builder or(Builder b1, Builder b2) {
            return new Builder(x -> b1.fn.apply(x) || b2.fn.apply(x),
                    Nature.OR,
                    new CharConstraint[]{b1.build(), b2.build()}
            );
        }

        public static Builder and(Builder b1, Builder b2) {
            return new Builder(x -> b1.fn.apply(x) && b2.fn.apply(x),
                    Nature.AND,
                    new CharConstraint[]{b1.build(), b2.build()}
            );
        }

        public CharConstraint build() {
            CharConstraint charConstraint = new CharConstraint(nature, subject);
            charConstraint.fn = fn;
            return charConstraint;
        }
    }

}
