package net.jr.pipes;

import net.jr.converters.Converter;
import net.jr.util.StreamUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipeableProcessor<In, Out> implements Supplier<Out> {

    private PipeableProcessor<?, In> source;

    private LinkedList<Out> buffer = new LinkedList<>();

    private boolean eof = false;

    private String filename = "<?>";

    protected PipeableProcessor<?, In> getSource() {
        return source;
    }

    public void setSource(PipeableProcessor<?, In> source) {
        this.source = source;
    }

    public void setSource(Iterator<In> source) {
        setSource(new PipeableProcessor<Void, In>() {
            @Override
            public In get() {
                if (source.hasNext()) {
                    return source.next();
                } else {
                    return null;
                }
            }
        });
    }

    public <Out2> PipeableProcessor<Out, Out2> pipeTo(PipeableProcessor<Out, Out2> processor) {
        processor.setSource(this);
        return processor;
    }

    public PipeableProcessor<Out, Out> callForEach(Consumer<Out> callable) {
        return this.pipeTo(new PipeableProcessor<Out, Out>() {
            @Override
            public Out get() {
                Out item = getSource().get();
                if (item != null) {
                    callable.accept(item);
                }
                return item;
            }
        });
    }

    public <Out2> PipeableProcessor<Out, Out2> convert(Converter<Out, Out2> converter) {
        return convert(converter::convert);
    }

    public <Out2> PipeableProcessor<Out, Out2> convert(Function<Out, Out2> converter) {
        return this.pipeTo(new PipeableProcessor<Out, Out2>() {
            @Override
            public Out2 get() {
                Out item = getSource().get();
                return item == null ? null : converter.apply(item);
            }
        });
    }

    public Stream<Out> stream() {
        return StreamUtil.takeWhile(Stream.generate(this), t -> t != null);
    }

    public void generate(In in, Consumer<Out> out) {
        out.accept((Out) in);
    }

    public void afterLast(Consumer<Out> out) {
    }

    @Override
    public Out get() {
        if (!buffer.isEmpty()) {
            return buffer.removeFirst();
        }
        if (!eof) {
            do {
                In nextIn = getSource().get();
                if (nextIn != null) {
                    generate(nextIn, o -> buffer.add(o));
                } else {
                    eof = true;
                    afterLast(o -> buffer.add(o));
                }
            } while (!eof && buffer.isEmpty());
        }
        return buffer.isEmpty() ? null : buffer.removeFirst();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<Out> list() {
        return stream().collect(Collectors.toList());
    }
}
