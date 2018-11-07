package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.lexer.PreprocLexer;
import net.jr.cpreproc.lexer.PreprocToken;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.NoArgsMacroDefinition;
import net.jr.cpreproc.reporting.Reporter;
import net.jr.lexer.Token;
import net.jr.pipes.PipeableProcessor;
import net.jr.types.ProxyUtil;
import net.jr.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interprets preprocessor '#' directives
 */
public class DirectivesInterpreter extends PipeableProcessor<PreprocessorLine, PreprocessorLine> {

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

    private ControlFlow RootState = new ControlFlow(null, null, true);

    private ControlFlow controlFlow = RootState;

    /**
     * Add a newline at end of file
     *
     * @param out
     */
    @Override
    public void afterLast(Consumer<PreprocessorLine> out) {
        out.accept(new PreprocessorLine(currentPosition == null ? Position.start() : currentPosition.nextLine()));
    }

    @Override
    public void generate(PreprocessorLine preprocessorLine, Consumer<PreprocessorLine> out) {
        currentPosition = preprocessorLine.getPosition();
        String text = preprocessorLine.getText();
        Pair<DirectiveType, String> detectedDirective = DirectiveType.detectDirective(text);

        if (detectedDirective != null) {
            List<PreprocToken> tokens = PreprocLexer.tokenize(detectedDirective.getValue());
            switch (detectedDirective.getKey()) {

                case Define:
                    handleDefine(tokens);
                    //generate an empty line
                    out.accept(new PreprocessorLine(currentPosition));
                    break;

                case If:
                    handleIf(tokens);
                    break;

                case Else:
                    handleElse();
                    break;

                case Elif:
                    handleElse();
                    handleIf(tokens);
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
                out.accept(MacroExpander.expand(definitions, preprocessorLine));
            }
        }
    }

    private void handleDefine(List<PreprocToken> tokens) {
        if (!controlFlow.isIgnoring()) {

            //TODO handle defines with args

            String key = tokens.get(0).getText();
            String value = "";

            if (tokens.size() > 2) {
                value = tokens.subList(2, tokens.size()).stream().map(t -> t.getText()).reduce("", (s1, s2) -> s1 + s2);
            }
            definitions.put(key, new NoArgsMacroDefinition(key, value));
        }
    }

    private void handleIf(List<PreprocToken> tokens) {
        String expression = StringUtil.ltrim(toText(tokens));
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
            String definitionName = tokens.get(0).getText();
            getDefinitions().remove(definitionName);
        }
    }

    private static final String toText(Collection<PreprocToken> tokens) {
        return tokens.stream().map(Token::getText).reduce("", (s1, s2) -> s1 + s2);
    }
}
