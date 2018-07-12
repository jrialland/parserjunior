package net.jr.codegen.js;

import net.jr.codegen.util.ArrayComprehension;
import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.parser.Grammar;
import net.jr.parser.Rule;
import net.jr.parser.impl.Action;
import net.jr.parser.impl.ActionTable;
import net.jr.parser.impl.ActionType;
import net.jr.util.WriterOutputStream;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsParserGenerator {

    private static final JtwigTemplate template;

    static {
        String path = JsParserGenerator.class.getPackage().getName().replace('.', '/') + "/js.twig";
        template = JtwigTemplate.classpathTemplate(path);
    }


    public void generate(Grammar grammar, Writer writer) {
        PrintWriter pw = (writer instanceof PrintWriter) ? (PrintWriter) writer : new PrintWriter(writer);

        List<Symbol> terminals = new ArrayList<>(grammar.getTerminals());
        List<String> terminalNames = terminals.stream().map(t -> "T_" + t.toString().toUpperCase()).collect(Collectors.toList());

        //prepare actionTable data

        ActionTable actionTable = ActionTable.lalr1(grammar);

        List<Integer> actionTypes = new ArrayList<>();
        List<Integer> actionParams = new ArrayList<>();
        List<Integer> emptyActions = new ArrayList<>();

        for (int s = 0, max = actionTable.getStatesCount(); s < max; s++) {
            for (Symbol t : terminals) {
                Action action = actionTable.getAction(s, t);
                if (action != null) {
                    int actionType = action.getActionType().ordinal();
                    int parameter = action.getActionParameter();
                    actionTypes.add(actionType);
                    actionParams.add(parameter);
                } else {
                    actionTypes.add(ActionType.Fail.ordinal());
                    actionParams.add(0);
                }

                boolean allowsEmpty = actionTable.getExpectedTerminals(s).contains(Lexemes.empty());
                emptyActions.add(allowsEmpty ? actionTable.getAction(s, Lexemes.empty()).getActionType().ordinal() : 0);
            }
        }

        JtwigModel model = JtwigModel.newModel();
        model.with("grammar", grammar);
        model.with("actionTypeEnum", Arrays.asList(ActionType.values()).stream().map(a -> a.name() + " = " + a.ordinal()).collect(Collectors.toList()));
        model.with("terminalNames", terminalNames);

        model.with("actionTypes_fnct", ArrayComprehension.toJs(actionTypes));
        model.with("actionParams_fnct", ArrayComprehension.toJs(actionParams));
        model.with("emptyActions_fnct", ArrayComprehension.toJs(emptyActions));

        model.with("targetRuleIndex", grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next().getId());

        model.with("nextStates", getNextStates(grammar, actionTable));

        template.render(model, new WriterOutputStream(pw));
        pw.flush();
    }

    private static String getNextStates(Grammar grammar, ActionTable actionTable) {

        StringWriter sw = new StringWriter();
        sw.append("function(state, ruleId) {\n");
        sw.append("    switch(state) {\n");

        for (int state = 0; state < actionTable.getStatesCount(); state++) {
            int[] rules = new int[grammar.getRules().size()];
            for (int id = 0; id < rules.length; id++) {
                Rule rule = grammar.getRuleById(id);
                Symbol targetSymbol = rule.getTarget();

                sw.append("        case " + state + ":\n");

                Action action = actionTable.getAction(state, targetSymbol);
                if (action != null) {
                    rules[id] = action.getActionParameter();
                }
                sw.append("        return " + ArrayComprehension.toJs(rules) + "(ruleId);");
            }
        }
        sw.append("        default:\n");
        sw.append("             return null;\n");
        sw.append("    }\n");
        sw.append("}");

        return sw.toString();
    }
}
