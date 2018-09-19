
package net.jr.jrc.qvm;

import net.jr.io.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by julien on 09/10/16.
 */
public class QvmFileTest {

    private String makeQvmFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        QvmFile qvm = new QvmFile();
        qvm.setBssLen(3467);
        qvm.getData().addAll(Arrays.asList(-36, 976, -829, 29));
        qvm.getInstructions().add(new QvmInstruction(QvmInstruction.OpCode.BREAK));
        qvm.getInstructions().add(new QvmInstruction(QvmInstruction.OpCode.CONST, 5678));
        qvm.getInstructions().add(new QvmInstruction(QvmInstruction.OpCode.IGNORE));
        qvm.setLit("Hello world".getBytes());
        qvm.write(baos);
        return Hex.toHex(baos.toByteArray());
    }

    @Test
    public void testWrite() throws Exception {
        String hex = makeQvmFile();
        Assert.assertEquals(
                "44147212030000000800000010000000200000008b0d000002082e1600000100dcffffffd0030000c3fcffff1d00000048656c6c6f20776f726c64",
                hex);
    }

    @Test
    public void testRead() throws Exception {
        String hex = makeQvmFile();
        QvmFile qvm = QvmFile.read(Hex.hexInputStream(new ByteArrayInputStream(hex.getBytes())));
        Assert.assertEquals(3467, qvm.getBssLen());
        Assert.assertEquals(-36, qvm.getData().get(0).intValue());
        Assert.assertEquals(976, qvm.getData().get(1).intValue());
        Assert.assertEquals(-829, qvm.getData().get(2).intValue());
        Assert.assertEquals(29, qvm.getData().get(3).intValue());
        Assert.assertEquals(QvmInstruction.OpCode.BREAK, qvm.getInstructions().get(0).getOpcode());
        Assert.assertEquals(QvmInstruction.OpCode.CONST, qvm.getInstructions().get(1).getOpcode());
        Assert.assertEquals(5678, qvm.getInstructions().get(1).getParameter());
        Assert.assertEquals(QvmInstruction.OpCode.IGNORE, qvm.getInstructions().get(2).getOpcode());
        Assert.assertArrayEquals("Hello world".getBytes(), qvm.getLit());

    }
}
