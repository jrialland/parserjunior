package net.jr.parser.impl;

import net.jr.caching.Cache;
import net.jr.marshalling.MarshallingUtil;
import net.jr.parser.Grammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ActionTableCaching {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionTableCaching.class);
    private static final Cache<Grammar, ActionTable> cache;
    private static boolean enabled = true;

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

    private static final Logger getLog() {
        return LOGGER;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ActionTableCaching.enabled = enabled;
    }

    public static ActionTable get(Grammar grammar) {
        if (enabled) {
            try {
                return cache.get(grammar);
            } catch (Exception e) {
                getLog().error("Cache failure", e);
                return ActionTable.lalr1(grammar);
            }
        } else {
            return ActionTable.lalr1(grammar);
        }
    }

}
