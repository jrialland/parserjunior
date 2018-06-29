package net.jr.cpreproc.pipe;

import net.jr.common.Position;
import net.jr.pipes.PipeableProcessor;

import java.io.*;
import java.nio.file.Path;

public class Suppliers {

    public static class DefaultSupplier extends PipeableProcessor<Void, String> {

        private BufferedReader reader;

        private Position position;

        private DefaultSupplier(Reader reader, String filename) {
            this.reader = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
            this.position = new Position(0, 1);
            this.position.setFilename(filename);
        }

        @Override
        public String get() {
            try {
                String s = reader.readLine();
                return s;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static PipeableProcessor<Void, String> fromReader(Reader reader, String filename) {
        return new DefaultSupplier(reader, filename);
    }

    public static PipeableProcessor<Void, String> fromString(String txt) {
        return fromReader(new StringReader(txt), "<str>");
    }

    public static PipeableProcessor<Void, String> fromFile(Path path) {
        final Reader reader;
        try {
            InputStream is = path.toUri().toURL().openStream();
            reader = new InputStreamReader(is);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return fromReader(reader, path.getFileName().toString());
    }


}
