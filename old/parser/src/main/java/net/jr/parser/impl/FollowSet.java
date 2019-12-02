package net.jr.parser.impl;


import net.jr.common.Symbol;
import net.jr.parser.Grammar;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of FOLLOW sets.
 * <p>
 * <blockquote cite="https://www.cs.uaf.edu/~cs331/notes/FirstFollow.pdf">
 * Define FOLLOW(A), for nonterminal A, to be the set of terminals a that can appear immediately to the right of A in some sentential form, that is, the set of terminals
 * a such that there exists a derivation of the form S ⇒αΑ a β  for some  α and  β
 * </blockquote>
 *
 * @see ActionTable.LALR1Builder#defineFollowSet(Map, Grammar, Symbol)
 */
public class FollowSet extends LazySet {

    private static final Set<Symbol> EmptySet = Collections.emptySet();

    public FollowSet(Symbol s) {
        super(s);
    }

    /**
     * Special method for creating an empty followSet
     *
     * @param s
     * @return
     */
    public static FollowSet emptySet(Symbol s) {
        FollowSet f = new FollowSet(s);
        f.setResolution(EmptySet);
        return f;
    }

    @Override
    public int hashCode() {
        return getSubject().hashCode() ^ 17;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(FollowSet.class)) {
            return false;
        }

        final FollowSet o = (FollowSet) obj;
        return o.getSubject().equals(getSubject());
    }

    @Override
    public String toString() {
        return String.format("FollowSet(%s)", getSubject());
    }
}
