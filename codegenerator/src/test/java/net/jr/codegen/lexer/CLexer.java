package net.jr.codegen.lexer;
import java.io.Reader;
import java.io.PushbackReader;
import java.io.IOException;

import java.util.function.Consumer;

import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Generated;

@Generated("parserjunior-codegenerator:1.0-SNAPSHOT")
public class CLexer {

    public enum TokenType {
        tok_eof,
        tok_unsigned,
        tok_cString,
        tok_typedef,
        tok_divAssign,
        tok_threePoints,
        tok_signed,
        tok_int,
        tok_cFloatingPoint,
        tok_plusAssign,
        tok_return,
        tok_break,
        tok_cInteger,
        tok_leftSquareBracket,
        tok_continue,
        tok_rightSquareBracket,
        tok_sizeof,
        tok_bitwiseNot,
        tok_cOctal,
        tok_rightShiftEq,
        tok_short,
        tok_shiftLeft,
        tok_logicalOr,
        tok_lte,
        tok_void,
        tok_extern,
        tok_double,
        tok_long,
        tok_do,
        tok_float,
        tok_cIdentifier,
        tok_leftCurlyBrace,
        tok_bitwiseOr,
        tok_switch,
        tok_rightCurlyBrace,
        tok_tilde,
        tok_if,
        tok_bitwiseNotAssign,
        tok_minusMinus,
        tok_eqEq,
        tok_enum,
        tok_struct,
        tok_union,
        tok_char,
        tok_volatile,
        tok_minusAssign,
        tok_arrow,
        tok_static,
        tok_goto,
        tok_default,
        tok_moduloEq,
        tok_notEq,
        tok_leftShiftAssign,
        tok_exclamationMark,
        tok_typeName,
        tok_gte,
        tok_shiftRight,
        tok_logicalAnd,
        tok_bitwiseOrEq,
        tok_modulo,
        tok_cHexNumber,
        tok_bitwiseAnd,
        tok_leftParen,
        tok_rightParen,
        tok_const,
        tok_mult,
        tok_plus,
        tok_comma,
        tok_minus,
        tok_for,
        tok_dot,
        tok_slash,
        tok_register,
        tok_cCharacter,
        tok_case,
        tok_mulAssign,
        tok_auto,
        tok_twoPoints,
        tok_andEq,
        tok_dotComma,
        tok_lt,
        tok_eq,
        tok_gt,
        tok_questionMark,
        tok_while,
        tok_plusPlus,
        tok_cBinary,
        tok_else,
        tok_multilineComment,
        tok_lineComment,
        tok_Whitespace,
        tok_newLine
        ;
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
            consumer.accept(new Token(TokenType.tok_eof, null, line, column));
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
                    if(c=='*') {
                        lexerState.nextStates.add(-301);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_mult;
                        }
                    }
                    else if(c=='.') {
                        lexerState.nextStates.add(298);
                    }
                    else if(c=='v') {
                        lexerState.nextStates.add(294);
                    }
                    else if(c=='\n') {
                        lexerState.nextStates.add(-293);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_newLine;
                        }
                    }
                    else if(c==';') {
                        lexerState.nextStates.add(-292);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_dotComma;
                        }
                    }
                    else if(c==':') {
                        lexerState.nextStates.add(-291);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_twoPoints;
                        }
                    }
                    else if(c=='a') {
                        lexerState.nextStates.add(287);
                    }
                    else if(c=='w') {
                        lexerState.nextStates.add(282);
                    }
                    else if(c=='v') {
                        lexerState.nextStates.add(274);
                    }
                    else if(c=='>') {
                        lexerState.nextStates.add(272);
                    }
                    else if(c=='>') {
                        lexerState.nextStates.add(269);
                    }
                    else if(c==')') {
                        lexerState.nextStates.add(-268);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_rightParen;
                        }
                    }
                    else if(c=='0') {
                        lexerState.nextStates.add(262);
                    }
                    else if(c=='r') {
                        lexerState.nextStates.add(254);
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(250);
                    }
                    else if(c=='+') {
                        lexerState.nextStates.add(248);
                    }
                    else if(c=='i') {
                        lexerState.nextStates.add(245);
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(239);
                    }
                    else if(c=='[') {
                        lexerState.nextStates.add(-238);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_leftSquareBracket;
                        }
                    }
                    else if(c=='=') {
                        lexerState.nextStates.add(236);
                    }
                    else if(c=='e') {
                        lexerState.nextStates.add(232);
                    }
                    else if(c=='<') {
                        lexerState.nextStates.add(230);
                    }
                    else if(c=='\"') {
                        lexerState.nextStates.add(226);
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(225);
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(222);
                    }
                    else if(c=='+') {
                        lexerState.nextStates.add(220);
                    }
                    else if(c=='-') {
                        lexerState.nextStates.add(-219);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_minus;
                        }
                    }
                    else if(c=='*') {
                        lexerState.nextStates.add(217);
                    }
                    else if(c=='-') {
                        lexerState.nextStates.add(215);
                    }
                    else if(c=='|') {
                        lexerState.nextStates.add(213);
                    }
                    else if(c=='u') {
                        lexerState.nextStates.add(205);
                    }
                    else if(c=='d') {
                        lexerState.nextStates.add(199);
                    }
                    else if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-198);
                        if(priority <= -10) {
                            priority = -10;
                            lexerState.candidate = TokenType.tok_cIdentifier;
                        }
                    }
                    else if(c=='g') {
                        lexerState.nextStates.add(194);
                    }
                    else if(c==',') {
                        lexerState.nextStates.add(-193);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_comma;
                        }
                    }
                    else if(c=='e') {
                        lexerState.nextStates.add(189);
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(183);
                    }
                    else if(c=='%') {
                        lexerState.nextStates.add(181);
                    }
                    else if(c=='c') {
                        lexerState.nextStates.add(176);
                    }
                    else if(c=='=') {
                        lexerState.nextStates.add(-175);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_eq;
                        }
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(169);
                    }
                    else if(c=='d') {
                        lexerState.nextStates.add(167);
                    }
                    else if(c=='-') {
                        lexerState.nextStates.add(165);
                    }
                    else if(c==']') {
                        lexerState.nextStates.add(-164);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_rightSquareBracket;
                        }
                    }
                    else if(c=='&') {
                        lexerState.nextStates.add(162);
                    }
                    else if(c=='&') {
                        lexerState.nextStates.add(-161);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_bitwiseAnd;
                        }
                    }
                    else if(c=='|') {
                        lexerState.nextStates.add(159);
                    }
                    else if(c=='+') {
                        lexerState.nextStates.add(-158);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_plus;
                        }
                    }
                    else if(c=='c') {
                        lexerState.nextStates.add(154);
                    }
                    else if(c=='.') {
                        lexerState.nextStates.add(-153);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_dot;
                        }
                    }
                    else if(c=='!') {
                        lexerState.nextStates.add(151);
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(-150);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_slash;
                        }
                    }
                    else if(c=='-') {
                        lexerState.nextStates.add(148);
                    }
                    else if(c=='d') {
                        lexerState.nextStates.add(141);
                    }
                    else if(c=='{') {
                        lexerState.nextStates.add(-140);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_leftCurlyBrace;
                        }
                    }
                    else if(c=='r') {
                        lexerState.nextStates.add(134);
                    }
                    else if(c=='u') {
                        lexerState.nextStates.add(129);
                    }
                    else if(c=='>') {
                        lexerState.nextStates.add(127);
                    }
                    else if(c=='!') {
                        lexerState.nextStates.add(-126);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_exclamationMark;
                        }
                    }
                    else if(c=='f') {
                        lexerState.nextStates.add(121);
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(115);
                    }
                    else if(c=='?') {
                        lexerState.nextStates.add(-114);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_questionMark;
                        }
                    }
                    else if(c=='^') {
                        lexerState.nextStates.add(-113);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_bitwiseNot;
                        }
                    }
                    else if(c=='|') {
                        lexerState.nextStates.add(-112);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_bitwiseOr;
                        }
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(110);
                    }
                    else if(c=='%') {
                        lexerState.nextStates.add(-109);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_modulo;
                        }
                    }
                    else if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-105);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    else if(c=='&') {
                        lexerState.nextStates.add(103);
                    }
                    else if(c=='0') {
                        lexerState.nextStates.add(-99);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(93);
                    }
                    else if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-92);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_Whitespace;
                        }
                    }
                    else if(c=='0') {
                        lexerState.nextStates.add(91);
                    }
                    else if(c=='c') {
                        lexerState.nextStates.add(83);
                    }
                    else if(c=='s') {
                        lexerState.nextStates.add(78);
                    }
                    else if(c=='\'') {
                        lexerState.nextStates.add(62);
                    }
                    else if(c=='b') {
                        lexerState.nextStates.add(57);
                    }
                    else if(c=='i') {
                        lexerState.nextStates.add(55);
                    }
                    else if(c=='<') {
                        lexerState.nextStates.add(-54);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_lt;
                        }
                    }
                    else if(c=='t') {
                        lexerState.nextStates.add(47);
                    }
                    else if(c=='0') {
                        lexerState.nextStates.add(44);
                    }
                    else if(c=='\r') {
                        lexerState.nextStates.add(43);
                    }
                    else if(c=='e') {
                        lexerState.nextStates.add(37);
                    }
                    else if(c=='<') {
                        lexerState.nextStates.add(35);
                    }
                    else if(c=='<') {
                        lexerState.nextStates.add(32);
                    }
                    else if(c=='(') {
                        lexerState.nextStates.add(-31);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_leftParen;
                        }
                    }
                    else if(c=='}') {
                        lexerState.nextStates.add(-30);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_rightCurlyBrace;
                        }
                    }
                    else if(c=='^') {
                        lexerState.nextStates.add(28);
                    }
                    else if(c=='c') {
                        lexerState.nextStates.add(24);
                    }
                    else if(c=='f') {
                        lexerState.nextStates.add(21);
                    }
                    else if(c=='.') {
                        lexerState.nextStates.add(11);
                    }
                    else if(c=='l') {
                        lexerState.nextStates.add(7);
                    }
                    else if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-3);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    else if(c=='>') {
                        lexerState.nextStates.add(-2);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_gt;
                        }
                    }
                    else if(c=='~') {
                        lexerState.nextStates.add(-1);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_tilde;
                        }
                    }
                    break;
                case -3 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-6);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    else if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-3);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    else if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-4);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -4 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-5);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -6 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-5);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case 7 :
                    if(c=='o') {
                        lexerState.nextStates.add(8);
                    }
                    break;
                case 8 :
                    if(c=='n') {
                        lexerState.nextStates.add(9);
                    }
                    break;
                case 9 :
                    if(c=='g') {
                        lexerState.nextStates.add(-10);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_long;
                        }
                    }
                    break;
                case 11 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-12);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -12 :
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(17);
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-12);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    else if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-13);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -13 :
                    if((c=='E')||(c=='e')) {
                        lexerState.nextStates.add(14);
                    }
                    break;
                case 14 :
                    if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(16);
                    }
                    else if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-15);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -15 :
                    if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-15);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 16 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-15);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 17 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-19);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    else if((c=='+')||(c=='-')) {
                        lexerState.nextStates.add(18);
                    }
                    break;
                case 18 :
                    if("123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-19);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case -19 :
                    if("lfLF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-20);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-19);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    break;
                case 21 :
                    if(c=='o') {
                        lexerState.nextStates.add(22);
                    }
                    break;
                case 22 :
                    if(c=='r') {
                        lexerState.nextStates.add(-23);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_for;
                        }
                    }
                    break;
                case 24 :
                    if(c=='a') {
                        lexerState.nextStates.add(25);
                    }
                    break;
                case 25 :
                    if(c=='s') {
                        lexerState.nextStates.add(26);
                    }
                    break;
                case 26 :
                    if(c=='e') {
                        lexerState.nextStates.add(-27);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_case;
                        }
                    }
                    break;
                case 28 :
                    if(c=='=') {
                        lexerState.nextStates.add(-29);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_bitwiseNotAssign;
                        }
                    }
                    break;
                case 32 :
                    if(c=='<') {
                        lexerState.nextStates.add(33);
                    }
                    break;
                case 33 :
                    if(c=='=') {
                        lexerState.nextStates.add(-34);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_leftShiftAssign;
                        }
                    }
                    break;
                case 35 :
                    if(c=='<') {
                        lexerState.nextStates.add(-36);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_shiftLeft;
                        }
                    }
                    break;
                case 37 :
                    if(c=='x') {
                        lexerState.nextStates.add(38);
                    }
                    break;
                case 38 :
                    if(c=='t') {
                        lexerState.nextStates.add(39);
                    }
                    break;
                case 39 :
                    if(c=='e') {
                        lexerState.nextStates.add(40);
                    }
                    break;
                case 40 :
                    if(c=='r') {
                        lexerState.nextStates.add(41);
                    }
                    break;
                case 41 :
                    if(c=='n') {
                        lexerState.nextStates.add(-42);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_extern;
                        }
                    }
                    break;
                case 43 :
                    if(c=='\n') {
                        lexerState.nextStates.add(-293);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_newLine;
                        }
                    }
                    break;
                case 44 :
                    if((c=='B')||(c=='b')) {
                        lexerState.nextStates.add(45);
                    }
                    break;
                case 45 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-46);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case -46 :
                    if((c=='0')||(c=='1')) {
                        lexerState.nextStates.add(-46);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cBinary;
                        }
                    }
                    break;
                case 47 :
                    if(c=='y') {
                        lexerState.nextStates.add(48);
                    }
                    break;
                case 48 :
                    if(c=='p') {
                        lexerState.nextStates.add(49);
                    }
                    break;
                case 49 :
                    if(c=='e') {
                        lexerState.nextStates.add(50);
                    }
                    break;
                case 50 :
                    if(c=='d') {
                        lexerState.nextStates.add(51);
                    }
                    break;
                case 51 :
                    if(c=='e') {
                        lexerState.nextStates.add(52);
                    }
                    break;
                case 52 :
                    if(c=='f') {
                        lexerState.nextStates.add(-53);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_typedef;
                        }
                    }
                    break;
                case 55 :
                    if(c=='f') {
                        lexerState.nextStates.add(-56);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_if;
                        }
                    }
                    break;
                case 57 :
                    if(c=='r') {
                        lexerState.nextStates.add(58);
                    }
                    break;
                case 58 :
                    if(c=='e') {
                        lexerState.nextStates.add(59);
                    }
                    break;
                case 59 :
                    if(c=='a') {
                        lexerState.nextStates.add(60);
                    }
                    break;
                case 60 :
                    if(c=='k') {
                        lexerState.nextStates.add(-61);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_break;
                        }
                    }
                    break;
                case 62 :
                    if(c=='\\') {
                        lexerState.nextStates.add(65);
                    }
                    else if((c>=32 && c<=128)&&(!(c=='\\'))) {
                        lexerState.nextStates.add(63);
                    }
                    break;
                case 63 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 65 :
                    if("\"?abfnrtv\\".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(63);
                    }
                    else if(c=='x') {
                        lexerState.nextStates.add(77);
                    }
                    else if((c=='u')||(c=='U')) {
                        lexerState.nextStates.add(68);
                    }
                    else if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(66);
                    }
                    else if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(77);
                    }
                    break;
                case 66 :
                    if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(67);
                    }
                    else if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 67 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    else if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(63);
                    }
                    break;
                case 68 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(69);
                    }
                    break;
                case 69 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(70);
                    }
                    break;
                case 70 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(71);
                    }
                    break;
                case 71 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(72);
                    }
                    break;
                case 72 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    else if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(73);
                    }
                    break;
                case 73 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(74);
                    }
                    break;
                case 74 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(75);
                    }
                    break;
                case 75 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(76);
                    }
                    break;
                case 76 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 77 :
                    if(c=='\'') {
                        lexerState.nextStates.add(-64);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cCharacter;
                        }
                    }
                    break;
                case 78 :
                    if(c=='h') {
                        lexerState.nextStates.add(79);
                    }
                    break;
                case 79 :
                    if(c=='o') {
                        lexerState.nextStates.add(80);
                    }
                    break;
                case 80 :
                    if(c=='r') {
                        lexerState.nextStates.add(81);
                    }
                    break;
                case 81 :
                    if(c=='t') {
                        lexerState.nextStates.add(-82);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_short;
                        }
                    }
                    break;
                case 83 :
                    if(c=='o') {
                        lexerState.nextStates.add(84);
                    }
                    break;
                case 84 :
                    if(c=='n') {
                        lexerState.nextStates.add(85);
                    }
                    break;
                case 85 :
                    if(c=='t') {
                        lexerState.nextStates.add(86);
                    }
                    break;
                case 86 :
                    if(c=='i') {
                        lexerState.nextStates.add(87);
                    }
                    break;
                case 87 :
                    if(c=='n') {
                        lexerState.nextStates.add(88);
                    }
                    break;
                case 88 :
                    if(c=='u') {
                        lexerState.nextStates.add(89);
                    }
                    break;
                case 89 :
                    if(c=='e') {
                        lexerState.nextStates.add(-90);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_continue;
                        }
                    }
                    break;
                case 91 :
                    if(c=='0') {
                        lexerState.nextStates.add(91);
                    }
                    else if("01234567".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-3);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cOctal;
                        }
                    }
                    break;
                case -92 :
                    if(" \u00A0\u2007\u202F\u000B\u001C\u001D\u001E\u001F\t\f\r".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-92);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_Whitespace;
                        }
                    }
                    break;
                case 93 :
                    if(c=='t') {
                        lexerState.nextStates.add(94);
                    }
                    break;
                case 94 :
                    if(c=='a') {
                        lexerState.nextStates.add(95);
                    }
                    break;
                case 95 :
                    if(c=='t') {
                        lexerState.nextStates.add(96);
                    }
                    break;
                case 96 :
                    if(c=='i') {
                        lexerState.nextStates.add(97);
                    }
                    break;
                case 97 :
                    if(c=='c') {
                        lexerState.nextStates.add(-98);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_static;
                        }
                    }
                    break;
                case -99 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-102);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    else if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-100);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -100 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-101);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -102 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-101);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case 103 :
                    if(c=='=') {
                        lexerState.nextStates.add(-104);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_andEq;
                        }
                    }
                    break;
                case -105 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-108);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-105);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    else if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-106);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -106 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-107);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case -108 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-107);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cInteger;
                        }
                    }
                    break;
                case 110 :
                    if(c=='=') {
                        lexerState.nextStates.add(-111);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_divAssign;
                        }
                    }
                    break;
                case 115 :
                    if(c=='t') {
                        lexerState.nextStates.add(116);
                    }
                    break;
                case 116 :
                    if(c=='r') {
                        lexerState.nextStates.add(117);
                    }
                    break;
                case 117 :
                    if(c=='u') {
                        lexerState.nextStates.add(118);
                    }
                    break;
                case 118 :
                    if(c=='c') {
                        lexerState.nextStates.add(119);
                    }
                    break;
                case 119 :
                    if(c=='t') {
                        lexerState.nextStates.add(-120);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_struct;
                        }
                    }
                    break;
                case 121 :
                    if(c=='l') {
                        lexerState.nextStates.add(122);
                    }
                    break;
                case 122 :
                    if(c=='o') {
                        lexerState.nextStates.add(123);
                    }
                    break;
                case 123 :
                    if(c=='a') {
                        lexerState.nextStates.add(124);
                    }
                    break;
                case 124 :
                    if(c=='t') {
                        lexerState.nextStates.add(-125);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_float;
                        }
                    }
                    break;
                case 127 :
                    if(c=='=') {
                        lexerState.nextStates.add(-128);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_gte;
                        }
                    }
                    break;
                case 129 :
                    if(c=='n') {
                        lexerState.nextStates.add(130);
                    }
                    break;
                case 130 :
                    if(c=='i') {
                        lexerState.nextStates.add(131);
                    }
                    break;
                case 131 :
                    if(c=='o') {
                        lexerState.nextStates.add(132);
                    }
                    break;
                case 132 :
                    if(c=='n') {
                        lexerState.nextStates.add(-133);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_union;
                        }
                    }
                    break;
                case 134 :
                    if(c=='e') {
                        lexerState.nextStates.add(135);
                    }
                    break;
                case 135 :
                    if(c=='t') {
                        lexerState.nextStates.add(136);
                    }
                    break;
                case 136 :
                    if(c=='u') {
                        lexerState.nextStates.add(137);
                    }
                    break;
                case 137 :
                    if(c=='r') {
                        lexerState.nextStates.add(138);
                    }
                    break;
                case 138 :
                    if(c=='n') {
                        lexerState.nextStates.add(-139);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_return;
                        }
                    }
                    break;
                case 141 :
                    if(c=='e') {
                        lexerState.nextStates.add(142);
                    }
                    break;
                case 142 :
                    if(c=='f') {
                        lexerState.nextStates.add(143);
                    }
                    break;
                case 143 :
                    if(c=='a') {
                        lexerState.nextStates.add(144);
                    }
                    break;
                case 144 :
                    if(c=='u') {
                        lexerState.nextStates.add(145);
                    }
                    break;
                case 145 :
                    if(c=='l') {
                        lexerState.nextStates.add(146);
                    }
                    break;
                case 146 :
                    if(c=='t') {
                        lexerState.nextStates.add(-147);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_default;
                        }
                    }
                    break;
                case 148 :
                    if(c=='>') {
                        lexerState.nextStates.add(-149);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_arrow;
                        }
                    }
                    break;
                case 151 :
                    if(c=='=') {
                        lexerState.nextStates.add(-152);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_notEq;
                        }
                    }
                    break;
                case 154 :
                    if(c=='h') {
                        lexerState.nextStates.add(155);
                    }
                    break;
                case 155 :
                    if(c=='a') {
                        lexerState.nextStates.add(156);
                    }
                    break;
                case 156 :
                    if(c=='r') {
                        lexerState.nextStates.add(-157);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_char;
                        }
                    }
                    break;
                case 159 :
                    if(c=='|') {
                        lexerState.nextStates.add(-160);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_logicalOr;
                        }
                    }
                    break;
                case 162 :
                    if(c=='&') {
                        lexerState.nextStates.add(-163);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_logicalAnd;
                        }
                    }
                    break;
                case 165 :
                    if(c=='-') {
                        lexerState.nextStates.add(-166);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_minusMinus;
                        }
                    }
                    break;
                case 167 :
                    if(c=='o') {
                        lexerState.nextStates.add(-168);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_do;
                        }
                    }
                    break;
                case 169 :
                    if(c=='i') {
                        lexerState.nextStates.add(170);
                    }
                    break;
                case 170 :
                    if(c=='z') {
                        lexerState.nextStates.add(171);
                    }
                    break;
                case 171 :
                    if(c=='e') {
                        lexerState.nextStates.add(172);
                    }
                    break;
                case 172 :
                    if(c=='o') {
                        lexerState.nextStates.add(173);
                    }
                    break;
                case 173 :
                    if(c=='f') {
                        lexerState.nextStates.add(-174);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_sizeof;
                        }
                    }
                    break;
                case 176 :
                    if(c=='o') {
                        lexerState.nextStates.add(177);
                    }
                    break;
                case 177 :
                    if(c=='n') {
                        lexerState.nextStates.add(178);
                    }
                    break;
                case 178 :
                    if(c=='s') {
                        lexerState.nextStates.add(179);
                    }
                    break;
                case 179 :
                    if(c=='t') {
                        lexerState.nextStates.add(-180);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_const;
                        }
                    }
                    break;
                case 181 :
                    if(c=='=') {
                        lexerState.nextStates.add(-182);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_moduloEq;
                        }
                    }
                    break;
                case 183 :
                    if(c=='w') {
                        lexerState.nextStates.add(184);
                    }
                    break;
                case 184 :
                    if(c=='i') {
                        lexerState.nextStates.add(185);
                    }
                    break;
                case 185 :
                    if(c=='t') {
                        lexerState.nextStates.add(186);
                    }
                    break;
                case 186 :
                    if(c=='c') {
                        lexerState.nextStates.add(187);
                    }
                    break;
                case 187 :
                    if(c=='h') {
                        lexerState.nextStates.add(-188);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_switch;
                        }
                    }
                    break;
                case 189 :
                    if(c=='n') {
                        lexerState.nextStates.add(190);
                    }
                    break;
                case 190 :
                    if(c=='u') {
                        lexerState.nextStates.add(191);
                    }
                    break;
                case 191 :
                    if(c=='m') {
                        lexerState.nextStates.add(-192);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_enum;
                        }
                    }
                    break;
                case 194 :
                    if(c=='o') {
                        lexerState.nextStates.add(195);
                    }
                    break;
                case 195 :
                    if(c=='t') {
                        lexerState.nextStates.add(196);
                    }
                    break;
                case 196 :
                    if(c=='o') {
                        lexerState.nextStates.add(-197);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_goto;
                        }
                    }
                    break;
                case -198 :
                    if("_abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-198);
                        if(priority <= -10) {
                            priority = -10;
                            lexerState.candidate = TokenType.tok_cIdentifier;
                        }
                    }
                    break;
                case 199 :
                    if(c=='o') {
                        lexerState.nextStates.add(200);
                    }
                    break;
                case 200 :
                    if(c=='u') {
                        lexerState.nextStates.add(201);
                    }
                    break;
                case 201 :
                    if(c=='b') {
                        lexerState.nextStates.add(202);
                    }
                    break;
                case 202 :
                    if(c=='l') {
                        lexerState.nextStates.add(203);
                    }
                    break;
                case 203 :
                    if(c=='e') {
                        lexerState.nextStates.add(-204);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_double;
                        }
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
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_unsigned;
                        }
                    }
                    break;
                case 213 :
                    if(c=='=') {
                        lexerState.nextStates.add(-214);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_bitwiseOrEq;
                        }
                    }
                    break;
                case 215 :
                    if(c=='=') {
                        lexerState.nextStates.add(-216);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_minusAssign;
                        }
                    }
                    break;
                case 217 :
                    if(c=='=') {
                        lexerState.nextStates.add(-218);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_mulAssign;
                        }
                    }
                    break;
                case 220 :
                    if(c=='+') {
                        lexerState.nextStates.add(-221);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_plusPlus;
                        }
                    }
                    break;
                case 222 :
                    if(c=='/') {
                        lexerState.nextStates.add(223);
                    }
                    break;
                case 223 :
                    if(!(c=='\n')) {
                        lexerState.nextStates.add(223);
                    }
                    else if(c=='\n') {
                        lexerState.nextStates.add(-224);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_lineComment;
                        }
                    }
                    break;
                case 225 :
                    if(c=='.') {
                        lexerState.nextStates.add(-12);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cFloatingPoint;
                        }
                    }
                    else if("0123456789".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(225);
                    }
                    break;
                case 226 :
                    if(!(c=='\"')) {
                        lexerState.nextStates.add(226);
                    }
                    else if(c=='\"') {
                        lexerState.nextStates.add(-229);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cString;
                        }
                    }
                    else if("\r\n".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(228);
                    }
                    else if(c=='\\') {
                        lexerState.nextStates.add(227);
                    }
                    break;
                case 227 :
                    if(true) {
                        lexerState.nextStates.add(226);
                    }
                    break;
                case 230 :
                    if(c=='=') {
                        lexerState.nextStates.add(-231);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_lte;
                        }
                    }
                    break;
                case 232 :
                    if(c=='l') {
                        lexerState.nextStates.add(233);
                    }
                    break;
                case 233 :
                    if(c=='s') {
                        lexerState.nextStates.add(234);
                    }
                    break;
                case 234 :
                    if(c=='e') {
                        lexerState.nextStates.add(-235);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_else;
                        }
                    }
                    break;
                case 236 :
                    if(c=='=') {
                        lexerState.nextStates.add(-237);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_eqEq;
                        }
                    }
                    break;
                case 239 :
                    if(c=='i') {
                        lexerState.nextStates.add(240);
                    }
                    break;
                case 240 :
                    if(c=='g') {
                        lexerState.nextStates.add(241);
                    }
                    break;
                case 241 :
                    if(c=='n') {
                        lexerState.nextStates.add(242);
                    }
                    break;
                case 242 :
                    if(c=='e') {
                        lexerState.nextStates.add(243);
                    }
                    break;
                case 243 :
                    if(c=='d') {
                        lexerState.nextStates.add(-244);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_signed;
                        }
                    }
                    break;
                case 245 :
                    if(c=='n') {
                        lexerState.nextStates.add(246);
                    }
                    break;
                case 246 :
                    if(c=='t') {
                        lexerState.nextStates.add(-247);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_int;
                        }
                    }
                    break;
                case 248 :
                    if(c=='=') {
                        lexerState.nextStates.add(-249);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_plusAssign;
                        }
                    }
                    break;
                case 250 :
                    if(c=='*') {
                        lexerState.nextStates.add(251);
                    }
                    break;
                case 251 :
                    if(c=='*') {
                        lexerState.nextStates.add(252);
                    }
                    else if(!(c=='*')) {
                        lexerState.nextStates.add(251);
                    }
                    break;
                case 252 :
                    if(!(c=='/')) {
                        lexerState.nextStates.add(251);
                    }
                    else if(c=='/') {
                        lexerState.nextStates.add(-253);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_multilineComment;
                        }
                    }
                    break;
                case 254 :
                    if(c=='e') {
                        lexerState.nextStates.add(255);
                    }
                    break;
                case 255 :
                    if(c=='g') {
                        lexerState.nextStates.add(256);
                    }
                    break;
                case 256 :
                    if(c=='i') {
                        lexerState.nextStates.add(257);
                    }
                    break;
                case 257 :
                    if(c=='s') {
                        lexerState.nextStates.add(258);
                    }
                    break;
                case 258 :
                    if(c=='t') {
                        lexerState.nextStates.add(259);
                    }
                    break;
                case 259 :
                    if(c=='e') {
                        lexerState.nextStates.add(260);
                    }
                    break;
                case 260 :
                    if(c=='r') {
                        lexerState.nextStates.add(-261);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_register;
                        }
                    }
                    break;
                case 262 :
                    if(c=='x') {
                        lexerState.nextStates.add(263);
                    }
                    break;
                case 263 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-264);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -264 :
                    if("0123456789abcdefABCDEF".indexOf((char)c)>-1) {
                        lexerState.nextStates.add(-264);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    else if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-267);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    else if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-265);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -265 :
                    if((c=='L')||(c=='l')) {
                        lexerState.nextStates.add(-266);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case -267 :
                    if((c=='U')||(c=='u')) {
                        lexerState.nextStates.add(-266);
                        if(priority <= 1) {
                            priority = 1;
                            lexerState.candidate = TokenType.tok_cHexNumber;
                        }
                    }
                    break;
                case 269 :
                    if(c=='>') {
                        lexerState.nextStates.add(270);
                    }
                    break;
                case 270 :
                    if(c=='=') {
                        lexerState.nextStates.add(-271);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_rightShiftEq;
                        }
                    }
                    break;
                case 272 :
                    if(c=='>') {
                        lexerState.nextStates.add(-273);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_shiftRight;
                        }
                    }
                    break;
                case 274 :
                    if(c=='o') {
                        lexerState.nextStates.add(275);
                    }
                    break;
                case 275 :
                    if(c=='l') {
                        lexerState.nextStates.add(276);
                    }
                    break;
                case 276 :
                    if(c=='a') {
                        lexerState.nextStates.add(277);
                    }
                    break;
                case 277 :
                    if(c=='t') {
                        lexerState.nextStates.add(278);
                    }
                    break;
                case 278 :
                    if(c=='i') {
                        lexerState.nextStates.add(279);
                    }
                    break;
                case 279 :
                    if(c=='l') {
                        lexerState.nextStates.add(280);
                    }
                    break;
                case 280 :
                    if(c=='e') {
                        lexerState.nextStates.add(-281);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_volatile;
                        }
                    }
                    break;
                case 282 :
                    if(c=='h') {
                        lexerState.nextStates.add(283);
                    }
                    break;
                case 283 :
                    if(c=='i') {
                        lexerState.nextStates.add(284);
                    }
                    break;
                case 284 :
                    if(c=='l') {
                        lexerState.nextStates.add(285);
                    }
                    break;
                case 285 :
                    if(c=='e') {
                        lexerState.nextStates.add(-286);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_while;
                        }
                    }
                    break;
                case 287 :
                    if(c=='u') {
                        lexerState.nextStates.add(288);
                    }
                    break;
                case 288 :
                    if(c=='t') {
                        lexerState.nextStates.add(289);
                    }
                    break;
                case 289 :
                    if(c=='o') {
                        lexerState.nextStates.add(-290);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_auto;
                        }
                    }
                    break;
                case 294 :
                    if(c=='o') {
                        lexerState.nextStates.add(295);
                    }
                    break;
                case 295 :
                    if(c=='i') {
                        lexerState.nextStates.add(296);
                    }
                    break;
                case 296 :
                    if(c=='d') {
                        lexerState.nextStates.add(-297);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_void;
                        }
                    }
                    break;
                case 298 :
                    if(c=='.') {
                        lexerState.nextStates.add(299);
                    }
                    break;
                case 299 :
                    if(c=='.') {
                        lexerState.nextStates.add(-300);
                        if(priority <= 2) {
                            priority = 2;
                            lexerState.candidate = TokenType.tok_threePoints;
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