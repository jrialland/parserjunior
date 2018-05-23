package net.jr.codegen.js;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.parser.Grammar;
import net.jr.parser.impl.Action;
import net.jr.parser.impl.ActionTable;
import net.jr.parser.impl.ActionType;
import net.jr.util.WriterOutputStream;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.PrintWriter;
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
        PrintWriter pw = (writer instanceof PrintWriter)?(PrintWriter)writer:new PrintWriter(writer);

        List<Symbol> terminals = new ArrayList<>(grammar.getTerminals());
        List<String> terminalNames = terminals.stream().map(t -> "T_" + t.toString().toUpperCase()).collect(Collectors.toList());

        //prepare actionTable data

        ActionTable actionTable = ActionTable.lalr1(grammar);

        List<Integer> actionTypes = new ArrayList<>();
        List<Integer> actionParams = new ArrayList<>();
        List<Integer> emptyActions = new ArrayList<>();

        for(int s=0, max=actionTable.getStatesCount(); s<max; s++) {
            for(Symbol t : terminals) {
                Action action = actionTable.getAction(s, t);
                if(action != null) {
                    int actionType = action.getActionType().ordinal();
                    int parameter = action.getActionParameter();
                    actionTypes.add(actionType);
                    actionParams.add(parameter);
                } else {
                    actionTypes.add(ActionType.Fail.ordinal());
                    actionParams.add(0);
                }

                boolean allowsEmpty = actionTable.getExpectedTerminals(s).contains(Lexemes.empty());
                emptyActions.add(allowsEmpty ?actionTable.getAction(s, Lexemes.empty()).getActionType().ordinal() : 0);
            }
        }

        JtwigModel model = JtwigModel.newModel();
        model.with("grammar", grammar);
        model.with("actionTypeEnum", Arrays.asList(ActionType.values()).stream().map(a -> a.name()+ " = " + a.ordinal()).collect(Collectors.toList()));
        model.with("terminalNames", terminalNames);
        model.with("actionTypes", actionTypes);
        model.with("actionParams", actionParams);
        model.with("emptyActions", emptyActions);
        model.with("targetRuleIndex", grammar.getRulesTargeting(grammar.getTargetSymbol()).iterator().next().getId());
        template.render(model, new WriterOutputStream(pw));
        pw.flush();
    }

}
