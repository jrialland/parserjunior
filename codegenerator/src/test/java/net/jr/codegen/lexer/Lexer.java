package net.jr.codegen.lexer;

import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;

import java.util.function.Consumer;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Generated;

@Generated("parserjunior-codegenerator:1.0-SNAPSHOT")
public class Lexer {

    public enum TokenType {
        tok_eof(false),
        tok_unsigned(false),
        tok_cString(false),
        tok_typedef(false),
        tok_divAssign(false),
        tok_threePoints(false),
        tok_signed(false),
        tok_int(false),
        tok_cFloatingPoint(false),
        tok_plusAssign(false),
        tok_return(false),
        tok_break(false),
        tok_cInteger(false),
        tok_leftSquareBracket(false),
        tok_continue(false),
        tok_rightSquareBracket(false),
        tok_sizeof(false),
        tok_bitwiseNot(false),
        tok_cOctal(false),
        tok_rightShiftEq(false),
        tok_short(false),
        tok_shiftLeft(false),
        tok_logicalOr(false),
        tok_lte(false),
        tok_void(false),
        tok_extern(false),
        tok_double(false),
        tok_long(false),
        tok_do(false),
        tok_float(false),
        tok_leftCurlyBrace(false),
        tok_bitwiseOr(false),
        tok_switch(false),
        tok_rightCurlyBrace(false),
        tok_tilde(false),
        tok_if(false),
        tok_bitwiseNotAssign(false),
        tok_minusMinus(false),
        tok_eqEq(false),
        tok_enum(false),
        tok_struct(false),
        tok_union(false),
        tok_char(false),
        tok_volatile(false),
        tok_minusAssign(false),
        tok_arrow(false),
        tok_static(false),
        tok_goto(false),
        tok_default(false),
        tok_moduloEq(false),
        tok_notEq(false),
        tok_leftShiftAssign(false),
        tok_exclamationMark(false),
        tok_typeName(false),
        tok_gte(false),
        tok_shiftRight(false),
        tok_logicalAnd(false),
        tok_bitwiseOrEq(false),
        tok_modulo(false),
        tok_cHexNumber(false),
        tok_bitwiseAnd(false),
        tok_leftParen(false),
        tok_rightParen(false),
        tok_const(false),
        tok_mult(false),
        tok_plus(false),
        tok_comma(false),
        tok_minus(false),
        tok_for(false),
        tok_dot(false),
        tok_slash(false),
        tok_register(false),
        tok_cCharacter(false),
        tok_case(false),
        tok_mulAssign(false),
        tok_auto(false),
        tok_twoPoints(false),
        tok_andEq(false),
        tok_dotComma(false),
        tok_lt(false),
        tok_eq(false),
        tok_gt(false),
        tok_questionMark(false),
        tok_while(false),
        tok_plusPlus(false),
        tok_CIdentifier(false),
        tok_cBinary(false),
        tok_else(false),
        tok_multilineComment(true),
        tok_lineComment(true),
        tok_Whitespace(true),
        tok_newLine(true)
        ;

        private boolean filtered;

        TokenType(boolean filtered) {
            this.filtered = filtered;
        }

        public boolean isFiltered() {
            return filtered;
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

        public String toString() {
            return tokenType.name()+"@"+line+":"+column;
        }
    }

    private static class LexerState {
        Set<Integer> currentStates = new TreeSet<Integer>();
        Set<Integer> nextStates = new TreeSet<Integer>();
        int startLine = 1, line = 1, startColumn = 1, column = 1;
        TokenType candidate = null;
        String matchedText = "";

        public LexerState() {
            currentStates.add(0);
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

        int c = reader.read();
        if (c == -1) {
            if(lexerState.candidate == null) {
                if(lexerState.currentStates.size() != 1 || lexerState.currentStates.iterator().next() != 0) {
                    throw new IllegalStateException(String.format("lexical error at line %d, column %d", lexerState.line, lexerState.column));
                }
            } else {
                if(!lexerState.candidate.isFiltered()) {
                    consumer.accept(new Token(lexerState.candidate, lexerState.matchedText, lexerState.startLine, lexerState.startColumn));
                }
            }
            consumer.accept(new Token(TokenType.tok_eof, "", lexerState.line, lexerState.column+1));
            return false;
        }
        int priority = -1;
        TokenType newCandidate = null;
        lexerState.matchedText += (char)c;

        for(int state : lexerState.currentStates) {
            switch(state) {
                case 0 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-298);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if(c=='=') {
                        lexerState.nextStates.add(296);
                    }
                    if(c=='v') {
                        lexerState.nextStates.add(288);
                    }
                    if(c=='a') {
                        lexerState.nextStates.add(284);
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(281);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(273);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(257);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(-256);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_gt;
                        }
                    }
                    if(c=='[') {
                        lexerState.nextStates.add(-255);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftSquareBracket;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(249);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(244);
                    }
                    if(c=='*') {
                        lexerState.nextStates.add(-243);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_mult;
                        }
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(241);
                    }
                    if(c=='(') {
                        lexerState.nextStates.add(-240);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftParen;
                        }
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(-239);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_slash;
                        }
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(237);
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(235);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(233);
                    }
                    if(c=='~') {
                        lexerState.nextStates.add(-232);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_tilde;
                        }
                    }
                    if(c=='!') {
                        lexerState.nextStates.add(-231);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_exclamationMark;
                        }
                    }
                    if(c=='g') {
                        lexerState.nextStates.add(227);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(224);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(220);
                    }
                    if(c=='=') {
                        lexerState.nextStates.add(-219);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_eq;
                        }
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(217);
                    }
                    if(c=='c') {
                        lexerState.nextStates.add(213);
                    }
                    if(c=='u') {
                        lexerState.nextStates.add(205);
                    }
                    if(c==']') {
                        lexerState.nextStates.add(-204);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightSquareBracket;
                        }
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(-203);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_dot;
                        }
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(-202);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseAnd;
                        }
                    }
                    if(c=='\"') {
                        lexerState.nextStates.add(198);
                    }
                    if(c=='*') {
                        lexerState.nextStates.add(196);
                    }
                    if(c=='%') {
                        lexerState.nextStates.add(-195);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_modulo;
                        }
                    }
                    if(c==')') {
                        lexerState.nextStates.add(-194);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightParen;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(188);
                    }
                    if(c=='f') {
                        lexerState.nextStates.add(183);
                    }
                    if(c=='u') {
                        lexerState.nextStates.add(178);
                    }
                    if(c==',') {
                        lexerState.nextStates.add(-177);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_comma;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(172);
                    }
                    if(c=='{') {
                        lexerState.nextStates.add(-171);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_leftCurlyBrace;
                        }
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(165);
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(159);
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(153);
                    }
                    if(c=='!') {
                        lexerState.nextStates.add(151);
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(145);
                    }
                    if(c=='|') {
                        lexerState.nextStates.add(-144);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseOr;
                        }
                    }
                    if(c=='r') {
                        lexerState.nextStates.add(138);
                    }
                    if(c=='\r') {
                        lexerState.nextStates.add(137);
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(135);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(133);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(131);
                    }
                    if(c=='w') {
                        lexerState.nextStates.add(126);
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(120);
                    }
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-119);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_Whitespace;
                        }
                    }
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-115);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if(c=='?') {
                        lexerState.nextStates.add(-114);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_questionMark;
                        }
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(111);
                    }
                    if(c=='f') {
                        lexerState.nextStates.add(108);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(106);
                    }
                    if(c=='|') {
                        lexerState.nextStates.add(104);
                    }
                    if(c=='/') {
                        lexerState.nextStates.add(100);
                    }
                    if(c=='i') {
                        lexerState.nextStates.add(98);
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(94);
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(92);
                    }
                    if(c=='e') {
                        lexerState.nextStates.add(88);
                    }
                    if(c=='|') {
                        lexerState.nextStates.add(86);
                    }
                    if(c=='d') {
                        lexerState.nextStates.add(79);
                    }
                    if(c=='l') {
                        lexerState.nextStates.add(75);
                    }
                    if(c=='^') {
                        lexerState.nextStates.add(73);
                    }
                    if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-72);
                        if(priority < 0) {
                            priority = 0;
                            newCandidate = TokenType.tok_CIdentifier;
                        }
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(71);
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(68);
                    }
                    if(c=='+') {
                        lexerState.nextStates.add(-67);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_plus;
                        }
                    }
                    if(c=='^') {
                        lexerState.nextStates.add(-66);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_bitwiseNot;
                        }
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(65);
                    }
                    if(c==';') {
                        lexerState.nextStates.add(-64);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_dotComma;
                        }
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(-63);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_lt;
                        }
                    }
                    if(c=='b') {
                        lexerState.nextStates.add(58);
                    }
                    if(c=='}') {
                        lexerState.nextStates.add(-57);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_rightCurlyBrace;
                        }
                    }
                    if(c=='i') {
                        lexerState.nextStates.add(54);
                    }
                    if(c=='&') {
                        lexerState.nextStates.add(52);
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(-48);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if(c=='s') {
                        lexerState.nextStates.add(42);
                    }
                    if(c=='\n') {
                        lexerState.nextStates.add(-41);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_newLine;
                        }
                    }
                    if(c==':') {
                        lexerState.nextStates.add(-40);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_twoPoints;
                        }
                    }
                    if(c=='r') {
                        lexerState.nextStates.add(32);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(29);
                    }
                    if(c=='%') {
                        lexerState.nextStates.add(27);
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(17);
                    }
                    if(c=='v') {
                        lexerState.nextStates.add(13);
                    }
                    if(c=='<') {
                        lexerState.nextStates.add(11);
                    }
                    if(c=='-') {
                        lexerState.nextStates.add(-10);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_minus;
                        }
                    }
                    if(c=='t') {
                        lexerState.nextStates.add(3);
                    }
                    if(c=='>') {
                        lexerState.nextStates.add(1);
                    }
                    break;
                case 1 :
                    if(c=='=') {
                        lexerState.nextStates.add(-2);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_gte;
                        }
                    }
                    break;
                case 3 :
                    if(c=='y') {
                        lexerState.nextStates.add(4);
                    }
                    break;
                case 4 :
                    if(c=='p') {
                        lexerState.nextStates.add(5);
                    }
                    break;
                case 5 :
                    if(c=='e') {
                        lexerState.nextStates.add(6);
                    }
                    break;
                case 6 :
                    if(c=='d') {
                        lexerState.nextStates.add(7);
                    }
                    break;
                case 7 :
                    if(c=='e') {
                        lexerState.nextStates.add(8);
                    }
                    break;
                case 8 :
                    if(c=='f') {
                        lexerState.nextStates.add(-9);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_typedef;
                        }
                    }
                    break;
                case 11 :
                    if(c=='<') {
                        lexerState.nextStates.add(-12);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_shiftLeft;
                        }
                    }
                    break;
                case 13 :
                    if(c=='o') {
                        lexerState.nextStates.add(14);
                    }
                    break;
                case 14 :
                    if(c=='i') {
                        lexerState.nextStates.add(15);
                    }
                    break;
                case 15 :
                    if(c=='d') {
                        lexerState.nextStates.add(-16);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_void;
                        }
                    }
                    break;
                case 17 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(17);
                    }
                    if(c=='.') {
                        lexerState.nextStates.add(-18);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -18 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-18);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(23);
                    }
                    if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-19);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -19 :
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(20);
                    }
                    break;
                case 20 :
                    if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(22);
                    }
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -21 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 22 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-21);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 23 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-25);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(24);
                    }
                    break;
                case 24 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-25);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -25 :
                    if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-26);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-25);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 27 :
                    if(c=='=') {
                        lexerState.nextStates.add(-28);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_moduloEq;
                        }
                    }
                    break;
                case 29 :
                    if(c=='>') {
                        lexerState.nextStates.add(30);
                    }
                    break;
                case 30 :
                    if(c=='=') {
                        lexerState.nextStates.add(-31);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_rightShiftEq;
                        }
                    }
                    break;
                case 32 :
                    if(c=='e') {
                        lexerState.nextStates.add(33);
                    }
                    break;
                case 33 :
                    if(c=='g') {
                        lexerState.nextStates.add(34);
                    }
                    break;
                case 34 :
                    if(c=='i') {
                        lexerState.nextStates.add(35);
                    }
                    break;
                case 35 :
                    if(c=='s') {
                        lexerState.nextStates.add(36);
                    }
                    break;
                case 36 :
                    if(c=='t') {
                        lexerState.nextStates.add(37);
                    }
                    break;
                case 37 :
                    if(c=='e') {
                        lexerState.nextStates.add(38);
                    }
                    break;
                case 38 :
                    if(c=='r') {
                        lexerState.nextStates.add(-39);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_register;
                        }
                    }
                    break;
                case 42 :
                    if(c=='t') {
                        lexerState.nextStates.add(43);
                    }
                    break;
                case 43 :
                    if(c=='a') {
                        lexerState.nextStates.add(44);
                    }
                    break;
                case 44 :
                    if(c=='t') {
                        lexerState.nextStates.add(45);
                    }
                    break;
                case 45 :
                    if(c=='i') {
                        lexerState.nextStates.add(46);
                    }
                    break;
                case 46 :
                    if(c=='c') {
                        lexerState.nextStates.add(-47);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_static;
                        }
                    }
                    break;
                case -48 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-51);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-49);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -49 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-50);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -51 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-50);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case 52 :
                    if(c=='&') {
                        lexerState.nextStates.add(-53);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_logicalAnd;
                        }
                    }
                    break;
                case 54 :
                    if(c=='n') {
                        lexerState.nextStates.add(55);
                    }
                    break;
                case 55 :
                    if(c=='t') {
                        lexerState.nextStates.add(-56);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_int;
                        }
                    }
                    break;
                case 58 :
                    if(c=='r') {
                        lexerState.nextStates.add(59);
                    }
                    break;
                case 59 :
                    if(c=='e') {
                        lexerState.nextStates.add(60);
                    }
                    break;
                case 60 :
                    if(c=='a') {
                        lexerState.nextStates.add(61);
                    }
                    break;
                case 61 :
                    if(c=='k') {
                        lexerState.nextStates.add(-62);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_break;
                        }
                    }
                    break;
                case 65 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-298);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if(c=='0') {
                        lexerState.nextStates.add(65);
                    }
                    break;
                case 68 :
                    if((c=='B')||(c=='b')) {
                        lexerState.nextStates.add(69);
                    }
                    break;
                case 69 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-70);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case -70 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-70);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case 71 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-18);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -72 :
                    if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-72);
                        if(priority < 0) {
                            priority = 0;
                            newCandidate = TokenType.tok_CIdentifier;
                        }
                    }
                    break;
                case 73 :
                    if(c=='=') {
                        lexerState.nextStates.add(-74);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_bitwiseNotAssign;
                        }
                    }
                    break;
                case 75 :
                    if(c=='o') {
                        lexerState.nextStates.add(76);
                    }
                    break;
                case 76 :
                    if(c=='n') {
                        lexerState.nextStates.add(77);
                    }
                    break;
                case 77 :
                    if(c=='g') {
                        lexerState.nextStates.add(-78);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_long;
                        }
                    }
                    break;
                case 79 :
                    if(c=='e') {
                        lexerState.nextStates.add(80);
                    }
                    break;
                case 80 :
                    if(c=='f') {
                        lexerState.nextStates.add(81);
                    }
                    break;
                case 81 :
                    if(c=='a') {
                        lexerState.nextStates.add(82);
                    }
                    break;
                case 82 :
                    if(c=='u') {
                        lexerState.nextStates.add(83);
                    }
                    break;
                case 83 :
                    if(c=='l') {
                        lexerState.nextStates.add(84);
                    }
                    break;
                case 84 :
                    if(c=='t') {
                        lexerState.nextStates.add(-85);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_default;
                        }
                    }
                    break;
                case 86 :
                    if(c=='=') {
                        lexerState.nextStates.add(-87);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_bitwiseOrEq;
                        }
                    }
                    break;
                case 88 :
                    if(c=='l') {
                        lexerState.nextStates.add(89);
                    }
                    break;
                case 89 :
                    if(c=='s') {
                        lexerState.nextStates.add(90);
                    }
                    break;
                case 90 :
                    if(c=='e') {
                        lexerState.nextStates.add(-91);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_else;
                        }
                    }
                    break;
                case 92 :
                    if(c=='=') {
                        lexerState.nextStates.add(-93);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_andEq;
                        }
                    }
                    break;
                case 94 :
                    if(c=='n') {
                        lexerState.nextStates.add(95);
                    }
                    break;
                case 95 :
                    if(c=='u') {
                        lexerState.nextStates.add(96);
                    }
                    break;
                case 96 :
                    if(c=='m') {
                        lexerState.nextStates.add(-97);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_enum;
                        }
                    }
                    break;
                case 98 :
                    if(c=='f') {
                        lexerState.nextStates.add(-99);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_if;
                        }
                    }
                    break;
                case 100 :
                    if(c=='*') {
                        lexerState.nextStates.add(101);
                    }
                    break;
                case 101 :
                    if(!(c=='*')) {
                        lexerState.nextStates.add(101);
                    }
                    if(c=='*') {
                        lexerState.nextStates.add(102);
                    }
                    break;
                case 102 :
                    if(c=='/') {
                        lexerState.nextStates.add(-103);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_multilineComment;
                        }
                    }
                    if(!(c=='/')) {
                        lexerState.nextStates.add(101);
                    }
                    break;
                case 104 :
                    if(c=='|') {
                        lexerState.nextStates.add(-105);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_logicalOr;
                        }
                    }
                    break;
                case 106 :
                    if(c=='=') {
                        lexerState.nextStates.add(-107);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_lte;
                        }
                    }
                    break;
                case 108 :
                    if(c=='o') {
                        lexerState.nextStates.add(109);
                    }
                    break;
                case 109 :
                    if(c=='r') {
                        lexerState.nextStates.add(-110);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_for;
                        }
                    }
                    break;
                case 111 :
                    if(c=='/') {
                        lexerState.nextStates.add(112);
                    }
                    break;
                case 112 :
                    if(c=='\n') {
                        lexerState.nextStates.add(-113);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_lineComment;
                        }
                    }
                    if(!(c=='\n')) {
                        lexerState.nextStates.add(112);
                    }
                    break;
                case -115 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-118);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-115);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-116);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -116 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-117);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -118 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-117);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -119 :
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-119);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_Whitespace;
                        }
                    }
                    break;
                case 120 :
                    if(c=='o') {
                        lexerState.nextStates.add(121);
                    }
                    break;
                case 121 :
                    if(c=='u') {
                        lexerState.nextStates.add(122);
                    }
                    break;
                case 122 :
                    if(c=='b') {
                        lexerState.nextStates.add(123);
                    }
                    break;
                case 123 :
                    if(c=='l') {
                        lexerState.nextStates.add(124);
                    }
                    break;
                case 124 :
                    if(c=='e') {
                        lexerState.nextStates.add(-125);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_double;
                        }
                    }
                    break;
                case 126 :
                    if(c=='h') {
                        lexerState.nextStates.add(127);
                    }
                    break;
                case 127 :
                    if(c=='i') {
                        lexerState.nextStates.add(128);
                    }
                    break;
                case 128 :
                    if(c=='l') {
                        lexerState.nextStates.add(129);
                    }
                    break;
                case 129 :
                    if(c=='e') {
                        lexerState.nextStates.add(-130);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_while;
                        }
                    }
                    break;
                case 131 :
                    if(c=='+') {
                        lexerState.nextStates.add(-132);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_plusPlus;
                        }
                    }
                    break;
                case 133 :
                    if(c=='>') {
                        lexerState.nextStates.add(-134);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_shiftRight;
                        }
                    }
                    break;
                case 135 :
                    if(c=='=') {
                        lexerState.nextStates.add(-136);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_minusAssign;
                        }
                    }
                    break;
                case 137 :
                    if(c=='\n') {
                        lexerState.nextStates.add(-41);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_newLine;
                        }
                    }
                    break;
                case 138 :
                    if(c=='e') {
                        lexerState.nextStates.add(139);
                    }
                    break;
                case 139 :
                    if(c=='t') {
                        lexerState.nextStates.add(140);
                    }
                    break;
                case 140 :
                    if(c=='u') {
                        lexerState.nextStates.add(141);
                    }
                    break;
                case 141 :
                    if(c=='r') {
                        lexerState.nextStates.add(142);
                    }
                    break;
                case 142 :
                    if(c=='n') {
                        lexerState.nextStates.add(-143);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_return;
                        }
                    }
                    break;
                case 145 :
                    if(c=='i') {
                        lexerState.nextStates.add(146);
                    }
                    break;
                case 146 :
                    if(c=='g') {
                        lexerState.nextStates.add(147);
                    }
                    break;
                case 147 :
                    if(c=='n') {
                        lexerState.nextStates.add(148);
                    }
                    break;
                case 148 :
                    if(c=='e') {
                        lexerState.nextStates.add(149);
                    }
                    break;
                case 149 :
                    if(c=='d') {
                        lexerState.nextStates.add(-150);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_signed;
                        }
                    }
                    break;
                case 151 :
                    if(c=='=') {
                        lexerState.nextStates.add(-152);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_notEq;
                        }
                    }
                    break;
                case 153 :
                    if(c=='x') {
                        lexerState.nextStates.add(154);
                    }
                    break;
                case 154 :
                    if(c=='t') {
                        lexerState.nextStates.add(155);
                    }
                    break;
                case 155 :
                    if(c=='e') {
                        lexerState.nextStates.add(156);
                    }
                    break;
                case 156 :
                    if(c=='r') {
                        lexerState.nextStates.add(157);
                    }
                    break;
                case 157 :
                    if(c=='n') {
                        lexerState.nextStates.add(-158);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_extern;
                        }
                    }
                    break;
                case 159 :
                    if(c=='w') {
                        lexerState.nextStates.add(160);
                    }
                    break;
                case 160 :
                    if(c=='i') {
                        lexerState.nextStates.add(161);
                    }
                    break;
                case 161 :
                    if(c=='t') {
                        lexerState.nextStates.add(162);
                    }
                    break;
                case 162 :
                    if(c=='c') {
                        lexerState.nextStates.add(163);
                    }
                    break;
                case 163 :
                    if(c=='h') {
                        lexerState.nextStates.add(-164);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_switch;
                        }
                    }
                    break;
                case 165 :
                    if(c=='x') {
                        lexerState.nextStates.add(166);
                    }
                    break;
                case 166 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-167);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -167 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-170);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-168);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-167);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -168 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-169);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -170 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-169);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case 172 :
                    if(c=='h') {
                        lexerState.nextStates.add(173);
                    }
                    break;
                case 173 :
                    if(c=='o') {
                        lexerState.nextStates.add(174);
                    }
                    break;
                case 174 :
                    if(c=='r') {
                        lexerState.nextStates.add(175);
                    }
                    break;
                case 175 :
                    if(c=='t') {
                        lexerState.nextStates.add(-176);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_short;
                        }
                    }
                    break;
                case 178 :
                    if(c=='n') {
                        lexerState.nextStates.add(179);
                    }
                    break;
                case 179 :
                    if(c=='i') {
                        lexerState.nextStates.add(180);
                    }
                    break;
                case 180 :
                    if(c=='o') {
                        lexerState.nextStates.add(181);
                    }
                    break;
                case 181 :
                    if(c=='n') {
                        lexerState.nextStates.add(-182);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_union;
                        }
                    }
                    break;
                case 183 :
                    if(c=='l') {
                        lexerState.nextStates.add(184);
                    }
                    break;
                case 184 :
                    if(c=='o') {
                        lexerState.nextStates.add(185);
                    }
                    break;
                case 185 :
                    if(c=='a') {
                        lexerState.nextStates.add(186);
                    }
                    break;
                case 186 :
                    if(c=='t') {
                        lexerState.nextStates.add(-187);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_float;
                        }
                    }
                    break;
                case 188 :
                    if(c=='i') {
                        lexerState.nextStates.add(189);
                    }
                    break;
                case 189 :
                    if(c=='z') {
                        lexerState.nextStates.add(190);
                    }
                    break;
                case 190 :
                    if(c=='e') {
                        lexerState.nextStates.add(191);
                    }
                    break;
                case 191 :
                    if(c=='o') {
                        lexerState.nextStates.add(192);
                    }
                    break;
                case 192 :
                    if(c=='f') {
                        lexerState.nextStates.add(-193);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_sizeof;
                        }
                    }
                    break;
                case 196 :
                    if(c=='=') {
                        lexerState.nextStates.add(-197);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_mulAssign;
                        }
                    }
                    break;
                case 198 :
                    if("\r\n".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(201);
                    }
                    if(!(c=='\"')) {
                        lexerState.nextStates.add(198);
                    }
                    if(c=='\\') {
                        lexerState.nextStates.add(200);
                    }
                    if(c=='\"') {
                        lexerState.nextStates.add(-199);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cString;
                        }
                    }
                    break;
                case 200 :
                    if(true) {
                        lexerState.nextStates.add(198);
                    }
                    break;
                case 205 :
                    if(c=='n') {
                        lexerState.nextStates.add(206);
                    }
                    break;
                case 206 :
                    if(c=='s') {
                        lexerState.nextStates.add(207);
                    }
                    break;
                case 207 :
                    if(c=='i') {
                        lexerState.nextStates.add(208);
                    }
                    break;
                case 208 :
                    if(c=='g') {
                        lexerState.nextStates.add(209);
                    }
                    break;
                case 209 :
                    if(c=='n') {
                        lexerState.nextStates.add(210);
                    }
                    break;
                case 210 :
                    if(c=='e') {
                        lexerState.nextStates.add(211);
                    }
                    break;
                case 211 :
                    if(c=='d') {
                        lexerState.nextStates.add(-212);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_unsigned;
                        }
                    }
                    break;
                case 213 :
                    if(c=='h') {
                        lexerState.nextStates.add(214);
                    }
                    break;
                case 214 :
                    if(c=='a') {
                        lexerState.nextStates.add(215);
                    }
                    break;
                case 215 :
                    if(c=='r') {
                        lexerState.nextStates.add(-216);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_char;
                        }
                    }
                    break;
                case 217 :
                    if(c=='-') {
                        lexerState.nextStates.add(-218);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_minusMinus;
                        }
                    }
                    break;
                case 220 :
                    if(c=='a') {
                        lexerState.nextStates.add(221);
                    }
                    break;
                case 221 :
                    if(c=='s') {
                        lexerState.nextStates.add(222);
                    }
                    break;
                case 222 :
                    if(c=='e') {
                        lexerState.nextStates.add(-223);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_case;
                        }
                    }
                    break;
                case 224 :
                    if(c=='<') {
                        lexerState.nextStates.add(225);
                    }
                    break;
                case 225 :
                    if(c=='=') {
                        lexerState.nextStates.add(-226);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_leftShiftAssign;
                        }
                    }
                    break;
                case 227 :
                    if(c=='o') {
                        lexerState.nextStates.add(228);
                    }
                    break;
                case 228 :
                    if(c=='t') {
                        lexerState.nextStates.add(229);
                    }
                    break;
                case 229 :
                    if(c=='o') {
                        lexerState.nextStates.add(-230);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_goto;
                        }
                    }
                    break;
                case 233 :
                    if(c=='=') {
                        lexerState.nextStates.add(-234);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_plusAssign;
                        }
                    }
                    break;
                case 235 :
                    if(c=='>') {
                        lexerState.nextStates.add(-236);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_arrow;
                        }
                    }
                    break;
                case 237 :
                    if(c=='o') {
                        lexerState.nextStates.add(-238);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_do;
                        }
                    }
                    break;
                case 241 :
                    if(c=='=') {
                        lexerState.nextStates.add(-242);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_divAssign;
                        }
                    }
                    break;
                case 244 :
                    if(c=='o') {
                        lexerState.nextStates.add(245);
                    }
                    break;
                case 245 :
                    if(c=='n') {
                        lexerState.nextStates.add(246);
                    }
                    break;
                case 246 :
                    if(c=='s') {
                        lexerState.nextStates.add(247);
                    }
                    break;
                case 247 :
                    if(c=='t') {
                        lexerState.nextStates.add(-248);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_const;
                        }
                    }
                    break;
                case 249 :
                    if(c=='t') {
                        lexerState.nextStates.add(250);
                    }
                    break;
                case 250 :
                    if(c=='r') {
                        lexerState.nextStates.add(251);
                    }
                    break;
                case 251 :
                    if(c=='u') {
                        lexerState.nextStates.add(252);
                    }
                    break;
                case 252 :
                    if(c=='c') {
                        lexerState.nextStates.add(253);
                    }
                    break;
                case 253 :
                    if(c=='t') {
                        lexerState.nextStates.add(-254);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_struct;
                        }
                    }
                    break;
                case 257 :
                    if(c=='\\') {
                        lexerState.nextStates.add(260);
                    }
                    if((c>=32 && c<=128)&&(!(c=='\\'))) {
                        lexerState.nextStates.add(258);
                    }
                    break;
                case 258 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 260 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(271);
                    }
                    if(c=='x') {
                        lexerState.nextStates.add(270);
                    }
                    if((c=='u')||(c=='U')) {
                        lexerState.nextStates.add(261);
                    }
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(270);
                    }
                    if("\"?abfnrtv\\".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(258);
                    }
                    break;
                case 261 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(262);
                    }
                    break;
                case 262 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(263);
                    }
                    break;
                case 263 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(264);
                    }
                    break;
                case 264 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(265);
                    }
                    break;
                case 265 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(266);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 266 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(267);
                    }
                    break;
                case 267 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(268);
                    }
                    break;
                case 268 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(269);
                    }
                    break;
                case 269 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 270 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 271 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(272);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 272 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(258);
                    }
                    if(c=='\'') {
                        lexerState.nextStates.add(-259);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 273 :
                    if(c=='o') {
                        lexerState.nextStates.add(274);
                    }
                    break;
                case 274 :
                    if(c=='n') {
                        lexerState.nextStates.add(275);
                    }
                    break;
                case 275 :
                    if(c=='t') {
                        lexerState.nextStates.add(276);
                    }
                    break;
                case 276 :
                    if(c=='i') {
                        lexerState.nextStates.add(277);
                    }
                    break;
                case 277 :
                    if(c=='n') {
                        lexerState.nextStates.add(278);
                    }
                    break;
                case 278 :
                    if(c=='u') {
                        lexerState.nextStates.add(279);
                    }
                    break;
                case 279 :
                    if(c=='e') {
                        lexerState.nextStates.add(-280);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_continue;
                        }
                    }
                    break;
                case 281 :
                    if(c=='.') {
                        lexerState.nextStates.add(282);
                    }
                    break;
                case 282 :
                    if(c=='.') {
                        lexerState.nextStates.add(-283);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_threePoints;
                        }
                    }
                    break;
                case 284 :
                    if(c=='u') {
                        lexerState.nextStates.add(285);
                    }
                    break;
                case 285 :
                    if(c=='t') {
                        lexerState.nextStates.add(286);
                    }
                    break;
                case 286 :
                    if(c=='o') {
                        lexerState.nextStates.add(-287);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_auto;
                        }
                    }
                    break;
                case 288 :
                    if(c=='o') {
                        lexerState.nextStates.add(289);
                    }
                    break;
                case 289 :
                    if(c=='l') {
                        lexerState.nextStates.add(290);
                    }
                    break;
                case 290 :
                    if(c=='a') {
                        lexerState.nextStates.add(291);
                    }
                    break;
                case 291 :
                    if(c=='t') {
                        lexerState.nextStates.add(292);
                    }
                    break;
                case 292 :
                    if(c=='i') {
                        lexerState.nextStates.add(293);
                    }
                    break;
                case 293 :
                    if(c=='l') {
                        lexerState.nextStates.add(294);
                    }
                    break;
                case 294 :
                    if(c=='e') {
                        lexerState.nextStates.add(-295);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_volatile;
                        }
                    }
                    break;
                case 296 :
                    if(c=='=') {
                        lexerState.nextStates.add(-297);
                        if(priority < 2) {
                            priority = 2;
                            newCandidate = TokenType.tok_eqEq;
                        }
                    }
                    break;
                case -298 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-301);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-298);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-299);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -299 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-300);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -301 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-300);
                        if(priority < 1) {
                            priority = 1;
                            newCandidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
            }
        }

        if(lexerState.nextStates.isEmpty()) {
            if(lexerState.candidate == null) {
                throw new IllegalStateException(String.format("lexical error at line %d, column %d", lexerState.line, lexerState.column));
            } else {

                if(!lexerState.candidate.isFiltered()) {
                    consumer.accept(new Token(lexerState.candidate, lexerState.matchedText, lexerState.startLine, lexerState.startColumn));
                }

                lexerState.candidate = null;
                lexerState.matchedText = "";
                lexerState.nextStates.add(0);
                reader.unread(c);
                lexerState.startLine = lexerState.line;
                lexerState.startColumn = lexerState.column;
                return true;
            }
        }

        lexerState.candidate = newCandidate;

        if(c=='\n') {
            lexerState.line += 1;
            lexerState.column = 1;
        } else {
            lexerState.column +=1;
        }

        return true;

    }
}
