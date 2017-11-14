package net.jr.marshalling;

import java.io.DataOutputStream;
import java.io.IOException;

public interface MarshallingCapable {

    void marshall(DataOutputStream dataOutputStream) throws IOException;

}
