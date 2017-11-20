package net.jr.lexer.expr;

import net.jr.lexer.impl.Automaton;
import net.jr.lexer.impl.DefaultAutomaton;
import net.jr.lexer.impl.LexemeImpl;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.VisitorHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegexLexeme extends LexemeImpl {

    private static final RegexGrammar Grammar = new RegexGrammar();

    private static final Parser RegexParser = Grammar.createParser();

    private String expression;

    private int priority = 1;

    private Automaton automaton;

    public RegexLexeme(String expression) {
        this(expression, 1);
    }

    public RegexLexeme(String expression, int priority) {
        this.expression = expression;
        this.priority = priority;

        AstNode astNode = RegexParser.parse(expression);
        RegexVisitor visitor = new RegexVisitor(this);
        VisitorHelper.visit(astNode, visitor);
        this.automaton = visitor.getAutomaton();
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

        if (!obj.getClass().isAssignableFrom(RegexLexeme.class)) {
            return false;
        }

        final RegexLexeme o = (RegexLexeme) obj;

        return expression.equals(o.expression) && priority == o.priority;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(expression);
        dataOutputStream.writeInt(priority);
    }

    @SuppressWarnings("unused")
    public static RegexLexeme unMarshall(DataInputStream in) throws IOException {
        String expression = in.readUTF();
        int priority = in.readInt();
        return new RegexLexeme(expression, priority);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }


}
