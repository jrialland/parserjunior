package net.jr.grammar.c;

import net.jr.lexer.Token;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CFilesTest {

    private Set<String> getResources() {
        return new Reflections("", new ResourcesScanner()).getResources(Pattern.compile("^.*\\.c$"));
    }

    @Test
    public void test() {
        getResources().stream().sorted().forEach(rsc -> {
            System.out.println(rsc);
            Reader reader = new InputStreamReader(CFilesTest.class.getClassLoader().getResourceAsStream(rsc));
            new CGrammar().createParser().parse(reader);
        });

    }


    @Test
    public void testParser() throws IOException {
        Reader reader = new InputStreamReader(CFilesTest.class.getClassLoader().getResourceAsStream("7_arrays.c"));
        List<Token> tokens = new CGrammar().createParser().getLexer().tokenize(reader);
        boolean gotToken = false;
        for (Token t : tokens) {
            if(t.toString().equals("CString@5:26")) {
                gotToken = true;
                Assert.assertEquals("\"\\\"Hello \\n World\\\"\\x0a\"", t.getText());
            }
        }
        Assert.assertTrue(gotToken);
    }

}
