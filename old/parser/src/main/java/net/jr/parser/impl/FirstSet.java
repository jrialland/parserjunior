package net.jr.parser.impl;

import net.jr.common.Symbol;
import net.jr.parser.Grammar;

/**
 * FIRST sets are used to descript what are the possible first tokens leading to the fullfilment of a particular grammar rule.
 * This notion is used when building lalr parsers
 * <p>
 * <quote>
 * If  α is any string of grammar symbols, let FIRST( α ) be the set of terminals that begin the strings derived
 * from  α.
 * </quote>
 * <p>
 * Some docs :
 * <ul>
 * <li><a href="https://www.jambe.co.nz/UNI/FirstAndFollowSets.html">https://www.jambe.co.nz/UNI/FirstAndFollowSets.html</a></li>
 * <li><a href="https://www.cs.uaf.edu/~cs331/notes/FirstFollow.pdf">https://www.cs.uaf.edu/~cs331/notes/FirstFollow.pdf</a></li>
 * </ul>
 *
 * @see ActionTable.LALR1Builder#getFirst(Grammar, Symbol)
 */
public class FirstSet extends LazySet {

    public FirstSet(Symbol s) {
        super(s);
    }

    @Override
    public int hashCode() {
        return getSubject().hashCode() + 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(FirstSet.class)) {
            return false;
        }
        final FirstSet o = (FirstSet) obj;
        return o.getSubject().equals(getSubject());
    }

    @Override
    public String toString() {
        return String.format("FirstSet(%s)", getSubject());
    }
}
