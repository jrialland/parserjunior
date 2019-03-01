package net.jr.cpreproc.procs;


import net.jr.collection.CollectionsUtil;
import net.jr.common.Position;
import net.jr.pipes.PipeableProcessor;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class TrigraphsRemover extends PipeableProcessor<String, PreprocessorLine> {

    private static final Map<String, String> digraphs;

    private static final Map<String, String> trigraphs;

    private static final Map<String, String> allSymbols;

    static {

        digraphs = CollectionsUtil.zip(
                new String[]{"<:", ":>", "<%", "%>", "%:"},
                new String[]{"[", "]", "{", "}", "#"}
        );

        trigraphs = CollectionsUtil.zip(
                new String[]{"??=", "??/", "??'", "??(", "??)", "??!", "??<", "??>", "??-"},
                new String[]{"#", "\\", "^", "[", "]", "|", "{", "}", "~"}
        );

        allSymbols = new TreeMap<>();
        digraphs.entrySet().forEach((e) -> allSymbols.put(e.getKey(), e.getValue()));
        trigraphs.entrySet().forEach((e) -> allSymbols.put(e.getKey(), e.getValue()));
    }

    int line = 0;

    @Override
    public void generate(String s, Consumer<PreprocessorLine> out) {
        line++;
        PreprocessorLine pLine = new PreprocessorLine(new Position(line, 1), s);
        for (Map.Entry<String, String> entry : allSymbols.entrySet()) {
            String symbol = entry.getKey();
            String replacement = entry.getValue();
            int i;
            while ((i = s.indexOf(symbol)) != -1) {
                pLine.removeChars(i, symbol.length());
                pLine.insert(i, replacement);
                s = pLine.getText();
            }
        }
        out.accept(pLine);
    }

}