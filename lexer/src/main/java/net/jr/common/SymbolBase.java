package net.jr.common;

public abstract class SymbolBase implements Symbol {

    private Integer id;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        if (id == null) {
            throw new IllegalStateException("this terminal has not be assigned an id yet. Call setId(int) first !");
        }
        return id;
    }
}
