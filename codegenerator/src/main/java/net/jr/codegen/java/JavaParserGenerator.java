package net.jr.codegen.java;

import net.jr.common.Symbol;
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

        int i=0;
        Map<Symbol, Integer> syms = new HashMap<>();
        for(Symbol t : grammar.getSymbols()) {
            syms.put(t, i++);
        }

        List<Integer> symbols = new ArrayList<>();
        List<Integer> states = new ArrayList<>();
        List<Integer> nextStates = new ArrayList<>();

        forActions(grammar.getNonTerminals(), actionTable, (symbol, state, action) -> {
            symbols.add(syms.get(symbol));
            states.add(state);
            nextStates.add(action.getActionParameter());
        });

        SearchFn.generate(symbols, states, nextStates, pw);

        pw.println("throw new IllegalArgumentException();");
        pw.deindent();
        pw.println("}");
        pw.println();
    }

    private interface ActionCb {
        void apply(Symbol symbol, int state, Action action);
    }

    private void forActions(Collection<Symbol> symbols, ActionTable actionTable, ActionCb cb) {
        for(int i=0, max=actionTable.getStatesCount(); i<max; i++) {
            for(Symbol t : symbols) {
                Action action = actionTable.getAction(i, t);
                if(action != null) {
                    cb.apply(t, i, action);
                }
            }
        }
    }

    private void writeGetActionMethod(IndentPrintWriter pw, Grammar grammar, ActionTable actionTable) {
        pw.println("private void getAction(int state, int symbol) {");
        pw.indent();
        pw.println("throw new IllegalStateException();");
        pw.deindent();
        pw.println("}");
        pw.println();
    }

}
