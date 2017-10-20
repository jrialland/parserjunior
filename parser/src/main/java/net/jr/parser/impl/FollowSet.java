package net.jr.parser.impl;


import net.jr.common.Symbol;
import net.jr.parser.Grammar;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of FOLLOW sets.
 *
 * <blockquote cite="https://www.cs.uaf.edu/~cs331/notes/FirstFollow.pdf">
 *     Define FOLLOW(A), for nonterminal A, to be the set of terminals a that can appear immediately to the right of A in some sentential form, that is, the set of terminals
 * a such that there exists a derivation of the form S ⇒αΑ a β  for some  α and  β
 * </blockquote>
 *
 * @see ActionTable.LALR1Builder#defineFollowSet(Map, Grammar, Symbol)
 */
public class FollowSet extends LazySet {

    public FollowSet(Symbol s) {
        super(s);
    }

    /**
     * Special method for creating an empty followSet
     * @param s
     * @return
     */
    public static FollowSet emptySet(Symbol s) {
        FollowSet f = new FollowSet(s);
        f.setResolution(Collections.emptySet());
        return f;
    }

    @Override
    public String toString() {
        return String.format("FollowSet(%s)", getSubject());
    }
}
