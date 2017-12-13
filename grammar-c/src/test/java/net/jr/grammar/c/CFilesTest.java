package net.jr.grammar.c;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Pattern;

public class CFilesTest {

    private Set<String> getResources() {
        return new Reflections("", new ResourcesScanner()).getResources(Pattern.compile("^.*\\.c$"));
    }

    @Test
    public void test() {

        CGrammar cGrammar = new CGrammar();

        getResources().stream().sorted().forEach(rsc -> {
            System.out.println(rsc);
            Reader reader = new InputStreamReader(CFilesTest.class.getClassLoader().getResourceAsStream(rsc));
            cGrammar.createParser().parse(reader);
        });

    }


}
