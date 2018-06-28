package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.cpreproc.lexer.PreprocToken;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.reporting.Reporter;
import net.jr.lexer.Token;
import net.jr.pipes.PipeableProcessor;
import net.jr.types.ProxyUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectivesInterpreter extends PipeableProcessor<PreprocessorLine, PreprocessorLine> {

    private static final Pattern PDirective = Pattern.compile("^\\p{Blank}*#\\p{Blank}*(.+)");

    Map<String, MacroDefinition> definitions;

    private Reporter reporter;

    private Position currentPosition;

    public DirectivesInterpreter(Map<String, MacroDefinition> macroDefinitions, Reporter reporter) {
        this.definitions = macroDefinitions;
        this.reporter = reporter != null ? reporter : ProxyUtil.nullProxy(Reporter.class);
    }

    public Map<String, MacroDefinition> getDefinitions() {
        return definitions;
    }

    private class ControlFlow {

        private ControlFlow parent;

        private Position startPosition;

        private boolean value;

        private Position elsePosition = null;

        public ControlFlow(ControlFlow parent, Position startPosition, boolean value) {
            this.parent = parent;
            this.startPosition = startPosition;
            this.value = value;
        }

        public ControlFlow enterIf(boolean condition) {
            ControlFlow newState = new ControlFlow(this, currentPosition, condition);
            return newState;
        }

        public ControlFlow exitIf() {
            if (this == RootState) {
                reporter.error(currentPosition, "#endif without matching #if or #ifdef");
                return this;
            } else {
                return parent;
            }
        }

        public ControlFlow handleElse() {
            if (elsePosition != null) {
                reporter.fatal(currentPosition, "#if/#else mismatch (#else already seen at " + elsePosition + ")");
            } else {
                elsePosition = currentPosition;
            }
            return this;
        }

        public boolean isIgnoring() {
            if (parent != null && parent.isIgnoring()) {
                return true;
            }
            return value ? (elsePosition != null) : elsePosition == null;
        }

        public Position getStartPosition() {
            return startPosition;
        }
    }

    private enum DirectiveType {
        Define,
        Elif,
        Else,
        Endif,
        Error,
        If,
        Ifdef,
        Ifndef,
        Include,
        Info,
        Line,
        Pragma,
        Undef,
        Warning;

        private static TreeMap<String, DirectiveType> byNames = new TreeMap<>();

        static {
            for (DirectiveType d : values()) {
                byNames.put(d.name().toLowerCase(), d);
            }
        }

        public static DirectiveType forString(String s) {
            return byNames.get(s);
        }

    }

    private ControlFlow RootState = new ControlFlow(null, null, true);

    private ControlFlow controlFlow = RootState;

    @Override
    public void generate(PreprocessorLine preprocessorLine, Consumer<PreprocessorLine> out) {
        currentPosition = preprocessorLine.getPosition();
        String text = preprocessorLine.getText();

        Matcher matcher;
        if ((matcher = PDirective.matcher(text)).matches()) {

            text = matcher.group(1);
            List<PreprocToken> tokens = PreprocLexer.tokenize(text);
            DirectiveType directiveType = DirectiveType.forString(tokens.get(0).getText());

            switch (directiveType) {

                case Define:
                    handleDefine(tokens);
                    break;

                case If:
                    handleIf(tokens, text);
                    break;

                case Else:
                    handleElse();
                    break;

                case Elif:
                    handleElse();
                    handleIf(tokens, text);
                    break;

                case Ifdef:
                    handleIfDef(tokens, false);
                    break;

                case Ifndef:
                    handleIfDef(tokens, true);
                    break;

                case Endif:
                    controlFlow = controlFlow.exitIf();
                    break;

                case Include:
                    throw new UnsupportedOperationException("not implemented yet");//handleInclude(text, tokens, out);
                    //break;

                case Line:
                    out.accept(preprocessorLine);//#line directives are passed as-is
                    break;

                case Pragma:
                    break;

                case Undef:
                    handleUndef(tokens);
                    break;

                case Info:
                    reporter.info(currentPosition, text.substring(tokens.get(0).getEndIndex()));
                    break;

                case Warning:
                    reporter.warn(currentPosition, text.substring(tokens.get(0).getEndIndex()));
                    break;

                case Error:
                    reporter.error(currentPosition, text.substring(tokens.get(0).getEndIndex()));
                    break;

                default:
                    reporter.error(preprocessorLine.getPosition(), String.format("Unrecognized preprocessor directive : '%s'", tokens.get(0).getText()));
                    out.accept(preprocessorLine);
            }


        } else {
            if (!controlFlow.isIgnoring()) {
                out.accept(preprocessorLine);//TODO macro expansion
            }
        }
    }

    private void handleDefine(List<PreprocToken> tokens) {
        if (!controlFlow.isIgnoring()) {

        }
    }

    private void handleIf(List<PreprocToken> tokens, String text) {
        String expression = text.substring(tokens.get(0).getEndIndex() + 1);
        boolean cond = ExpressionEval.eval(expression, definitions);
        controlFlow = controlFlow.enterIf(cond);
    }

    private void handleElse() {
        controlFlow = controlFlow.handleElse();
    }

    private void handleIfDef(List<PreprocToken> tokens, boolean isNdef) {
        String definitionName = tokens.get(1).getText();
        boolean ifCondition = getDefinitions().containsKey(definitionName);
        if (isNdef) {
            ifCondition = !ifCondition;
        }
        controlFlow = controlFlow.enterIf(ifCondition);
    }

    protected void handleUndef(List<PreprocToken> tokens) {
        if (!controlFlow.isIgnoring()) {
            String definitionName = tokens.get(1).getText();
            getDefinitions().remove(definitionName);
        }
    }

}
