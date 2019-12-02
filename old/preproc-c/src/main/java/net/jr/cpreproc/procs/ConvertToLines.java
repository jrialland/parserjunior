package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.pipes.PipeableProcessor;

/**
 * Convert lines of the input file into {@link PreprocessorLine}s
 */
public class ConvertToLines extends PipeableProcessor<String, PreprocessorLine> {

    private PipeableProcessor<?, String> in;

    private Position position;

    public ConvertToLines() {
        position = Position.beforeStart();
    }

    @Override
    public PreprocessorLine get() {
        String s = getSource().get();
        return s == null ? null : new PreprocessorLine(position = position.nextLine(), s);
    }

    @Override
    public String getFilename() {
        return getSource().getFilename();
    }
}
