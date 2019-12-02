package net.jr.jrc.qvm;

import net.jr.io.HexdumpOutputStream;
import net.jr.jrc.qvm.memory.PagedMemory;
import net.jr.jrc.qvm.syscalls.QvmSyscall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class QvmInterpreter {

    public static final long CODE_START_ADDR = 0x2DL;
    private static final Logger LOGGER = LoggerFactory.getLogger(QvmInterpreter.class);
    private PagedMemory memory;

    private QvmStack stack;

    private Map<Integer, Callable<Integer>> syscalls = new TreeMap<>();

    private boolean goOn;

    public QvmInterpreter(Object... syscallIfaces) {
        for (Object obj : syscallIfaces) {
            prepareSyscalls(obj);
        }
        memory = new PagedMemory();
        stack = new QvmStack(memory);
    }

    private void prepareSyscalls(Object sys) {
        Arrays.asList(sys.getClass().getMethods()).stream().filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers())).filter(m -> m.getReturnType().equals(Integer.TYPE))
                .filter(m -> m.getParameterTypes().length == 1).filter(m -> m.getParameterTypes()[0].equals(QvmInterpreter.class))
                .forEach(m -> {
                    QvmSyscall a = m.getDeclaredAnnotation(QvmSyscall.class);
                    if (a != null) {
                        syscalls.put(a.value(), () -> (Integer) m.invoke(sys, QvmInterpreter.this));
                    }
                });
    }

    public int run(QvmFile qvmExe) {

        // pre-compute adresses for all instructions (speeds up branching)
        long addr = CODE_START_ADDR;
        Map<Long, QvmInstruction> instructions = new TreeMap<>();
        for (QvmInstruction instr : qvmExe.getInstructions()) {
            instructions.put(addr, instr);
            addr += instr.getSize();
        }

        // prepare memory : data section (write each value, starting at address 0)
        int offset = 0;
        OutputStream sectionsWriter = memory.createWriter(0);
        for (int val : qvmExe.getData()) {
            try {
                Endian.write4Le(val, sectionsWriter);
                offset += 4;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // prepare memory : bss (skip length for bss data)
        int bssLen = qvmExe.getBssLen();
        int bssAlign = bssLen == 0 ? 0 : 4 - bssLen % 4;
        offset += bssLen + bssAlign;

        // lit section
        sectionsWriter = memory.createWriter(offset);
        try {
            sectionsWriter.write(qvmExe.getLit());
        } catch (IOException e) {
            throw new RuntimeException();
        }

        // here we go!
        Registers regs = new Registers();
        regs.pc = CODE_START_ADDR;
        goOn = true;
        while (goOn) {
            QvmInstruction instruction = instructions.get(regs.pc);
            if (instruction == null) {
                throw new IllegalStateException(String.format("illegal code address: 0x%08x", regs.pc));
            }
            regs.pc = step(regs, instruction);
        }

        if (stack.isEmpty()) {
            LOGGER.warn("stack is empty");
        }
        return stack.isEmpty() ? -1 : stack.popInt();
    }

    public void stop() {
        goOn = false;
    }

    protected long step(Registers regs, QvmInstruction instr) {
        if (LOGGER.isDebugEnabled()) {
            dumpRegisters(regs);
            dumpStack();
            LOGGER.debug(instr.toString());
        }
        final QvmInstruction.OpCode opcode = instr.getOpcode();
        long nextPc = regs.pc + instr.getSize();
        switch (opcode) {
            case UNDEF:
                break;
            case IGNORE:
                break;
            case BREAK:
                // exits the vm
                goOn = false;
                break;
            case CALL: {
                int callAddr = stack.popInt();

                // negative addresses are mapped to sys calls
                if (callAddr < 0) {
                    Callable<Integer> syscall = syscalls.get(-callAddr);
                    if (syscall == null) {
                        throw new IllegalArgumentException(String.format("invalid syscall (%d)", nextPc));
                    }
                    try {
                        stack.pushInt(syscall.call());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                // normal calls
                else {
                    stack.pushUInt(regs.pc + 1); // push return address
                    stack.pushUInt(regs.fp); // push FP
                    regs.fp = stack.getTopOfStack(); // update FP
                    nextPc = callAddr;// will jump to the called address
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));
                }
            }
            break;
            case ENTER: {
                // pushes the value of LP
                stack.pushUInt(regs.lp);
                // pushes the value of AP
                stack.pushUInt(regs.ap);
                // grows the stack by n
                stack.grow(instr.getParameter());
                // make LP as being the stop of stack
                regs.lp = stack.getTopOfStack();
            }
            break;
            case LOCAL: {
                int offset = instr.getParameter();
                stack.pushUInt(regs.lp + offset);
            }
            break;
            case ARGS: {
                int size = instr.getParameter();
                regs.ap = regs.fp - 8 - size;
            }
            break;
            case ARG: {
                // writes at adress of the argument
                int offset = instr.getParameter();
                int value = stack.popInt();
                stack.writeInt(regs.ap + offset, value);
            }
            break;
            case LEAVE: {
                int value = stack.popInt();
                stack.setTopOfStack(regs.lp);
                stack.shrink(instr.getParameter());
                regs.ap = stack.popUInt();
                regs.lp = stack.popUInt();
                regs.fp = stack.popUInt();
                nextPc = stack.popUInt();
                LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                // push the value
                stack.pushInt(value);
            }
            break;
            case PUSH:
                stack.pushInt(0);
                break;
            case POP:
                stack.popInt();
                break;
            case CONST: {
                int value = instr.getParameter();
                stack.pushInt(value);
            }
            break;
            case JUMP: {
                nextPc = stack.popUInt();
                LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

            }
            break;
            case EQ: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis == tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case NE: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis != tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LTI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis < tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LEI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis <= tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GTI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis > tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GEI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                if (nis >= tos) {
                    nextPc = instr.getParameterUInt();
                }
            }
            break;
            case LTU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                if (nis < tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LEU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                if (nis <= tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GTU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                if (nis > tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GEU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                if (nis >= tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case EQF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis == tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case NEF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis != tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LTF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis < tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LEF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis <= tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GTF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis > tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case GEF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                if (nis >= tos) {
                    nextPc = instr.getParameterUInt();
                    LOGGER.debug(String.format("Jumping to 0x%08x", nextPc));

                }
            }
            break;
            case LOAD1: {
                long addr = stack.popUInt();
                byte[] tmp = new byte[1];
                memory.read(addr, tmp);
                stack.pushInt(tmp[0]);
            }
            break;
            case LOAD2: {
                long addr = stack.popUInt();
                byte[] tmp = new byte[2];
                memory.read(addr, tmp);
                stack.pushInt(Endian.read2Le(tmp));
            }
            break;
            case LOAD4: {
                long addr = stack.popUInt();
                byte[] tmp = new byte[4];
                memory.read(addr, tmp);
                stack.pushInt(Endian.read4Le(tmp));
            }
            break;
            case STORE1: {
                int b = stack.popInt() & 0x000000ff;
                long addr = stack.popUInt();
                byte[] tmp = new byte[]{(byte) b};
                memory.write(addr, tmp);
            }
            break;
            case STORE2: {
                int s = stack.popInt() & 0x0000ffff;
                byte[] tmp = new byte[2];
                Endian.write2Le((short) s, tmp);
                long addr = stack.popUInt();
                memory.write(addr, tmp);
            }
            break;
            case STORE4: {
                int value = stack.popInt();
                long addr = stack.popUInt();
                byte[] tmp = new byte[4];
                Endian.write4Le(value, tmp);
                memory.write(addr, tmp);
            }
            break;
            case SEX8:
                stack.pushInt(stack.popInt() & 0x000000ff);
                break;
            case SEX16:
                stack.pushInt(stack.popInt() & 0x0000ffff);
                break;
            case NEGI:
                stack.pushInt(-stack.popInt());
                break;
            case ADD: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis + tos);
            }
            break;
            case SUB: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis - tos);
            }
            break;
            case DIVI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis / tos);
            }
            break;
            case DIVU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis / tos);
            }
            break;
            case MODI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis % tos);
            }
            break;
            case MODU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis % tos);
            }
            break;
            case MULI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis * tos);
            }
            break;
            case MULU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis * tos);
            }
            break;
            case BAND: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis & tos);
            }
            break;
            case BOR: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis | tos);
            }
            break;
            case BXOR: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushUInt(nis ^ tos);
            }
            break;
            case BCOM: {
                int tos = stack.popInt();
                stack.pushUInt(~tos);
            }
            break;
            case LSH: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis << tos);
            }
            break;
            case RSHI: {
                int tos = stack.popInt();
                int nis = stack.popInt();
                stack.pushInt(nis >> tos);
            }
            break;
            case RSHU: {
                long tos = stack.popUInt();
                long nis = stack.popUInt();
                stack.pushInt((int) (nis >> tos));
            }
            break;
            case NEGF: {
                float tos = stack.popFloat();
                stack.pushFloat(-tos);
            }
            break;
            case ADDF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                stack.pushFloat(nis + tos);
            }
            break;
            case SUBF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                stack.pushFloat(nis - tos);
            }
            break;
            case DIVF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                stack.pushFloat(nis / tos);
            }
            break;
            case MULF: {
                float tos = stack.popFloat();
                float nis = stack.popFloat();
                stack.pushFloat(nis * tos);
            }
            break;
            case CVIF: {
                int tos = stack.popInt();
                stack.pushFloat(tos);
            }
            break;
            case CVFI: {
                float tos = stack.popFloat();
                stack.pushInt((int) tos);
            }
            break;
            case BLOCK_COPY: {
                byte[] buf = new byte[instr.getParameter()];
                memory.read(stack.getTopOfStack(), buf);
                stack.setTopOfStack(stack.getTopOfStack() - buf.length);
                memory.write(stack.getTopOfStack(), buf);
            }
            break;
            case SWAP: {
                int i0 = stack.popInt();
                int i1 = stack.popInt();
                stack.pushInt(i0);
                stack.pushInt(i1);
            }
            break;
            default:
                throw new UnsupportedOperationException("unsupported opcode " + opcode);
        }
        return nextPc;
    }

    protected void dumpStack() {
        try {
            long tos = stack.getTopOfStack();
            String s = "\n" + HexdumpOutputStream.readFully(memory.createReader(tos), tos);
            LOGGER.debug(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void dumpRegisters(Registers regs) {
        LOGGER.debug(String.format("pc=0x%08x,fp=0x%08x,ap=0x%08x,lp=0x%08x", regs.pc, regs.fp, regs.ap, regs.lp));
    }

    public QvmStack getStack() {
        return stack;
    }

    private static class Registers {

        long ap, lp, fp, pc;
    }
}
