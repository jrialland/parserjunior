package net.jr.cpreproc.procs;

import net.jr.common.Position;
import net.jr.cpreproc.macrodefs.MacroDefinition;
import net.jr.cpreproc.macrodefs.MacroExpander;
import net.jr.lexer.Token;
import net.jr.pipes.PipeableProcessor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectivesInterpreter extends PipeableProcessor<PreprocessorLine, PreprocessorLine> {

    private static final Pattern PDirective = Pattern.compile("^\\p{Blank}*#\\p{Blank}*(.+)");

    Map<String, MacroDefinition> definitions;

    private Position currentPosition;

    public DirectivesInterpreter(Map<String, MacroDefinition> macroDefinitions) {
        this.definitions = macroDefinitions;
    }

    public Map<String, MacroDefinition> getDefinitions() {
        return definitions;
    }

    private void fatal(Position position, String message) {

    }

    private void error(Position position, String message) {

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

        }

        public ControlFlow handleElse() {
            if(elsePosition != null) {
                fatal(currentPosition, "#if/#else mismatch (#else already seen at " + elsePosition + ")");
            } else {
                elsePosition = currentPosition;
            }
            return this;
        }

        public boolean isIgnoring() {
            if(parent != null && parent.isIgnoring()) {
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
    }

    private ControlFlow controlFlow;

    @Override
    public void generate(PreprocessorLine preprocessorLine, Consumer<PreprocessorLine> out) {
        currentPosition = preprocessorLine.getPosition();
        String text = preprocessorLine.getText();

        Matcher matcher;
        if((matcher = PDirective.matcher(text)).matches()) {

            text = matcher.group(1);
            List<Token> tokens;
            DirectiveType directiveType = null;

            switch(directiveType) {

                case Define:
                    handleDefine(text);
                    break;
                case If:


                default:
                    error(preprocessorLine.getPosition(), String.format("Unrecognized preprocessor directive : '%s'", tokens.get(0).getText());
                    out.accept(preprocessorLine);
            }


        } else {
            if( ! controlFlow.isIgnoring()) {
                out.accept(new MacroExpander(getDefinitions()).expand(text));
            }
        }
    }

    private void handleDefine(String text) {
        if(!controlFlow.isIgnoring()) {

        }
    }


}
