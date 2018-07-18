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

    default int getPriority() {
        return 1;
    }

    Terminal withPriority(int priority);

}
