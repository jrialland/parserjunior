package net.jr.cpreproc.procs;


import net.jr.pipes.PipeableProcessor;

/**
 * Merges 'continued' lines (the ones ending with antislash-return) into single {@link PreprocessorLine}s
 */
public class ContinuedLinesMerger extends PipeableProcessor<PreprocessorLine, PreprocessorLine> {

    @Override
    public PreprocessorLine get() {
        PreprocessorLine t = getSource().get();
        while (t != null && t.getText().endsWith("\\")) {
            String newVal = t.getText();
            newVal = newVal.substring(0, newVal.length() - 1);//remove trailing antislash
            t.setText(newVal);
            PreprocessorLine next = getSource().get();
            if (next != null) {
                t = t.mergeWith(next);
            }
        }
        return t;
    }
}
