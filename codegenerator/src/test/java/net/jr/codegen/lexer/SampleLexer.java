package net.jr.codegen.lexer;
import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;

import java.util.function.Consumer;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Generated;

@Generated("parserjunior-codegenerator:1.0-SNAPSHOT")
class SampleLexer {

    public enum TokenType {
        _EOF(-1),
        INT(0),
        PLUS(1),
        MINUS(2),
        MUL(3),
        DIV(4),
        LPAREN(5),
        RPAREN(6),
        whitespace(7)
        ;

        private int id;

        TokenType(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static TokenType forId(int id) {
            for(TokenType t : values()) {
                if(t.id == id) {
                    return t;
                }
            }
            return null;
        }
    }

    public static class Token {

        private TokenType tokenType;

        private String matchedText;

        private int line;

        private int column;

        public Token(TokenType tokenType, String matchedText, int line, int column) {
            this.tokenType = tokenType;
            this.matchedText = matchedText;
            this.line = line;
            this.column = column;
        }

        TokenType getTokenType() {
            return tokenType;
        }

        String getMatchedText() {
            return matchedText;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

    }

    private static class LexerState {
        Set<Integer> currentStates = new TreeSet<Integer>();
        Set<Integer> nextStates = new TreeSet<Integer>();
        int line = 1;
        int column=1;
        TokenType candidate = null;
        String matchedText = "";

        public LexerState() {
            currentStates.add(0);
        }

        public Token makeToken() {
            Token t = new Token(candidate, matchedText, line, column);
            matchedText = "";
            return t;
        }

        public void prepareNextStep() {
            Set<Integer> tmp = currentStates;
            currentStates = nextStates;
            tmp.clear();
            nextStates = tmp;
        }
    }

    public void lex(Reader reader, Consumer<Token> consumer) throws IOException {
        final PushbackReader pbReader = reader instanceof PushbackReader ? (PushbackReader) reader : new PushbackReader(reader);
        final LexerState lexerState = new LexerState();
        while(step(pbReader, lexerState, consumer)) {
            lexerState.prepareNextStep();
        }
    }

    private boolean step(PushbackReader reader, LexerState lexerState, Consumer<Token> consumer) throws IOException {
        int line = lexerState.line;
        int column = lexerState.column;
        int priority = -1;
        int c = reader.read();

        if (c == -1) {
            if (lexerState.candidate == null) {
                if (lexerState.currentStates.size() != 1 || lexerState.currentStates.iterator().next() != 0) {
                    throw new RuntimeException("lex error");
                }
            } else {
                consumer.accept(lexerState.makeToken());
            }
            consumer.accept(new Token(TokenType._EOF, null, line, column));
            return false;
        } else if( c == '\n') {
            lexerState.line += 1;
            lexerState.column = 1;
        } else {
            lexerState.column += 1;
        }

        for(int state : lexerState.currentStates) {
            switch(state) {
                case 0 :
                    if(c=='+') {
                        lexerState.nextStates.add(-15);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.PLUS;
                        }
                    }
                    else if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-14);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.whitespace;
                        }
                    }
                    else if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-10);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    else if(c=='(') {
                        lexerState.nextStates.add(-9);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.LPAREN;
                        }
                    }
                    else if(c=='0') {
                        lexerState.nextStates.add(-5);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    else if(c==')') {
                        lexerState.nextStates.add(-4);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.RPAREN;
                        }
                    }
                    else if(c=='-') {
                        lexerState.nextStates.add(-3);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.MINUS;
                        }
                    }
                    else if(c=='*') {
                        lexerState.nextStates.add(-2);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.MUL;
                        }
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(-1);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.DIV;
                        }
                    }
                    break;
                case -5 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-8);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    else if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-6);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -6 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-7);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -8 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-7);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -10 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-13);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    else if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-11);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-10);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -11 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-12);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -13 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-12);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.INT;
                        }
                    }
                    break;
                case -14 :
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-14);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.whitespace;
                        }
                    }
                    break;
            }
        }

        if (lexerState.nextStates.isEmpty()) {
            if (lexerState.candidate == null) {
                throw new IllegalStateException();
            } else {
                consumer.accept(lexerState.makeToken());
                lexerState.candidate = null;
                lexerState.nextStates.clear();
                lexerState.nextStates.add(0);
                lexerState.line = line;
                lexerState.column = column;
                reader.unread(c);
            }
        } else {
            lexerState.matchedText += (char)c;
        }

        return true;

    }
}
