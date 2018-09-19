import net.jr.cpreproc.Preprocessor;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.io.IOUtil;
import net.jr.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PreprocessorTest {

    private Set<Pair<String, String>> getResources() {
        Set<String> expected = new Reflections("", new ResourcesScanner()).getResources(Pattern.compile("^.*\\.expected\\.txt$"));
        return expected.stream().map(s -> Pair.of(s.replaceFirst("\\.expected\\.txt$", ".txt"), s)).collect(Collectors.toSet());
    }

    private String read(String rsc) {
        Reader r = new InputStreamReader(PreprocessorTest.class.getClassLoader().getResourceAsStream(rsc));
        return IOUtil.readFully(r);
    }

    protected String process(String txt) {
        Map<String, MacroDefinition> defs = new TreeMap<>();
        Preprocessor p = new Preprocessor();
        p.setInput(new StringReader(txt), "str");
        return p.process();
    }

    @Test
    public void test() {
        for (Pair<String, String> cases : getResources()) {
            String testData = read(cases.getLeft());
            String expected = read(cases.getRight());
            System.out.println(StringUtil.repeatUntilSize("-", 10) + "    " + cases.getLeft());
            String processed = process(testData);

            System.out.println(processed);
            //Assert.assertEquals(expected, processed);
        }


    }
}
