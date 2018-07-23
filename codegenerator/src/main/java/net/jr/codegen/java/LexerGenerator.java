package net.jr.codegen.java;

import com.google.common.base.Strings;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.Terminal;
import net.jr.lexer.automaton.State;
import net.jr.lexer.impl.MergingLexerStreamImpl;
import net.jr.lexer.impl.StatesVisitor;
import net.jr.util.IOUtil;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class LexerGenerator {

    private static final JtwigTemplate template;

    static {
        String path = LexerGenerator.class.getPackage().getName().replace('.', '/');
        template = JtwigTemplate.classpathTemplate(path + "/lexer.twig");
    }

    private String packageName;

    public LexerGenerator() {
        this(null);
    }

    public LexerGenerator(String packageName) {
        this.packageName = packageName;
    }

    private List<State<Character>> getStates(Lexer lexer) {
        State<Character> initialState = ((MergingLexerStreamImpl) lexer.iterator(IOUtil.emptyReader())).getInitialState();
        List<State<Character>> allStates = new ArrayList<>();
        StatesVisitor.visit(initialState, (state) -> {
            Terminal terminal = state.getTerminal();
            if (terminal != null) {
                if (Strings.isNullOrEmpty(terminal.getName())) {
                    throw new IllegalStateException(String.format("Terminal %s has no name !", terminal.toString()));
                }
            }
            allStates.add(state);
        });
        return allStates;
    }

    public void generate(Lexer lexer, Writer writer) throws IOException {
        OutputStream os = new ByteArrayOutputStream();
        List<State<Character>> states = getStates(lexer);
        JtwigModel model = JtwigModel.newModel();
        model.with("packageName", packageName);
        model.with("lexer", lexer);
        model.with("is_eof_filtered", lexer.isFilteredOut(Lexemes.eof()));
        model.with("states", states);
        template.render(model, os);
        os.flush();
        String code = os.toString().replaceAll("\n(\\p{Space}+\n)*", "\n");
        writer.write(code);
        writer.flush();
    }

}
