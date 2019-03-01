package net.jr.jrc.qvm.syscalls;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import net.jr.jrc.qvm.QvmInterpreter;

/**
 * Created by julien on 10/10/16.
 */
public class BasicIO {

    private Reader in;

    private Writer out;

    public BasicIO(Reader in, Writer out) {
        this.in = in;
        this.out = out;
    }

    @QvmSyscall(1)
    public int putc(QvmInterpreter interpreter) {
        int i = interpreter.getStack().popInt();
        try {
            out.write(i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @QvmSyscall(2)
    public int getc(QvmInterpreter interpreter) {
        try {
            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
