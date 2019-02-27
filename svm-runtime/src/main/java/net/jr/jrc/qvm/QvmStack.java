
package net.jr.jrc.qvm;

import net.jr.jrc.qvm.memory.PagedMemory;

/**
 * The stack is bound to memory, starting at the highest address, and grows downwards.
 */
public class QvmStack {

    private PagedMemory memory;

    private long baseAddr;

    private long topOfStack;

    private byte[] tmp;

    protected QvmStack(PagedMemory memory) {
        this.memory = memory;
        this.baseAddr = memory.getMaxAddr();
        this.topOfStack = this.baseAddr;
        this.tmp = new byte[4];
    }

    public int popInt() {
        memory.read(topOfStack, tmp);
        topOfStack += 4;
        return Endian.read4Le(tmp);
    }

    public float popFloat() {
        memory.read(topOfStack, tmp);
        topOfStack += 4;
        return Endian.read4Lef(tmp);
    }

    public void pushFloat(float f) {
        topOfStack -= 4;
        Endian.write4Lef(f, tmp);
        memory.write(topOfStack, tmp);
    }

    public void pushInt(int i) {
        topOfStack -= 4;
        Endian.write4Le(i, tmp);
        memory.write(topOfStack, tmp);
    }

    public long popUInt() {
        memory.read(topOfStack, tmp);
        topOfStack += 4;
        return Endian.read4ULe(tmp);
    }

    public void pushUInt(long l) {
        topOfStack -= 4;
        Endian.write4ULe(l, tmp);
        memory.write(topOfStack, tmp);
    }

    public void writeInt(long addr, int i) {
        Endian.write4Le(i, tmp);
        memory.write(addr, tmp);
    }

    public long getTopOfStack() {
        return topOfStack;
    }

    public void setTopOfStack(long topOfStack) {
        this.topOfStack = topOfStack;
    }

    public void grow(int space) {
        topOfStack -= space;
    }

    public void shrink(int space) {
        topOfStack += space;
    }

    public boolean isEmpty() {
        return topOfStack == baseAddr;
    }

    public long size() {
        return baseAddr - topOfStack;
    }
}
