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

    void setPriority(int priority);

    int getPriority();

    Terminal withPriority(int priority);

    void setName(String name);

    String getName();

    default Terminal withName(String name) {
        setName(name);
        return this;
    }
}
