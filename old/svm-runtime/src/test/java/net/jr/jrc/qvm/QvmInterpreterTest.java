package net.jr.jrc.qvm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class QvmInterpreterTest {


    @Test
    public void testAddition() {

        List<QvmInstruction> instructions = Arrays.asList(
                new QvmInstruction(QvmInstruction.OpCode.CONST, 21),
                new QvmInstruction(QvmInstruction.OpCode.CONST, 21),
                new QvmInstruction(QvmInstruction.OpCode.ADD),
                new QvmInstruction(QvmInstruction.OpCode.BREAK)
        );

        QvmFile qvmFile = new QvmFile();
        qvmFile.getInstructions().addAll(instructions);
        QvmInterpreter interpreter = new QvmInterpreter();
        int result = interpreter.run(qvmFile);
        Assert.assertEquals(42, result);
    }

}
