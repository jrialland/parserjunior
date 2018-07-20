package net.jr.parser;


import net.jr.common.Symbol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This is the base type for non-terminal symbols, that may be used to designate any non-terminal in a grammar.
 */
public class NonTerminal implements Symbol {

    private String name;

    private Integer id;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        if (id == null) {
            throw new IllegalStateException("This terminal has not be assigned an id yet. Call setId(int) first !");
        }
        return id;
    }

    public NonTerminal() {
        this(null);
    }

    public NonTerminal(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public static NonTerminal unMarshall(DataInput in) throws IOException {
        String name = in.readUTF();
        return new NonTerminal(name);
    }

    public String getName() {
        return name == null ? super.toString() : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return false
     */
    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(NonTerminal.class)) {
            return false;
        }

        NonTerminal o = (NonTerminal) obj;
        if (name == null) {
            return hashCode() == o.hashCode();
        } else {
            return o.name != null && name.equals(o.name);
        }
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return super.hashCode();
        } else {
            return name.hashCode() ^ 41;
        }
    }

    @Override
    public void marshall(DataOutput dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(name);
    }
}
