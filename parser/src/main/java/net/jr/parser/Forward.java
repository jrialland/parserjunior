package net.jr.parser;


import net.jr.common.Symbol;

/**
 * This is the base type for non-terminal symbols, that may be used to designate any non-terminal in a grammar.
 * The 'Forward' term is borrowed from the python pyparsing (http://pyparsing.wikispaces.com/) library
 */
public class Forward implements Symbol {

    private String name;

    public Forward() {
        this(null);
    }

    public Forward(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name == null ? super.toString() : name;
    }

    /**
     * @return false
     */
    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(Forward.class)) {
            return false;
        }

        Forward o = (Forward) obj;
        if (name == null) {
            return hashCode() == o.hashCode();
        } else {
            return o.name != null && name.equals(o.name);
        }
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return super.hashCode();
        } else {
            return name.hashCode() ^ 41;
        }
    }
}
