package net.jr.codegen.java;

import net.jr.text.IndentPrintWriter;

import java.io.Writer;
import java.util.*;

public class SearchFn {

    public static <T> void generate(List<Integer> lx, List<Integer> ly, List<T> lz, Writer writer) {
        assert lx.size() == ly.size();
        assert lx.size() == lz.size();

        Iterator<Integer> itX = lx.iterator();
        Iterator<Integer> itY = ly.iterator();
        Iterator<T> itZ = lz.iterator();

        Map<Integer, Map<T, Set<Integer>>> byX = new TreeMap<>();

        for (int i = 0, max = lx.size(); i < max; i++) {
            int x = itX.next();
            int y = itY.next();
            T z = itZ.next();
            Map<T, Set<Integer>> byZ = byX.computeIfAbsent(x, _x -> new TreeMap());
            byZ.computeIfAbsent(z, _z -> new TreeSet<>()).add(y);
        }

        IndentPrintWriter ipw = writer instanceof IndentPrintWriter ? (IndentPrintWriter) writer : new IndentPrintWriter(writer, "  ");

        ipw.println("switch(x) {");
        ipw.indent();
        for (Map.Entry<Integer, Map<T, Set<Integer>>> entry : byX.entrySet()) {
            int x = entry.getKey();

            ipw.println("case " + x + " :");
            ipw.indent();
            ipw.println("switch(y) {");
            ipw.indent();
            for (Map.Entry<T, Set<Integer>> entryZy : entry.getValue().entrySet()) {
                T z = entryZy.getKey();
                for (Integer y : entryZy.getValue()) {
                    ipw.println("case " + y + " :");
                }
                ipw.indent();
                ipw.println("return " + z + ";");
                ipw.deindent();
            }
            ipw.println("default : throw new IllegalStateException();");

            ipw.deindent();
            ipw.println("}");
            ipw.deindent();

        }
        ipw.indent();
        ipw.println("default : throw new IllegalStateException();");
        ipw.deindent();
        ipw.deindent();
        ipw.println("}");
        ipw.flush();
    }
}
