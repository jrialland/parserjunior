package net.jr.jrc.qvm.memory;

public class MemoryException extends IllegalArgumentException {

    private static final long serialVersionUID = -5755315955218679827L;

    public MemoryException(long addr, boolean write) {
        this(makeMsg(addr, write));
    }

    public MemoryException(String message) {
        super(message);
    }

    private static final String makeMsg(long addr, boolean write) {
        StringBuilder sb = new StringBuilder(String.format("cannot %s memory at 0x%08x", write ? "write" : "read", addr));
        return sb.toString();
    }
}
