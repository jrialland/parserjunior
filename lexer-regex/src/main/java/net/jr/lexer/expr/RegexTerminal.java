package net.jr.lexer.expr;

import net.jr.lexer.automaton.Automaton;
import net.jr.lexer.expr.impl.RegexAutomaton;
import net.jr.lexer.expr.impl.RegexGrammar;
import net.jr.lexer.expr.impl.RegexVisitor;
import net.jr.lexer.impl.TerminalImpl;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A Terminal that can be configured using some kind of 'regular expressions'.
 */
public class RegexTerminal extends TerminalImpl {

    private static final RegexGrammar Grammar = new RegexGrammar();

    private static final Parser RegexParser = Grammar.createParser();

    private String expression;

    private int priority = 1;

    private RegexAutomaton automaton;

    public RegexTerminal(String expression) {
        this(expression, 1);
    }

    public RegexTerminal(String expression, int priority) {
        this.expression = expression;
        this.priority = priority;

        AstNode astNode = RegexParser.parse(expression);
        RegexVisitor visitor = new RegexVisitor(this);
        VisitorHelper.visit(astNode, visitor);
        this.automaton = visitor.getAutomaton();
        this.automaton.setTokenType(this);
    }

    @SuppressWarnings("unused")
    public static RegexTerminal unMarshall(DataInputStream in) throws IOException {
        String expression = in.readUTF();
        int priority = in.readInt();
        return new RegexTerminal(expression, priority);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (!obj.getClass().isAssignableFrom(RegexTerminal.class)) {
            return false;
        }

        final RegexTerminal o = (RegexTerminal) obj;

        return expression.equals(o.expression) && priority == o.priority;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(expression);
        dataOutputStream.writeInt(priority);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
}
