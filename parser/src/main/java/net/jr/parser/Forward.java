package net.jr.parser;


import net.jr.common.Symbol;

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

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }
}
