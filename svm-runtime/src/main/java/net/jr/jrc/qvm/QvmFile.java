package net.jr.jrc.qvm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * QVM file format.
 * Multi-octet words are little-endian (LE).
 * <p>
 * </p><table border="1">
 * <tbody><tr><td colspan="3">QVM header</td></tr>
 * <tr><td align="RIGHT">offset</td><td align="CENTER">size</td><td>description</td></tr>
 * <tr><td align="RIGHT">0</td><td align="CENTER">4</td><td>magic (0x12721444 LE, 0x44147212 BE)</td></tr>
 * <tr><td align="RIGHT">4</td><td align="CENTER">4</td><td>instruction count</td></tr>
 * <tr><td align="RIGHT">8</td><td align="CENTER">4</td><td>length of CODE segment</td></tr>
 * <tr><td align="RIGHT">12</td><td align="CENTER">4</td><td>offset of CODE segment</td></tr>
 * <tr><td align="RIGHT">16</td><td align="CENTER">4</td><td>lenth of DATA segment</td></tr>
 * <tr><td align="RIGHT">20</td><td align="CENTER">4</td><td>offset of DATA segment</td></tr>
 * <tr><td align="RIGHT">24</td><td align="CENTER">4</td><td>length of LIT segment</td></tr>
 * <tr><td align="RIGHT">28</td><td align="CENTER">4</td><td>length of BSS segment</td></tr>
 * </tbody></table>
 */
public class QvmFile {

    private static final byte[] MAGIC = new byte[]{0x44, 0x14, 0x72, 0x12};

    /**
     * length of the bss section
     */
    private int bssLen = 0;

    /**
     * instructions
     */
    private List<QvmInstruction> instructions = new ArrayList<>();

    /**
     * DATA section
     */
    private List<Integer> data = new ArrayList<>();

    private byte[] lit = new byte[]{};

    public QvmFile() {
        super();
    }

    public static QvmFile read(InputStream is) throws IOException {
        byte[] fileMagic = new byte[4];
        if (is.read(fileMagic) != 4) {
            throw new IOException("read error");
        }
        if (!Arrays.equals(fileMagic, MAGIC)) {
            throw new IllegalArgumentException("invalid magic bytes");
        }

        QvmFile qvm = new QvmFile();

        //instructions count
        /*int instuctionsCount =*/
        Endian.read4Le(is);
        int codeLen = Endian.read4Le(is);
        int dataLen = Endian.read4Le(is);
        int dataOffset = Endian.read4Le(is);
        qvm.bssLen = Endian.read4Le(is);

        //code segment
        int offset = 24;
        while (offset < codeLen + 24) {
            QvmInstruction.OpCode opCode = QvmInstruction.OpCode.byVal(is.read());
            final QvmInstruction instr;
            if (opCode.getParameterSize() == 1) {
                instr = new QvmInstruction(opCode, is.read());
                offset += 2;
            } else if (opCode.getParameterSize() == 4) {
                instr = new QvmInstruction(opCode, Endian.read4Le(is));
                offset += 5;
            } else {
                instr = new QvmInstruction(opCode);
                offset++;
            }
            qvm.instructions.add(instr);
        }

        //realign on 4bytes if necessary
        offset += (int) is.skip(offset % 4 == 0 ? 0 : 4 - offset % 4);

        //data segment
        offset += (int) is.skip(Math.max(0, dataOffset - offset));
        int s = dataLen / 4;
        qvm.data = new ArrayList<>(s);
        for (int i = 0; i < s; i++) {
            qvm.data.add(Endian.read4Le(is));
        }

        //lit segment
        byte[] buff = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c = 0;
        while ((c = is.read(buff)) > -1) {
            baos.write(buff, 0, c);
        }
        qvm.lit = baos.toByteArray();

        return qvm;
    }

    public int getBssLen() {
        return bssLen;
    }

    public void setBssLen(int bssLen) {
        this.bssLen = bssLen;
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public List<QvmInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<QvmInstruction> instructions) {
        this.instructions = instructions;
    }

    public byte[] getLit() {
        return lit;
    }

    public void setLit(byte[] lit) {
        this.lit = lit;
    }

    public int write(OutputStream os) throws IOException {
        int offset = 0;
        //magic
        os.write(MAGIC);
        offset += 4;

        //instructions count
        Endian.write4Le(instructions.size(), os);
        offset += 4;

        //length of CODE segment
        int codeLen = instructions.stream().map(i -> 1 + i.getOpcode().getParameterSize()).collect(Collectors.summingInt(Integer::intValue));
        //align on 4 bytes
        codeLen += codeLen % 4 == 0 ? 0 : 4 - codeLen % 4;
        Endian.write4Le(codeLen, os);
        offset += 4;

        //lenth of DATA segment
        Endian.write4Le(data.size() * 4, os);
        offset += 4;

        //offset of DATA segment
        int dataOffset = 24 + codeLen;
        Endian.write4Le(dataOffset, os);
        offset += 4;

        //length of BSS segment
        Endian.write4Le(bssLen, os);
        offset += 4;

        //copy the entire CODE segment
        for (QvmInstruction instr : instructions) {
            os.write(instr.getOpcode().getCode());
            offset++;
            int paramSize = instr.getOpcode().getParameterSize();
            if (paramSize == 1) {
                os.write(instr.getParameter());
                offset++;
            } else if (paramSize == 4) {
                Endian.write4Le(instr.getParameter(), os);
                offset += 4;
            }
        }

        //align on 4bytes
        while (offset % 4 != 0) {
            os.write(0);
            offset++;
        }

        //copy the DATA segment
        for (int d : data) {
            Endian.write4Le(d, os);
            offset += 4;
        }

        //copy the lit segment
        os.write(lit);
        offset += lit.length;

        os.flush();
        return offset;
    }

}
