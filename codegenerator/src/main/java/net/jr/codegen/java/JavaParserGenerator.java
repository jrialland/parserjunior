package net.jr.codegen.java;

import net.jr.parser.Grammar;
import net.jr.parser.impl.Action;
import net.jr.parser.impl.ActionTable;
import net.jr.text.IndentPrintWriter;
import net.jr.util.WriterOutputStream;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.Writer;
import java.util.*;

public class JavaParserGenerator {

    private static final JtwigTemplate templateFileStart;

    private static final JtwigTemplate templateFileEnd;

    static {
        String path = JavaParserGenerator.class.getPackage().getName().replace('.', '/');
        templateFileStart = JtwigTemplate.classpathTemplate(path + "/java_start.twig");
        templateFileEnd = JtwigTemplate.classpathTemplate(path + "/java_end.twig");
    }

    public void generate(Grammar grammar, Writer writer) {

        ActionTable actionTable = ActionTable.lalr1(grammar);

        IndentPrintWriter pw = new IndentPrintWriter(writer, "  ");
        JtwigModel model = JtwigModel.newModel();
        model.with("grammar", grammar);

        //start of file
        templateFileStart.render(model, new WriterOutputStream(pw));
        pw.println();
        pw.indent();

        //method for getting shift/reduce action depending on symbol

        writeGetActionMethod(pw, grammar, actionTable);


        //method for getting next state when doing a reduce
        writeNextStateMethod(pw, grammar, actionTable);


        //end of file
        pw.deindent();
        templateFileEnd.render(model, new WriterOutputStream(pw));
        pw.flush();
    }

    private void writeNextStateMethod(IndentPrintWriter pw, Grammar grammar, ActionTable actionTable) {
        Map<Integer, Map<Integer, Integer>> bySymbol = new TreeMap<>();

        pw.println("private int getNextState(int state, int symbol) {");
        pw.indent();

        pw.println("switch(symbol) {");
        pw.indent();

        for (int symbol : bySymbol.keySet()) {

            pw.println("case " + symbol + ":");
            pw.indent();

            Map<Integer, Integer> byState = bySymbol.get(symbol);
            if (byState.size() == 1) {
                pw.println("return " + byState.values().iterator().next() + ";");
            } else {
                pw.println("switch(state) {");
                pw.indent();
                for (Map.Entry<Integer, Integer> entry : byState.entrySet()) {
                    int state = entry.getKey();
                    pw.println("case " + state + ":");
                    pw.indent();
                    pw.println("return " + entry.getValue() + ";");
                    pw.deindent();
                }
                pw.deindent();
                pw.println("}");
                pw.println("break;");
            }
            pw.deindent();
        }

        pw.println("throw new IllegalArgumentException();");

        pw.deindent();
        pw.println("}");
        pw.deindent();
        pw.println("}");
        pw.println();
    }

    private void writeGetActionMethod(IndentPrintWriter pw, Grammar grammar, ActionTable actionTable) {
        pw.println("private void getAction(int state, int symbol) {");
        pw.indent();

        pw.println("switch(symbol) {");
        pw.indent();

        for (int symbol : map.keySet()) {
            pw.println("case " + symbol + ":");
            pw.indent();

            Map<Integer, Action> possibleActions = map.get(symbol);
            if (possibleActions.size() == 1) {
                pw.println("return /*" + possibleActions.values().iterator().next() + "*/;");
            } else {
                pw.println("switch(state) {");
                pw.indent();
                for (Map.Entry<Integer, Action> entry : possibleActions.entrySet()) {
                    int state = entry.getKey();
                    Action action = entry.getValue();
                    pw.println("case " + state + ":");
                    pw.indent();
                    pw.println("return /*" + action + "*/;");
                    pw.deindent();
                }
                pw.deindent();
                pw.println("}");
                pw.println("break;");
            }
            pw.deindent();
        }

        pw.deindent();
        pw.println("}");
        pw.println("throw new IllegalStateException();");

        pw.deindent();
        pw.println("}");
        pw.println();
    }

}
