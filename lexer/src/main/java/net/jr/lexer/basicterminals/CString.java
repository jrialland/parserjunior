package net.jr.lexer.basicterminals;

import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CString extends QuotedString {

    public CString() {
        super('\"', '\"', '\\', new char[]{'\r', '\n'});
        setName("cString");
    }

    public static CString unMarshall(DataInputStream in) throws IOException {
        return TerminalImpl.unMarshall(new CString(), in);
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
