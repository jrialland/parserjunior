package net.jr.jrc.qvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public class QvmInstruction {

    private OpCode opcode;
    private int parameter;

    public QvmInstruction(OpCode opcode) {
        this.opcode = opcode;
    }

    public QvmInstruction(OpCode opcode, int parameter) {
        this(opcode);
        this.parameter = parameter;
    }

    public static QvmInstruction read(InputStream is) throws IOException {
        OpCode opcode = OpCode.byVal(is.read());
        int param;
        switch (opcode.getParameterSize()) {
            case 1:
                param = is.read();
                break;
            case 4:
                param = Endian.read4Le(is);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return new QvmInstruction(opcode, param);
    }

    public OpCode getOpcode() {
        return opcode;
    }

    public int getParameter() {
        return parameter;
    }

    public void setParameter(int parameter) {
        this.parameter = parameter;
    }

    public long getParameterUInt() {
        return parameter & 0X00000000FFFFFFFFL;
    }

    public void setParameterUInt(long parameterUInt) {
        setParameter((int) (parameterUInt & 0X00000000FFFFFFFFL));
    }

    public float getParameterFloat() {
        return Float.intBitsToFloat(parameter);
    }

    public void setParameterFloat(float parameterFloat) {
        setParameter(Float.floatToRawIntBits(parameterFloat));
    }

    public int getSize() {
        return 1 + opcode.getParameterSize();
    }

    public void write(OutputStream os) throws IOException {
        os.write(opcode.getCode());
        if (opcode.getParameterSize() == 1) {
            os.write(getParameter());
        } else if (opcode.getParameterSize() == 4) {
            Endian.write4Le(getParameter(), os);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(opcode.name());
        if (opcode.getParameterSize() > 0) {
            sb.append(" ");
            sb.append(Integer.toString(parameter));
        }
        return sb.toString();
    }

    public enum ParameterType {
        None(0),
        UInt8(1),
        UInt32(4);
        private int size;

        ParameterType(int size) {
            this.size = size;
        }

        public int size() {
            return size;
        }
    }

    public enum OpCode {
        UNDEF(0, ParameterType.None, "undefined opcode."),
        IGNORE(1, ParameterType.None, "no-operation (nop) instruction."),
        BREAK(2, ParameterType.None, "??"),
        ENTER(3, ParameterType.UInt32, "Begin procedure body, adjust stack $PARM octets for frame (always at least 8 (i.e. 2 words)). Frame contains all local storage/variables and arguments space for any calls within this procedure."),
        LEAVE(4, ParameterType.UInt32, "End procedure body, $PARM is same as that of the matching ENTER."),
        CALL(5, ParameterType.None, "make call to procedure (code address <- TOS)."),
        PUSH(6, ParameterType.None, "push nonsense (void) value to opstack (TOS <- 0)."),
        POP(7, ParameterType.None, "pop a value from stack (remove TOS, decrease stack by 1)."),
        CONST(8, ParameterType.UInt32, "push literal value onto stack (TOS <- $PARM)"),
        LOCAL(9, ParameterType.UInt32, "get address of local storage (local variable or argument) (TOS <- (frame + $PARM))."),
        JUMP(10, ParameterType.None, "branch (code address <- TOS)"),
        EQ(11, ParameterType.UInt32, "check equality (signed integer) (compares NIS vs TOS, jump to $PARM if true)."),
        NE(12, ParameterType.UInt32, "check inequality (signed integer) (NIS vs TOS, jump to $PARM if true)."),
        LTI(13, ParameterType.UInt32, "check less-than (signed integer) (NIS vs TOS, jump to $PARM if true)."),
        LEI(14, ParameterType.UInt32, "check less-than or equal-to (signed integer) (NIS vs TOS, jump to $PARM if true)."),
        GTI(15, ParameterType.UInt32, "check greater-than (signed integer) (NIS vs TOS), jump to $PARM if true."),
        GEI(16, ParameterType.UInt32, "check greater-than or equal-to (signed integer) (NIS vs TOS), jump to $PARM if true."),
        LTU(17, ParameterType.UInt32, "check less-than (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
        LEU(18, ParameterType.UInt32, "check less-than or equal-to (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
        GTU(19, ParameterType.UInt32, "check greater-than (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
        GEU(20, ParameterType.UInt32, "check greater-than or equal-to (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
        EQF(21, ParameterType.UInt32, "check equality (float) (NIS vs TOS, jump to $PARM if true)."),
        NEF(22, ParameterType.UInt32, "check inequality (float) (NIS vs TOS, jump to $PARM if true)."),
        LTF(23, ParameterType.UInt32, "check less-than (float) (NIS vs TOS, jump to $PARM if true)."),
        LEF(24, ParameterType.UInt32, "check less-than or equal-to (float) (NIS vs TOS, jump to $PARM if true)."),
        GTF(25, ParameterType.UInt32, "check greater-than (float) (NIS vs TOS, jump to $PARM if true)."),
        GEF(26, ParameterType.UInt32, "check greater-than or equal-to (float) (NIS vs TOS, jump to $PARM if true)."),
        LOAD1(27, ParameterType.None, "Load 1-octet value from address in TOS (TOS <- [TOS])"),
        LOAD2(28, ParameterType.None, "Load 2-octet value from address in TOS (TOS <- [TOS])"),
        LOAD4(29, ParameterType.None, "Load 4-octet value from address in TOS (TOS <- [TOS])"),
        STORE1(30, ParameterType.None, "lowest octet of TOS is 1-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
        STORE2(31, ParameterType.None, "lowest two octets of TOS is 2-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
        STORE4(32, ParameterType.None, "TOS is 4-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
        ARG(33, ParameterType.UInt8, "TOS is 4-octet value to store into arguments-marshalling space of the indicated octet offset (ARGS[offset] <- TOS)."),
        BLOCK_COPY(34, ParameterType.UInt8, "duplicates n bytes on the top of stack"),
        SEX8(35, ParameterType.None, "Sign-extend 8-bit (TOS <- TOS)."),
        SEX16(36, ParameterType.None, "Sign-extend 16-bit (TOS <- TOS)."),
        NEGI(37, ParameterType.None, "Negate signed integer (TOS <- -TOS)."),
        ADD(38, ParameterType.None, "Add integer-wise (TOS <- NIS + TOS)."),
        SUB(39, ParameterType.None, "Subtract integer-wise (TOS <- NIS - TOS)."),
        DIVI(40, ParameterType.None, "Divide integer-wise (TOS <- NIS / TOS)."),
        DIVU(41, ParameterType.None, "Divide unsigned integer (TOS <- NIS / TOS)."),
        MODI(42, ParameterType.None, "Modulo (signed integer) (TOS <- NIS mod TOS)."),
        MODU(43, ParameterType.None, "Modulo (unsigned integer) (TOS <- NIS mod TOS)."),
        MULI(44, ParameterType.None, "Multiply (signed integer) (TOS <- NIS * TOS)."),
        MULU(45, ParameterType.None, "Multiply (unsigned integer) (TOS <- NIS * TOS)."),
        BAND(46, ParameterType.None, "Bitwise AND (TOS <- NIS & TOS)."),
        BOR(47, ParameterType.None, "Bitwise OR (TOS <- NIS | TOS)."),
        BXOR(48, ParameterType.None, "Bitwise XOR (TOS <- NIS ^ TOS)."),
        BCOM(49, ParameterType.None, "Bitwise complement (TOS <- ~TOS)."),
        LSH(50, ParameterType.None, "Bitwise left-shift (TOS <- NIS << TOS)."),
        RSHI(51, ParameterType.None, "Algebraic (signed) right-shift (TOS <- NIS >> TOS)."),
        RSHU(52, ParameterType.None, "Bitwise (unsigned) right-shift (TOS <- NIS >> TOS)."),
        NEGF(53, ParameterType.None, "Negate float value (TOS <- -TOS)."),
        ADDF(54, ParameterType.None, "Add floats (TOS <- NIS + TOS)."),
        SUBF(55, ParameterType.None, "Subtract floats (TOS <- NIS - TOS)."),
        DIVF(56, ParameterType.None, "Divide floats (TOS <- NIS / TOS)."),
        MULF(57, ParameterType.None, "Multiply floats (TOS <- NIS x TOS)."),
        CVIF(58, ParameterType.None, "Convert signed integer to float (TOS <- TOS)."),
        CVFI(59, ParameterType.None, "Convert float to signed integer (TOS <- TOS)."),
        ARGS(60, ParameterType.UInt8, "declares the size of the arguments-marshalling space of the current function (see doc)"),
        SWAP(61, ParameterType.None, "Swaps NIS and TOS (as 4-bytes values)"),
        ;

        private static final Map<Integer, OpCode> BYVAL = new TreeMap<>();

        static {
            for (OpCode op : OpCode.values()) {
                BYVAL.put(op.code, op);
            }
        }

        private int code;

        private ParameterType parameterType;

        private String description;

        OpCode(int code, ParameterType parameterType, String description) {
            this.code = code;
            this.parameterType = parameterType;
            this.description = description;
        }

        public static OpCode byVal(int val) {
            return BYVAL.get(val);
        }

        public int getParameterSize() {
            return parameterType.size();
        }

        public byte getCode() {
            return (byte) code;
        }

        public String getDescription() {
            return description;
        }
    }
}
