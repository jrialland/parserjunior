package net.jr.codegen.lexer;

import net.jr.codegen.java.Compiler;
import net.jr.grammar.c.CGrammar;
import net.jr.lexer.Lexer;
import net.jr.util.StringUtil;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public class LexerGeneratorTest {

    @Test
    public void testCGrammar() throws Exception {
        Lexer lexer = new CGrammar().createParser().getLexer();
        StringWriter sw = new StringWriter();
        new LexerGenerator().generate(lexer, sw);
        System.out.println(sw.toString());
        Class<?> clazz = Compiler.compile("Lexer", new StringReader(sw.toString()));

        Method lexMethod = clazz.getMethod("lex", Reader.class, Consumer.class);

        String l = "int  main( void) { int i; i = 12; return i || 2; }";

        new CLexer().lex(new StringReader(l), new Consumer<CLexer.Token>() {
            @Override
            public void accept(CLexer.Token token) {
                System.out.println(token);
            }
        });


        /*

        lexMethod.invoke(clazz.newInstance(), new StringReader(l), new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                System.out.println(o);
            }
        });

        */

    }

    @Test
    public void testOnGeneratedLexer() throws IOException {
        SampleLexer sampleLexer = new SampleLexer();
        sampleLexer.lex(new StringReader("17 + 23 - 17 + 7   "), (token) -> {
            System.out.println(">>>" + token.getMatchedText() + "<<<" + token.getTokenType().name());
        });
    }
}
