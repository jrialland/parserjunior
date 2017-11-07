package net.jr.grammar.c11;


import net.jr.lexer.Lexemes;
import net.jr.lexer.Token;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;


public class CGrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void testPrintln() {
        CGrammar grammar = new CGrammar();
        List<String> lines = new ArrayList<>(Arrays.asList(grammar.toString().split("\n")));
        lines = lines.stream().map(l -> l.replaceFirst("\\([0-9]+\\)", "")).collect(Collectors.toList());
        Collections.sort(lines);
        lines = lines.subList(0, lines.size() - 2);
        int i = 1;
        for (String line : lines) {
            System.out.println(Integer.toString(i++) + ".  \t" + line);
        }
    }

    @Test
    public void testSimple() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser();
        AstNode ast = parser.parse("int main(int argc, char **argv) { return EXIT_SUCCESS;}");
        System.out.println(ast);
    }

    @Test
    public void testIfElse() {
        CGrammar grammar = new CGrammar();
        Parser parser = grammar.createParser();
        AstNode ast = parser.parse("int main(int argc, char **argv) { if(argc==0) { return false;} else {return true;}}");
        System.out.println(ast);
    }

}


