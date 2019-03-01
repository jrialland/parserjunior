package net.jr.codegen.java;

import net.jr.lexer.Lexer;
import net.jr.lexer.Terminal;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.automaton.State;
import net.jr.lexer.automaton.Transition;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.MergingLexerStreamImpl;
import net.jr.parser.Grammar;
import net.jr.test.Assert;
import net.jr.text.IndentPrintWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParserGenerator {

    private Grammar grammar;

    private Lexer lexer;

    private Path dest;

    private String packageName;

    public ParserGenerator(Grammar grammar, Path dest, String packageName) throws IOException {
        if (!Files.exists(dest)) {
            Files.createDirectories(dest);
        }
        Assert.isTrue(Files.isDirectory(dest));
        this.grammar = grammar;
        this.dest = dest;
        this.packageName = packageName;
    }

    private void withFile(String filename, FileCallback cb) {
        IndentPrintWriter pw = new IndentPrintWriter(new PrintWriter(System.out));
        cb.withWriter(pw);
        pw.flush();
    }

    public void generateTokenTypes() {

        withFile("TokenType.java", (pw) -> {

            pw.println("package " + packageName + ";");

            pw.println("public enum TokenType {");
            pw.indent();

            for (Terminal terminal : lexer.getTokenTypes()) {
                pw.println(String.format("T_%s(),", terminal.getName()));
            }

            pw.deindent();
            pw.println("}");
        });

    }

    public void generateLexer(Lexer lexer) {
        MergingLexerStreamImpl lexerStream = (MergingLexerStreamImpl) lexer.iterator(new StringReader(""));

        withFile("Lexer.java", (pw) -> {
            pw.println("package " + packageName + ";");
            pw.println("public class Lexer {");
            pw.indent();
            makeTransitions(pw, lexerStream.getInitialState());
            pw.deindent();
            pw.println("}");
        });

    }

    private void makeTransitions(IndentPrintWriter pw, State<Character> state) {
        int n = 0;
        for (Transition<Character> transition : state.getOutgoingTransitions()) {
            if (n == 0) {
                pw.print("if(");
            } else {
                pw.print("else if(");
            }
            pw.print(asExpression(transition));
            pw.println(") {");
            pw.indent();
            pw.println("return lexer_state_" + transition.getNextState().getId() + ";");
            pw.println("// " + state.getTerminal());
            pw.deindent();
            pw.println("}");
            n++;
        }
    }

    private String asExpression(Transition<Character> transition) {
        CharConstraint charConstraint = (CharConstraint) transition.getConstraint();
        return charConstraint.getExpr();
    }

    interface FileCallback {
        void withWriter(IndentPrintWriter pw);
    }

}
