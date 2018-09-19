
package net.jr.jrc.qvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public class QvmInstruction {

  public enum OpCode {
      UNDEF(0, 0, "undefined opcode."),
      IGNORE(1, 0, "no-operation (nop) instruction."),
      BREAK(2, 0, "??"),
      ENTER(3, 4, "Begin procedure body, adjust stack $PARM octets for frame (always at least 8 (i.e. 2 words)). Frame contains all local storage/variables and arguments space for any calls within this procedure."),
      LEAVE(4, 4, "End procedure body, $PARM is same as that of the matching ENTER."),
      CALL(5, 0, "make call to procedure (code address <- TOS)."),
      PUSH(6, 0, "push nonsense (void) value to opstack (TOS <- 0)."),
      POP(7, 0, "pop a value from stack (remove TOS, decrease stack by 1)."),
      CONST(8, 4, "push literal value onto stack (TOS <- $PARM)"),
      LOCAL(9, 4, "get address of local storage (local variable or argument) (TOS <- (frame + $PARM))."),
      JUMP(10, 0, "branch (code address <- TOS)"),
      EQ(11, 4, "check equality (signed integer) (compares NIS vs TOS, jump to $PARM if true)."),
      NE(12, 4, "check inequality (signed integer) (NIS vs TOS, jump to $PARM if true)."),
      LTI(13, 4, "check less-than (signed integer) (NIS vs TOS, jump to $PARM if true)."),
      LEI(14, 4, "check less-than or equal-to (signed integer) (NIS vs TOS, jump to $PARM if true)."),
      GTI(15, 4, "check greater-than (signed integer) (NIS vs TOS), jump to $PARM if true."),
      GEI(16, 4, "check greater-than or equal-to (signed integer) (NIS vs TOS), jump to $PARM if true."),
      LTU(17, 4, "check less-than (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
      LEU(18, 4, "check less-than or equal-to (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
      GTU(19, 4, "check greater-than (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
      GEU(20, 4, "check greater-than or equal-to (unsigned integer) (NIS vs TOS), jump to $PARM if true."),
      EQF(21, 4, "check equality (float) (NIS vs TOS, jump to $PARM if true)."),
      NEF(22, 4, "check inequality (float) (NIS vs TOS, jump to $PARM if true)."),
      LTF(23, 4, "check less-than (float) (NIS vs TOS, jump to $PARM if true)."),
      LEF(24, 4, "check less-than or equal-to (float) (NIS vs TOS, jump to $PARM if true)."),
      GTF(25, 4, "check greater-than (float) (NIS vs TOS, jump to $PARM if true)."),
      GEF(26, 4, "check greater-than or equal-to (float) (NIS vs TOS, jump to $PARM if true)."),
      LOAD1(27, 0, "Load 1-octet value from address in TOS (TOS <- [TOS])"),
      LOAD2(28, 0, "Load 2-octet value from address in TOS (TOS <- [TOS])"),
      LOAD4(29, 0, "Load 4-octet value from address in TOS (TOS <- [TOS])"),
      STORE1(30, 0, "lowest octet of TOS is 1-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
      STORE2(31, 0, "lowest two octets of TOS is 2-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
      STORE4(32, 0, "TOS is 4-octet value to store, destination address in next-in-stack ([NIS] <- TOS)."),
      ARG(33, 1, "TOS is 4-octet value to store into arguments-marshalling space of the indicated octet offset (ARGS[offset] <- TOS)."),
      BLOCK_COPY(34, 1, "duplicates n bytes on the top of stack"),
      SEX8(35, 0, "Sign-extend 8-bit (TOS <- TOS)."),
      SEX16(36, 0, "Sign-extend 16-bit (TOS <- TOS)."),
      NEGI(37, 0, "Negate signed integer (TOS <- -TOS)."),
      ADD(38, 0, "Add integer-wise (TOS <- NIS + TOS)."),
      SUB(39, 0, "Subtract integer-wise (TOS <- NIS - TOS)."),
      DIVI(40, 0, "Divide integer-wise (TOS <- NIS / TOS)."),
      DIVU(41, 0, "Divide unsigned integer (TOS <- NIS / TOS)."),
      MODI(42, 0, "Modulo (signed integer) (TOS <- NIS mod TOS)."),
      MODU(43, 0, "Modulo (unsigned integer) (TOS <- NIS mod TOS)."),
      MULI(44, 0, "Multiply (signed integer) (TOS <- NIS * TOS)."),
      MULU(45, 0, "Multiply (unsigned integer) (TOS <- NIS * TOS)."),
      BAND(46, 0, "Bitwise AND (TOS <- NIS & TOS)."),
      BOR(47, 0, "Bitwise OR (TOS <- NIS | TOS)."),
      BXOR(48, 0, "Bitwise XOR (TOS <- NIS ^ TOS)."),
      BCOM(49, 0, "Bitwise complement (TOS <- ~TOS)."),
      LSH(50, 0, "Bitwise left-shift (TOS <- NIS << TOS)."),
      RSHI(51, 0, "Algebraic (signed) right-shift (TOS <- NIS >> TOS)."),
      RSHU(52, 0, "Bitwise (unsigned) right-shift (TOS <- NIS >> TOS)."),
      NEGF(53, 0, "Negate float value (TOS <- -TOS)."),
      ADDF(54, 0, "Add floats (TOS <- NIS + TOS)."),
      SUBF(55, 0, "Subtract floats (TOS <- NIS - TOS)."),
      DIVF(56, 0, "Divide floats (TOS <- NIS / TOS)."),
      MULF(57, 0, "Multiply floats (TOS <- NIS x TOS)."),
      CVIF(58, 0, "Convert signed integer to float (TOS <- TOS)."),
      CVFI(59, 0, "Convert float to signed integer (TOS <- TOS)."),
      ARGS(60, 4, "declares the size of the arguments-marshalling space of the current function (see doc)"),
      SWAP(61,0, "Swaps NIS and TOS (as 4-bytes values)"),
      ;

    private static final Map<Integer, OpCode> BYVAL = new TreeMap<>();

    static {
      for (OpCode op : OpCode.values()) {
        BYVAL.put(op.code, op);
      }
    }

    private int code;

    private int parameterSize;

    private String description;

    OpCode(int code, int parameterSize, String description) {
      this.code = code;
      this.parameterSize = parameterSize;
      this.description = description;
    }

    public int getParameterSize() {
      return parameterSize;
    }

    public byte getCode() {
      return (byte) code;
    }

    public String getDescription() {
      return description;
    }

    public static OpCode byVal(int val) {
      return BYVAL.get(val);
    }
  }

  private OpCode opcode;

  private int parameter;

  public QvmInstruction(OpCode opcode) {
    this.opcode = opcode;
  }

  public QvmInstruction(OpCode opcode, int parameter) {
    this(opcode);
    this.parameter = parameter;
  }

  public OpCode getOpcode() {
    return opcode;
  }

  public int getParameter() {
    return parameter;
  }

  public long getParameterUInt() {
    return parameter & 0X00000000FFFFFFFFL;
  }

  public float getParameterFloat() {
    return Float.intBitsToFloat(parameter);
  }

  public void setParameter(int parameter) {
    this.parameter = parameter;
  }

  public void setParameterFloat(float parameterFloat) {
    setParameter(Float.floatToRawIntBits(parameterFloat));
  }

  public void setParameterUInt(long parameterUInt) {
    setParameter((int) (parameterUInt & 0X00000000FFFFFFFFL));
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

  public static QvmInstruction read(InputStream is) throws IOException {
    OpCode opcode = OpCode.byVal(is.read());
    int param = 0;
    if (opcode.getParameterSize() == 1) {
      param = is.read();
    } else if (opcode.getParameterSize() == 4) {
      param = Endian.read4Le(is);
    }
    return new QvmInstruction(opcode, param);
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
}
