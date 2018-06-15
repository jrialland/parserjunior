package net.jr.cpreproc.procs;

import net.jr.pipes.PipeableProcessor;

import java.util.function.Consumer;

public class CommentsRemover extends PipeableProcessor<PreprocessorLine, PreprocessorLine> {

    private enum State {
        NormalFlow("normal flow"),
        String("string litteral"),
        StringEscapeSeq("string escape sequence"),
        MaybeComment("comment start (slash character)"),
        EolComment("comment at end of line"),
        MultilineComment("multiline comment"),
        MaybeMultilineCommentEnd("multiline comment end (asterisk character)");

        private String displayName;

        State(java.lang.String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }


    private State state = State.NormalFlow;

    private PreprocessorLine toMerge = null;

    @Override
    public void generate(PreprocessorLine preprocessorLine, Consumer<PreprocessorLine> out) {
        State previousState = state;
        PreprocessorLine modified = lex(preprocessorLine);

        //when entering multiline comment, state the line in order to merge it later
        if (previousState != State.MultilineComment && state == State.MultilineComment) {
            toMerge = modified;
        } else {

            //when exiting multiline comment, merge the previous line
            if (previousState == State.MultilineComment && state != State.MultilineComment) {
                if (toMerge != null) {
                    modified = toMerge.mergeWith(modified);
                    toMerge = null;
                }
            }

            if (!modified.isEmpty()) {
                out.accept(modified);
            }
        }
    }

    protected PreprocessorLine lex(PreprocessorLine line) {
        PreprocessorLine modifiedLine = new PreprocessorLine(line.getPosition(), "");
        char[] chars = line.getText().toCharArray();
        line.setText("");
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {
                case NormalFlow:
                    switch (c) {
                        case '/':
                            state = State.MaybeComment;
                            break;
                        case '"':
                            state = State.String;
                        default:
                            modifiedLine.extend(line.getPosition(i), c);
                            break;
                    }
                    break;
                case String:
                    modifiedLine.extend(line.getPosition(i), c);
                    switch (c) {
                        case '\\':
                            state = State.StringEscapeSeq;
                            break;
                        case '"':
                            state = State.NormalFlow;
                            break;
                        default:
                            break;
                    }
                    break;
                case StringEscapeSeq:
                    modifiedLine.extend(line.getPosition(i), c);
                    state = State.String;
                    break;
                case MaybeComment:
                    switch (c) {
                        case '/':
                            state = State.EolComment;
                            break;
                        case '*':
                            state = State.MultilineComment;
                            break;
                        default:
                            //add the character that we have missed
                            modifiedLine.extend(line.getPosition(i - 1), chars[i - 1]);
                            //add current character
                            modifiedLine.extend(line.getPosition(i), c);
                            state = State.NormalFlow;
                            break;
                    }
                    break;
                case EolComment:
                    break;
                case MultilineComment:
                    switch (c) {
                        case '*':
                            state = state.MaybeMultilineCommentEnd;
                            break;
                        default:
                            break;
                    }
                    break;
                case MaybeMultilineCommentEnd:
                    switch (c) {
                        case '*':
                            break;
                        case '/':
                            state = State.NormalFlow;
                            break;
                        default:
                            state = State.MultilineComment;
                            break;
                    }
                    break;
            }
        }

        //reset state at end of line
        if (state == State.EolComment) {
            state = State.NormalFlow;
        }
        return modifiedLine;
    }

}
