package net.jr.common;

import net.jr.marshalling.MarshallingCapable;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

public class Position implements MarshallingCapable {

    public static final String UNKNOWN_FILENAME = "<?>";

    private static final Position UNKNOWN = new Position(-1, -1, UNKNOWN_FILENAME);

    private int line;

    private int column;

    private String filename = UNKNOWN_FILENAME;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Position(int line, int column, String filename) {
        this(line, column);
        setFilename(filename);
    }

    @SuppressWarnings("unused")
    public static  Position  unMarshall(java.io.DataInput in) throws IOException {
        int line = in.readInt();
        int column = in.readInt();
        String position = in.readUTF();
        return new Position(line, column, position);
    }

    public static Position unknown() {
        return UNKNOWN;
    }

    public static Position beforeStart() {
        return new Position(0, 0);
    }

    public static Position start() {
        return new Position(1, 1);
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public Position nextLine() {
        return new Position(line + 1, 1, filename);
    }

    public Position nextColumn() {
        return new Position(line, column + 1, filename);
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
        return line ^ 7 + column ^ 23 + filename.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(Position.class)) {
            return false;
        }
        final Position oPosition = (Position) obj;
        return oPosition.line == line && oPosition.column == column && oPosition.filename.equals(filename);
    }

    @Override
    public void marshall(DataOutput dataOutputStream) throws IOException {
        dataOutputStream.writeInt(line);
        dataOutputStream.writeInt(column);
        dataOutputStream.writeUTF(filename);
    }

    public Position withOffset(int cols) {
        return cols == 0 ? this : new Position(line, column + cols, filename);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private boolean isSameFile(Position position) {
        return this.filename.equals(position.filename);
    }

    public boolean isSameLine(Position position) {
        return isSameFile(position) && this.line == position.line;
    }

    public boolean isNextLineOf(Position position) {
        return isSameFile(position) && this.line == position.line + 1;
    }


}
