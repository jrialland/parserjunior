package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LineComment extends LexemeImpl {

    private String commentStart;

    public LineComment(String commentStart) {
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
        dataOutputStream.writeUTF(commentStart);
    }

    @SuppressWarnings("unused")
    public static LineComment unMarshall(DataInputStream in) throws IOException {
        return new LineComment(in.readUTF());
    }
}
