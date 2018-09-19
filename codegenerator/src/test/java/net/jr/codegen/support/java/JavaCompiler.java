package net.jr.codegen.support.java;

import net.jr.io.IOUtil;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.util.*;

public final class JavaCompiler {

    private abstract static class Mem extends SimpleJavaFileObject {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public Mem(Kind kind, URI uri) {
            super(uri, kind);
        }

        public Mem(Kind kind, URI uri, byte[] data) {
            this(kind, uri);
            try {
                baos.write(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long getLastModified() {
            return super.getLastModified();
        }

        @Override
        public CharSequence getCharContent(boolean b) throws IOException {
            return baos.toString();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return baos;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(baos.toByteArray());
        }

        public byte[] getBytes() {
            return baos.toByteArray();
        }
    }

    private static class MemSrc extends Mem {
        public MemSrc(String name, byte[] src) {
            super(Kind.SOURCE, URI.create("file:///" + name.replace('.', '/') + ".java"), src);
        }
    }

    private static class MemBytecode extends Mem {
        public MemBytecode(String name) {
            super(Kind.CLASS, URI.create("byte:///" + name + ".class"));
        }
    }

    private static class MutableClassLoader extends ClassLoader {

        private Map<String, MemBytecode> extraClasses = new HashMap<>();

        private MutableClassLoader() {
            super(null);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            MemBytecode memBytecode = extraClasses.get(name);
            if (memBytecode == null) {
                memBytecode = extraClasses.get(name.replaceFirst(".", "/"));
                if (memBytecode == null) {
                    return super.findClass(name);
                }
            }
            byte[] byteCode = memBytecode.getBytes();
            return defineClass(name, byteCode, 0, byteCode.length);
        }

        public void addClass(String name, MemBytecode m) {
            extraClasses.put(name, m);
        }

    }

    private static final MutableClassLoader classLoader = new MutableClassLoader();

    public static final ClassLoader getClassLoader() {
        return classLoader;
    }

    private static class SimpleFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {


        public SimpleFileManager(StandardJavaFileManager sjfm) {
            super(sjfm);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            MemBytecode m = new MemBytecode(name);
            classLoader.addClass(name, m);
            return m;
        }
    }

    public static Class<?> compile(String name, Reader code) {

        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager sjfm = compiler.getStandardFileManager(null, null, null);
        SimpleFileManager sfm = new SimpleFileManager(sjfm);
        List<? extends JavaFileObject> compilationUnits = Arrays.asList(new MemSrc(name, IOUtil.readFully(code).getBytes()));
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        javax.tools.JavaCompiler.CompilationTask task = compiler.getTask(IOUtil.devNull(), sfm, collector, Collections.emptyList(), null, compilationUnits);

        if (task.call()) {
            try {
                Class<?> clazz = classLoader.findClass(name);
                if (clazz != null) {
                    return clazz;
                } else {
                    throw new IllegalStateException("class '" + name + "' could not be found");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            String err = collector.getDiagnostics().stream().map(d ->
                    String.format("%d:%d - %s", d.getLineNumber(), d.getColumnNumber(), d.getMessage(Locale.getDefault()))
            ).reduce("", (s1, s2) -> s1 + "\n" + s2);
            throw new IllegalStateException(err);
        }

    }


}
