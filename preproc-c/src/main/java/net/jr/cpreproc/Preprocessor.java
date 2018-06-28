package net.jr.cpreproc;

import net.jr.cpreproc.macrodefs.DateMacroDefinition;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.NoArgsMacroDefinition;
import net.jr.cpreproc.pipe.Sinks;
import net.jr.cpreproc.pipe.Suppliers;
import net.jr.cpreproc.procs.*;
import net.jr.cpreproc.reporting.Reporter;
import net.jr.pipes.PipeableProcessor;
import net.jr.test.Assert;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Preprocessor {

    private Reader reader;

    private String filename;

    private Map<Option, Object> options = new HashMap<>();

    private DirectivesInterpreter directivesInterpreter;

    private static Map<String, MacroDefinition> defaultDefinitions() {
        Map<String, MacroDefinition> defs = new TreeMap<>();
        defs.put(DateMacroDefinition.TimeStamp, DateMacroDefinition.TimeStampDefinition);
        defs.put(DateMacroDefinition.Time, DateMacroDefinition.TimeDefinition);
        defs.put(DateMacroDefinition.Date, DateMacroDefinition.DateDefinition);
        return defs;
    }

    public MacroDefinition getDefinition(String name) {
        return directivesInterpreter.getDefinitions().get(name);
    }

    public Preprocessor() {
        this(defaultDefinitions(), null);
    }

    public Preprocessor(Map<String, MacroDefinition> definitions, Reporter reporter) {
        this.directivesInterpreter = new DirectivesInterpreter(definitions, reporter);
    }

    public void setInput(Reader reader, String filename) {
        this.reader = reader;
        this.filename = filename;
        options.put(Option.NoLineDirectives, false);
        options.put(Option.NoTrigraphs, false);
    }

    public PipeableProcessor<?, PreprocessorLine> getChain() {

        Assert.notNull(reader, "the [reader] has not been set");
        Assert.notNull(filename, "the [filename] has not been set");

        PipeableProcessor<Void, String> supplier = Suppliers.fromReader(reader, filename);
        PipeableProcessor<String, PreprocessorLine> p;

        if (!(Boolean) options.get(Option.NoTrigraphs)) {
            p = supplier.pipeTo(new TrigraphsRemover());
        } else {
            p = supplier.pipeTo(new ConvertToLines());
        }

        return p.pipeTo(new ContinuedLinesMerger())
                .pipeTo(new CommentsRemover())
                .pipeTo(directivesInterpreter);

    }

    public void sinkToWriter(Writer writer) throws IOException {
        Sinks.WriterSink sink = new Sinks.WriterSink(writer);
        sink.setAddLineDirectives(!(Boolean) options.get(Option.NoLineDirectives));
        getChain().stream().forEach(sink);
        writer.flush();
    }

    public String process() {
        StringWriter sw = new StringWriter();
        try {
            sinkToWriter(sw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public void setOption(Option option, boolean value) {
        options.put(option, value);
    }

    public enum Option {
        NoTrigraphs,
        NoLineDirectives
    }

    public void addSimpleDefinition(String macro, String definition) {
        directivesInterpreter.getDefinitions().put(macro, new NoArgsMacroDefinition(macro, definition));
    }

    public DirectivesInterpreter getDirectivesInterpreter() {
        return directivesInterpreter;
    }
}
