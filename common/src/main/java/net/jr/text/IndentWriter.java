package net.jr.text;

import net.jr.test.Assert;
import net.jr.util.StringUtil;

import java.io.IOException;
import java.io.Writer;

public class IndentWriter extends Writer {

    private Writer wrapped;

    private String indentStr;

    private String indentPrefix = "";

    private int indentationLevel = 0;

    private boolean needIndent = true;

    public IndentWriter(Writer wrapped, String indentStr) {
        this.wrapped = wrapped;
        this.indentStr = indentStr;
    }

    public void indent() {
        indentPrefix += indentStr;
        indentationLevel++;
    }

    public void deindent() {
        indentationLevel--;
        Assert.isFalse(indentationLevel < 0);
        indentPrefix = indentPrefix.substring(0, indentPrefix.length() - indentStr.length());
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public void setIndentationLevel(int i) {
        Assert.isTrue(i >= 0);
        indentationLevel = i;
        indentPrefix = StringUtil.repeatTimes(indentStr, i);
    }

    public String getIndentation() {
        return indentPrefix;
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
    public void write(char[] chars, int off, int len) throws IOException {
        if (needIndent) {
            wrapped.write(getIndentation());
            needIndent = false;
        }
        for (int i = off, max = off + len; i < max; i++) {
            char c = chars[i];
            wrapped.write(c);
            if (c == '\n') {
                if (i < max - 1) {
                    wrapped.write(indentPrefix);
                } else {
                    needIndent = true;
                }
            }
        }
    }
}
