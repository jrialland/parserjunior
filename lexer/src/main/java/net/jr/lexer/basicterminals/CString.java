package net.jr.lexer.basicterminals;

import java.io.DataInputStream;
import java.io.IOException;

public class CString extends QuotedString {

    public CString() {
        super('\"', '\"', '\\', new char[]{'\r', '\n'});
        setName("cString");
    }

    public static  CString  unMarshall(java.io.DataInput in) throws IOException {
        return QuotedString.unMarshall(new CString(), in);
    }

    @Override
    public String toString() {
        return "CString";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!CString.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        return super.equals(obj);
    }

}
