package net.jr.codegen.util;

import net.jr.util.Rle;

import java.io.StringWriter;
import java.util.*;

public class ArrayComprehension {

    public static String toJs(List<Integer> list) {
        return toJs(list.stream().mapToInt(i->i).toArray());
    }

    public static String toJs(int[] array) {
        Map<Integer, List<String>> constraints = new HashMap<>();
        int[] rle = Rle.encode(array);
        int index = 0;
        for (int i = 0; i < rle.length; i += 2) {
            int val = rle[i];
            int repetitions = rle[i + 1];

            List<String> list = constraints.get(val);
            if (list == null) {
                list = new ArrayList<>();
                constraints.put(val, list);
            }

            if (repetitions == 1) {
                list.add("index==" + index);
            } else {
                list.add("(index>=" + index + "&&index<=" + (index + repetitions) + ")");
            }

            index += repetitions;

        }

        StringWriter sw = new StringWriter();
        sw.append("function(index) {\n");

        List<Map.Entry<Integer, List<String>>> entries = new ArrayList<>(constraints.entrySet());
        Collections.sort(entries, Comparator.comparingInt( x -> x.getValue().size()));

        Iterator<Map.Entry<Integer, List<String>>> it = entries.iterator();
        while(it.hasNext()) {
            Map.Entry<Integer, List<String>> entry = it.next();
            int value = entry.getKey();
            String condition = String.join("||", entry.getValue());
            if(it.hasNext()) {
                sw.append(String.format("    if(%s) { return %d; }\n", condition, value));
            } else {
                sw.append(String.format("    return %d;\n", value));
            }
        }

        constraints.entrySet().stream().forEach((e) -> {

        });
        sw.append("}");
        return sw.toString();
    }

}
