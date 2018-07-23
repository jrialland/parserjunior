package net.jr.codegen.lexer;

import net.jr.codegen.java.Compiler;
import net.jr.codegen.java.LexerGenerator;
import net.jr.grammar.c.CGrammar;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class LexerGeneratorTest {

    @Test
    public void testCGrammar() throws Exception {

        String packageName = LexerGeneratorTest.class.getPackage().getName();

        StringWriter sw = new StringWriter();
        new LexerGenerator(packageName).generate(new CGrammar().createParser().getLexer(), sw);
        String javaCode = sw.toString();

        Path javaFilePath = Paths.get(".")
                .toAbsolutePath()
                .resolve("src/test/java/")
                .resolve(packageName.replace('.', '/'))
                .resolve("Lexer.java");

        Files.write(javaFilePath, javaCode.getBytes());

        Class<?> clazz = Compiler.compile("net.jr.codegen.lexer.Lexer", new StringReader(javaCode));

        Object instance = clazz.newInstance();
        Method lex = instance.getClass().getMethod("lex", Reader.class, Consumer.class);

        String l = "int  main(void) { int i; i = 12; return i || 2; }";

        lex.invoke(instance, new StringReader(l), new Consumer() {
            @Override
            public void accept(Object o) {
                System.out.println(o);
            }
        });
    }
}
