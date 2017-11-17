package net.jr.lexer.impl;

public class CString extends QuotedString {

    public CString() {
        super('\"', '\"', '\\', new char[]{'\r', '\n'});
    }

    @Override
    public String toString() {
        return "CString";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(this==obj) {
            return true;
        }
        if(!CString.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        return super.equals(obj);
    }
}
