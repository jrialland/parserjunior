package net.jr.parser.impl;

import net.jr.caching.Cache;
import net.jr.common.Symbol;
import net.jr.converters.Converter;
import net.jr.parser.Grammar;
import net.jr.parser.Rule;
import net.jr.util.HexUtil;

import java.io.*;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ActionTableCaching {

    private static final Cache<Grammar, ActionTable> cache;

    private static final Converter<ActionTable, byte[]> serializer = new Converter<ActionTable, byte[]>() {
        @Override
        public ActionTable convertBack(byte[] bytes) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return (ActionTable) ois.readObject();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        @Override
        public byte[] convert(ActionTable actionTable) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(actionTable);
                oos.flush();
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    static {

        Cache.Builder<String, byte[]> onDisk = Cache.Builder.onDisk(ActionTableCaching.class.getName())
                .withTtl(1, TimeUnit.HOURS);

        cache = Cache.Builder.inMemory(Grammar.class, ActionTable.class)
                .withTtl(10, TimeUnit.MINUTES)
                .fallbackingTo(onDisk.withKeyMapper(ActionTableCaching::getCacheKey).withValueConverter(serializer))
                .withFactory(ActionTable::lalr1)
                .build();

    }

    private static final String ruleToString(Rule rule) {
        StringWriter sw = new StringWriter();
        sw.append(rule.getTarget().toString());
        for (Symbol s : rule.getClause()) {
            sw.append(" ");
            sw.append(s.toString());
        }
        return sw.toString();
    }

    private static String getCacheKey(Grammar grammar) {
        List<String> ruleNames = grammar.getRules().stream().map(rule -> ruleToString(rule)).collect(Collectors.toList());
        Collections.sort(ruleNames);
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            for (String ruleName : ruleNames) {
                md.update(ruleName.getBytes());
            }
            return HexUtil.bytesToHex(md.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ActionTable get(Grammar grammar) {
        return cache.get(grammar);
    }

}
