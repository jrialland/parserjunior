package net.jr.jrc.qvm.syscalls;

import net.jr.jrc.qvm.QvmInterpreter;

public class Exit {

    @QvmSyscall(255)
    public int exit(QvmInterpreter interpreter) {
        int retVal = interpreter.getStack().popInt();
        interpreter.stop();
        return retVal;
    }

}
