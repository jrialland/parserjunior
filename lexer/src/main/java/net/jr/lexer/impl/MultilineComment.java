package net.jr.lexer.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;
import static net.jr.lexer.impl.CharConstraint.Builder.not;

public class MultilineComment extends LexemeImpl {

    private String commentStart;

    private String commentEnd;

    public MultilineComment(String commentStart, String commentEnd) {
        this.commentStart = commentStart;
        this.commentEnd = commentEnd;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        for (char c : commentStart.toCharArray()) {
            DefaultAutomaton.Builder.BuilderState next = builder.newNonFinalState();
            currentState.when(eq(c)).goTo(next);
            currentState = next;
        }
        DefaultAutomaton.Builder.BuilderState inComment = currentState;
        char[] end = commentEnd.toCharArray();
        for (int i = 0; i < end.length; i++) {
            final char c = end[i];
            DefaultAutomaton.Builder.BuilderState nextState = i == end.length - 1 ? builder.newFinalState() : builder.newNonFinalState();
            currentState.when(eq(c)).goTo(nextState);
            currentState.when(not(eq(c))).goTo(inComment);
            currentState = nextState;
        }
        setAutomaton(builder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(MultilineComment.class)) {
            return false;
        }
        final MultilineComment o = (MultilineComment) obj;
        return commentStart.equals(o.commentStart) && commentEnd.equals(o.commentEnd);
    }

    @Override
    public int hashCode() {
        return 23 + commentStart.hashCode() + commentEnd.hashCode();
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(commentStart);
        dataOutputStream.writeUTF(commentEnd);
    }

    public static MultilineComment unMarshall(DataInputStream in) throws IOException {
        return new MultilineComment(in.readUTF(), in.readUTF());
    }
}
