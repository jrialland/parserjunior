package net.jr.lexer;

import net.jr.common.Symbol;

/**
 * A {@link Terminal} is the basicterminals unit of meaning in a grammar. It is a synonymn of "terminal symbol"
 */
public interface Terminal extends Symbol {

    @Override
    default boolean isTerminal() {
        return true;
    }

    int getPriority();

    void setPriority(int priority);

    Terminal withPriority(int priority);

    String getName();

    void setName(String name);

    default Terminal withName(String name) {
        setName(name);
        return this;
    }
}
