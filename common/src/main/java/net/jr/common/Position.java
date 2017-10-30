package net.jr.common;

public class Position {

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
        return new Position(line+1, 1);
    }

    public Position nextColumn() {
        return new Position(line, column+1);
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
}
