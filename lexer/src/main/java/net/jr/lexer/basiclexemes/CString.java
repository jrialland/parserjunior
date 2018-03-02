package net.jr.lexer.basiclexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CString extends QuotedString {

    public CString() {
        super('\"', '\"', '\\', new char[]{'\r', '\n'});
        setName("cString");
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

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CString unMarshall(DataInputStream in) throws IOException {
        return new CString();
    }
}
