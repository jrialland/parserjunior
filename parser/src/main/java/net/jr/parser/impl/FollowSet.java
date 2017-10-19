package net.jr.parser.impl;


import net.jr.common.Symbol;

import java.util.Collections;

public class FollowSet extends LazySet {

    public FollowSet(Symbol s) {
        super(s);
    }

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
