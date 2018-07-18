package net.jr.codegen.lexer;

import net.jr.lexer.Lexer;
import net.jr.lexer.automaton.State;
import net.jr.lexer.impl.MergingLexerStreamImpl;
import net.jr.lexer.impl.StatesVisitor;
import net.jr.text.IndentPrintWriter;
import net.jr.util.IOUtil;
import net.jr.util.WriterOutputStream;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class LexerGenerator {

    private static final JtwigTemplate templateStart, templateEnd;

    static {
        String path = LexerGenerator.class.getPackage().getName().replace('.', '/');
        templateStart = JtwigTemplate.classpathTemplate(path + "/lexer_start.twig");
        templateEnd = JtwigTemplate.classpathTemplate(path + "/lexer_end.twig");

    }

    private List<State<Character>> getStates(Lexer lexer) {
        State<Character> initialState = ((MergingLexerStreamImpl) lexer.iterator(IOUtil.emptyReader())).getInitialState();
        List<State<Character>> allStates = new ArrayList<>();
        StatesVisitor.visit(initialState, (state) -> {
            allStates.add(state);
        });
        return allStates;
    }

    public void generate(Lexer lexer, Writer writer) {

        WriterOutputStream os = new WriterOutputStream(writer);
        IndentPrintWriter ipw = new IndentPrintWriter(writer);

        JtwigModel model = JtwigModel.newModel();
        model.with("lexer", lexer);
        model.with("states", getStates(lexer));

        templateStart.render(model, os);
        templateEnd.render(model, os);

        ipw.flush();
    }

}
