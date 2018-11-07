package net.jr.cpreproc.pipe;

import net.jr.common.Position;
import net.jr.cpreproc.procs.PreprocessorLine;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

public class Sinks {

    public static class WriterSink implements Consumer<PreprocessorLine> {

        private Writer writer;

        private Position position;

        private boolean addLineDirectives = true;

        public WriterSink(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void accept(PreprocessorLine token) {
            try {
                if (addLineDirectives && position != null) {
                    final Position tokenPos = token.getPosition();
                    if (!(tokenPos.isSameLine(position) || tokenPos.isNextLineOf(position))) {
                        writer.write(String.format("#line %d \"%s\"\n", tokenPos.getLine(), tokenPos.getFilename()));
                    }
                }
                position = token.getPosition();
                writer.write(token.getText() + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void setAddLineDirectives(boolean addLineDirectives) {
            this.addLineDirectives = addLineDirectives;
        }

    }

    public static WriterSink writerSink(Writer writer) {
        return new WriterSink(writer);
    }

}
