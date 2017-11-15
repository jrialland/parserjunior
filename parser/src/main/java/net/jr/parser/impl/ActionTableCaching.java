package net.jr.parser.impl;

import net.jr.caching.Cache;
import net.jr.marshalling.MarshallingUtil;
import net.jr.parser.Grammar;

import java.util.concurrent.TimeUnit;

public class ActionTableCaching {

    private static final Cache<Grammar, ActionTable> cache;

    private static boolean enabled = true;

    public static void setEnabled(boolean enabled) {
        ActionTableCaching.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    static {

        Cache.Builder<String, byte[]> onDisk = Cache.Builder.onDisk(ActionTableCaching.class.getName())
                .withTtl(1, TimeUnit.HOURS);

        cache = Cache.Builder.inMemory(Grammar.class, ActionTable.class)
                .withTtl(10, TimeUnit.MINUTES)
                .fallbackingTo(
                        onDisk.withKeyMapper(Grammar::getFingerprint)
                                .withValueConverter(MarshallingUtil.converter(ActionTable.class, true))
                )
                .withFactory(ActionTable::lalr1)
                .build();

    }

    public static ActionTable get(Grammar grammar) {
        if (enabled) {
            return cache.get(grammar);
        } else {
            return ActionTable.lalr1(grammar);
        }
    }

}
