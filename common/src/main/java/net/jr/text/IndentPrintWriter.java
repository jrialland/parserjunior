package net.jr.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class IndentPrintWriter extends PrintWriter {

    private static final ThreadLocal<WriterDelegate> tlHolder = new ThreadLocal<>();

    private static class WriterDelegate extends Writer {

        private Writer wrapped;

        public WriterDelegate(Writer wrapped) {
            this.wrapped = wrapped;
            tlHolder.set(this);
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }

        @Override
        public void flush() throws IOException {
            wrapped.flush();
        }

        @Override
        public void write(char[] chars, int i, int i1) throws IOException {
            wrapped.write(chars, i, i1);
        }
    }

    private IndentWriter indentWriter;

    public IndentPrintWriter(Writer writer, String indentStr) {
        super(new WriterDelegate(new IndentWriter(writer, indentStr)));
        indentWriter = (IndentWriter) tlHolder.get().wrapped;
    }

    public void indent() {
        indentWriter.indent();
    }

    public void deindent() {
        indentWriter.deindent();
    }

    public void setIndentationLevel(int lvl) {
        indentWriter.setIndentationLevel(lvl);
    }

    public int getIndentationLevel() {
        return indentWriter.getIndentationLevel();
    }

    public String getIndentation() {
        return indentWriter.getIndentation();
    }

}
