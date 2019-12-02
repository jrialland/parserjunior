package net.jr.marshalling;

import java.io.DataOutput;
import java.io.IOException;

public interface MarshallingCapable {

    void marshall(DataOutput dataOutput) throws IOException;

}
