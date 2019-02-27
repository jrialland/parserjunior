package net.jr.jrc.qvm.asm;

import net.jr.jrc.qvm.QvmFile;
import net.jr.jrc.qvm.QvmInstruction;
import net.jr.jrc.qvm.QvmInterpreter;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Assembler {

    enum Section {
        Bss,
        Data,
        Code,
        Lit;

        static Section fromName(String name) {
            name = name.trim();
            for (Section s : values()) {
                if (name.equalsIgnoreCase(s.name())) {
                    return s;
                }
            }
            return null;
        }
    }

    private static class Addr {
        String name;
        int offset;
        boolean isCode;

        @Override
        public String toString() {
            return name + " = " + offset + "(isCode:" + isCode + ")";
        }
    }

    private static class Instr {
        QvmInstruction.OpCode opcode;
        int offset;
        boolean resolved = false;
        int parameter;

        public Instr(int offset, QvmInstruction.OpCode opcode) {
            this.offset = offset;
            this.opcode = opcode;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private static Pattern reservePatten = Pattern.compile("reserve ([0-9]+)");

    private Section currentSection = null;

    Function<String, Void> currentReader = (s) -> {
        throw new IllegalStateException("no section specified");
    };

    private Map<Section, Integer> offsets = new HashMap<>();

    private Map<String, Addr> addrs = new HashMap<>();

    private List<Instr> instrs = new ArrayList<>();

    private List<Function<String, Boolean>> resolutionListeners = new ArrayList<>();

    public Assembler() {
        for (Section s : Section.values()) {
            offsets.put(s, 0);
        }
        offsets.put(Section.Code, (int)QvmInterpreter.CODE_START_ADDR);
    }

    public QvmFile assemble(Reader reader) throws IOException {
        Matcher matcher;
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.replaceFirst("#.*$", "").trim();
            if (!line.isEmpty()) {

                if (line.startsWith("section ")) {
                    changeCurrentSection(line.replaceFirst("section ", ""));
                } else if (line.endsWith(":")) {
                    String lbl = line.replaceFirst(":$", "");
                    resolveLabel(lbl);
                } else {
                    currentReader.apply(line);
                }
            }
        }

        //address resolution
        for (Function<String, Boolean> listener : resolutionListeners) {
            if (!listener.apply(null)) {
                throw new RuntimeException("there are unresolved symbols !");
            }
        }

        // At the end
        QvmFile qvmFile = new QvmFile();

        // bss section size
        qvmFile.setBssLen(offsets.get(Section.Bss));


        //write code section
        List<QvmInstruction> code = qvmFile.getInstructions();
        for (Instr instr : instrs) {
            if (!instr.resolved) {
                throw new IllegalStateException();
            }
            code.add(new QvmInstruction(instr.opcode, instr.parameter));
        }

        return qvmFile;

    }

    protected void readBss(String line) {
        Matcher matcher;
        if ((matcher = reservePatten.matcher(line)).matches()) {
            int size = Integer.parseInt(matcher.group(1));
            int currentOffset = offsets.get(Section.Bss);
            //update offset for bss section
            offsets.put(Section.Bss, currentOffset + size);
        } else {
            throw new IllegalArgumentException(line);
        }
    }

    protected void readCode(String line) {
        String[] parts = line.split(" ");
        QvmInstruction.OpCode opcode = QvmInstruction.OpCode.valueOf(parts[0]);

        Instr instr = new Instr(offsets.get(currentSection), opcode);

        if (opcode.getParameterSize() > 0) {
            if (parts.length != 2) {
                throw new IllegalArgumentException(opcode.name() + " : missing parameter");
            }
            readInstrParameter(instr, parts[1]);
        } else {
            if(parts.length > 1) {
                throw new IllegalArgumentException(opcode.name() + " does not take parameters");
            }
            instr.resolved = true;
        }

        // add instruction
        instrs.add(instr);

        //grow code offset
        offsets.put(Section.Code, offsets.get(Section.Code) + 1 + opcode.getParameterSize());
    }

    protected void readInstrParameter(Instr instr, String expr) {
        Expression expression = ExpressionGrammar.parse(expr);
        Function<String, Boolean> resolve = (symbol) -> {
            if (areResolved(expression.getRefs())) {
                instr.resolved = true;

                Map<String, Integer> values = addrs
                        .entrySet()
                        .stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue().offset))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
                values.put("pc", offsets.get(Section.Code));

                instr.parameter = expression.compute(values);
            }
            return instr.resolved;
        };
        if (!resolve.apply(null)) {
            addResolutionListener(resolve);
        }
    }

    protected boolean areResolved(Set<String> symbols) {
        for (String symbol : symbols) {
            Addr addr = addrs.get(symbol);
            if (addr == null) {
                return false;
            }
        }
        return true;
    }

    protected void addResolutionListener(Function<String, Boolean> listener) {
        resolutionListeners.add(listener);
    }

    protected void setResolved(String symbol) {
        Iterator<Function<String, Boolean>> it = resolutionListeners.iterator();
        while (it.hasNext()) {
            Function<String, Boolean> listener = it.next();
            if (listener.apply(symbol)) {
                it.remove();
            }
        }
    }

    protected void resolveLabel(String lbl) {
        if (addrs.containsKey(lbl)) {
            throw new IllegalStateException("redefining address for label '" + lbl + "'");
        }
        Addr addr = new Addr();
        addr.name = lbl;
        addr.offset = offsets.get(currentSection);
        addr.isCode = currentSection == Section.Code;
        addrs.put(lbl, addr);
        setResolved(lbl);
    }

    protected void changeCurrentSection(String name) {
        currentSection = Section.fromName(name);
        if (currentSection == null) {
            throw new IllegalStateException("Illegal section specifier : " + name);
        }
        switch (currentSection) {
            case Bss:
                currentReader = (line) -> {
                    readBss(line);
                    return null;
                };
                break;
            case Code:
                currentReader = (line) -> {
                    readCode(line);
                    return null;
                };
                break;
        }
    }

}