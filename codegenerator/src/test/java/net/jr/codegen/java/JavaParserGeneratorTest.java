package net.jr.codegen.java;

import net.jr.grammar.c.CGrammar;
import org.junit.Test;

import java.io.StringWriter;

public class JavaParserGeneratorTest {

    @Test
    public void testCodeGeneration() {

        JavaParserGenerator generator = new JavaParserGenerator();

        StringWriter sw = new StringWriter();
        generator.generate(new CGrammar(), sw);
        String code = sw.toString();

        System.out.println(code);

        //Class<?> parserClass = Compiler.compile("CParser", new StringReader(code));

    }
}
