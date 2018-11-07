
package net.jr.jrc.qvm.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.TreeMap;

public class PagedMemory {

    private static final int DEFAULT_PAGE_SIZE = 1024 * 4;// page = 4k

    private static final long MAX_ADDR = 2 + ((long) Integer.MAX_VALUE) * 2;

    private interface Page {
        int write(long addr, byte[] data, int offset, int len);

        int read(long addr, byte[] data, int offset, int len);

        long getBaseAddr();
    }

    private class FakePage implements Page {
        private long baseAddr;

        public FakePage(long baseAddr) {
            this.baseAddr = baseAddr;
        }

        @Override
        public long getBaseAddr() {
            return baseAddr;
        }

        @Override
        public int write(long addr, byte[] data, int offset, int len) {
            throw new MemoryException(addr, true);
        }

        @Override
        public int read(long addr, byte[] data, int offset, int len) {
            int arrayOffset = Long.valueOf(addr - baseAddr).intValue();
            int s = Math.min(len, pageSize - arrayOffset);
            Arrays.fill(data, offset, s, (byte) 0);
            return s;
        }
    }

    /**
     * "real" pages store data into an allocated byte array
     */
    private class ArrayPage implements Page {

        long baseAddr;

        byte[] pageData;

        public ArrayPage(long baseAddr) {
            this.baseAddr = baseAddr;
            this.pageData = new byte[pageSize];
        }

        @Override
        public int write(long addr, byte[] data, int offset, int len) {
            int arrayOffset = Long.valueOf(addr - baseAddr).intValue();
            int s = Math.min(len, pageSize - arrayOffset);
            System.arraycopy(data, offset, pageData, arrayOffset, s);
            return s;
        }

        @Override
        public int read(long addr, byte[] data, int offset, int len) {
            int arrayOffset = Long.valueOf(addr - baseAddr).intValue();
            int s = Math.min(len, pageSize - arrayOffset);
            System.arraycopy(pageData, arrayOffset, data, offset, s);
            return s;
        }

        @Override
        public long getBaseAddr() {
            return baseAddr;
        }
    }

    private TreeMap<Long, ArrayPage> pages;

    private int pageSize;

    private Page currentPage = null;

    public PagedMemory(int pageSize) {
        this.pages = new TreeMap<>();
        this.pageSize = pageSize;
    }

    public PagedMemory() {
        this(DEFAULT_PAGE_SIZE);
    }

    /**
     * finds the page corresponding to the asked address
     * @param pageStart must be a page boundary/start address (i.e a multiple of pageSize)
     * @param rw if we need to actually write to the page
     * @return
     */
    private Page getPage(long pageStart, boolean rw) {
        ArrayPage p = pages.get(pageStart);
        if (p == null) {
            if (rw = false) {
                return new FakePage(pageStart);
            } else {
                p = new ArrayPage(pageStart);
                pages.put(pageStart, p);
            }
        }
        return p;
    }

    private long getPageStart(long addr, boolean rw) {
        if (!isReadable(addr)) {
            throw new MemoryException(addr, rw);
        }
        return (addr / pageSize) * pageSize;
    }

    public void read(long addr, byte[] destination) {
        read(addr, destination, 0, destination.length);
    }

    public void read(long addr, byte[] destination, int offset, int len) {
        if (!isReadable(addr)) {
            throw new MemoryException(addr, false);
        }
        while (len > 0) {
            long pageStart = getPageStart(addr, false);
            if (currentPage == null || pageStart != currentPage.getBaseAddr()) {
                currentPage = getPage(pageStart, false);
            }
            int read = currentPage.read(addr, destination, offset, len);
            addr += read;
            offset += read;
            len -= read;
        }
    }

    public void write(long addr, byte[] array) {
        write(addr, array, 0, array.length);
    }

    public void write(long addr, byte[] array, int offset, int len) {
        while (len > 0) {
            long pageStart = getPageStart(addr, true);
            if (currentPage == null || pageStart != currentPage.getBaseAddr()) {
                currentPage = getPage(pageStart, true);
            }
            int written = currentPage.write(addr, array, offset, len);
            addr += written;
            offset += written;
            len -= written;
        }
    }

    public int getMaxPages() {
        return Long.valueOf(((long) Integer.MAX_VALUE) * 2 / pageSize).intValue();
    }

    public int getUsedPages() {
        return pages.size();
    }

    public boolean isReadable(long addr) {
        return addr >= 0 && addr < MAX_ADDR;
    }

    /**
     * clears the whole memory
     */
    public void clear() {
        pages.clear();
    }

    public void clearPage(long pageAddr) {
        if (!isReadable(pageAddr)) {
            throw new MemoryException(String.format("0x%08x is not a valid address", pageAddr));
        }
        if (pageAddr != getPageStart(pageAddr, true)) {
            throw new IllegalArgumentException(
                    String.format("0x%08x is not the start address of a page (but 0x%08x is)", pageAddr, getPageStart(pageAddr, true)));
        }
        pages.remove(pageAddr);
    }

    public OutputStream createWriter(final long startAddr) {
        return new OutputStream() {

            private long addr = startAddr;

            @Override
            public void write(byte[] bytes) throws IOException {
                write(bytes, 0, bytes.length);
            }

            @Override
            public void write(int i) throws IOException {
                write(new byte[]{(byte) i});
            }

            @Override
            public void write(byte[] bytes, int offset, int len) throws IOException {
                PagedMemory.this.write(addr, bytes, offset, len);
                addr += len;
            }
        };
    }

    public InputStream createReader(final long baseAddr) {
        return createReader(baseAddr, getMaxAddr() - baseAddr);
    }

    public InputStream createReader(final long baseAddr, final long maxBytes) {
        if(baseAddr>getMaxAddr()){
          throw new MemoryException(baseAddr, false);
        }
        return new InputStream() {

            private long addr = baseAddr;

            private long remaining = Math.min(getMaxAddr() - baseAddr, maxBytes);

            private byte[] tmp = new byte[1];

            @Override
            public int read() throws IOException {
                if (read(tmp) > 0) {
                    return tmp[0];
                } else {
                    return -1;
                }
            }

            @Override
            public int read(byte[] bytes) throws IOException {
                return read(bytes, 0, bytes.length);
            }

            @Override
            public int read(byte[] bytes, int offset, int len) throws IOException {
                if (remaining == 0) {
                    return -1;
                }
                int r = (int) Math.min(remaining, len);
                PagedMemory.this.read(addr, bytes, offset, r);
                remaining -= r;
                return r;
            }
        };
    }

    public long getMaxAddr() {
        return MAX_ADDR;
    }

    public long getPageSize() {
        return pageSize;
    }
}
