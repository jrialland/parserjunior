package net.jr.codegen.java;

import net.jr.grammar.c.CGrammar;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

public class ParserGeneratorTest {

    @Test
    public void test() throws IOException {

        CGrammar grammar = new CGrammar();

        ParserGenerator generator = new ParserGenerator(grammar, Files.createTempDirectory("test"), "com.test");
        generator.generateLexer(grammar.getLexer());

    }

}
