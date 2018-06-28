package net.jr.cpreproc.procs;

import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.pipe.Suppliers;
import net.jr.util.IOUtil;
import net.jr.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DirectivesInterpreterTest {


    private Set<Pair<String, String>> getResources() {
        Set<String> expected = new Reflections("", new ResourcesScanner()).getResources(Pattern.compile("^.*\\.expected\\.txt$"));
        return expected.stream().map(s -> Pair.of(s.replaceFirst("\\.expected\\.txt$", ".txt"), s)).collect(Collectors.toSet());
    }

    private String read(String rsc) {
        Reader r = new InputStreamReader(DirectivesInterpreterTest.class.getClassLoader().getResourceAsStream(rsc));
        return IOUtil.readFully(r);
    }

    protected String interpret(String txt) {

        Map<String, MacroDefinition> defs = new TreeMap<>();


        DirectivesInterpreter d = new DirectivesInterpreter(defs, null);

        List<String> result = Suppliers.fromString(txt)
                .pipeTo(new ConvertToLines())
                .pipeTo(d)
                .convert(line -> line.getText())
                .list();

        return String.join("\n", result);
    }

    @Test
    public void test() {
        for (Pair<String, String> cases : getResources()) {
            String testData = read(cases.getLeft());
            String expected = read(cases.getRight());
            System.out.println(StringUtil.repeat("-", 10) + "    " + cases.getLeft());
            Assert.assertEquals(expected, interpret(testData));
        }


    }
}
