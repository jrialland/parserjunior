package net.jr.common;

import net.jr.marshalling.MarshallingCapable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Position implements MarshallingCapable {

    private static final Position INITIAL = new Position(1,1);

    private int line;

    private int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Position nextLine() {
        return new Position(line + 1, 1);
    }

    public Position nextColumn() {
        return new Position(line, column + 1);
    }

    public Position updated(char c) {
        return c == '\n' ? nextLine() : nextColumn();
    }

    @Override
    public String toString() {
        return getLine() + ":" + getColumn();
    }

    @Override
    public int hashCode() {
        return line ^ 7 + column ^ 23;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(Position.class)) {
            return false;
        }
        final Position oPosition = (Position) obj;
        return oPosition.line == line && oPosition.column == column;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(line);
        dataOutputStream.writeInt(column);
    }

    @SuppressWarnings("unused")
    public static Position unMarshall(DataInputStream in) throws IOException {
        int line = in.readInt();
        int column = in.readInt();
        return new Position(line, column);
    }

    public static Position start() {
        return INITIAL;
    }
}
