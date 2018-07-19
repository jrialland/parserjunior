package net.jr.lexer.basicterminals;

import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LineComment extends TerminalImpl {

    private String commentStart;

    public LineComment(String commentStart) {
        this();
        init(commentStart);
    }

    private LineComment() {
        super();
    }

    private void init(String commentStart) {
        this.commentStart = commentStart;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(CharConstraint.Builder.eq(c)).goTo(next);
            currentState = next;
        }
        currentState.when(CharConstraint.Builder.not(CharConstraint.Builder.eq('\n'))).goTo(currentState);
        currentState.when(CharConstraint.Builder.eq('\n')).goTo(builder.newFinalState());
        setAutomaton(builder.build());
    }

    @SuppressWarnings("unused")
    public static LineComment unMarshall(DataInputStream in) throws IOException {
        LineComment l = TerminalImpl.unMarshall(new LineComment(), in);
        l.init(in.readUTF());
        return l;
    }

    @Override
    public String toString() {
        return String.format("LineComment('%s')", commentStart);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(LineComment.class)) {
            return false;
        }
        final LineComment o = (LineComment) obj;
        return commentStart.equals(o.commentStart);
    }

    @Override
    public int hashCode() {
        return 17 + commentStart.hashCode();
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        super.marshall(dataOutputStream);
        dataOutputStream.writeUTF(commentStart);
    }
}
