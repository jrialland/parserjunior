package net.jr.jrc.qvm.syscalls;

import net.jr.jrc.qvm.QvmInterpreter;

/**
 * Created by julien on 11/10/16.
 */
public class Math {

    @QvmSyscall(100)
    public int sin(QvmInterpreter interpreter) {
        float val = (float) java.lang.Math.sin(interpreter.getStack().popFloat());
        return Float.floatToRawIntBits(val);
    }

    @QvmSyscall(101)
    public int cos(QvmInterpreter interpreter) {
        float val = (float) java.lang.Math.cos(interpreter.getStack().popFloat());
        return Float.floatToRawIntBits(val);
    }

    @QvmSyscall(102)
    public int tan(QvmInterpreter interpreter) {
        float val = (float) java.lang.Math.tan(interpreter.getStack().popFloat());
        return Float.floatToRawIntBits(val);
    }
}
