package net.jr.grammars.lua5;

import net.jr.lexer.CommonTokenTypes;
import net.jr.lexer.Lexeme;
import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.SingleChar;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;

public class Lua5Grammar extends Grammar {

    public static final Lexeme T_Name = CommonTokenTypes.cIdentifier();

    public static final Lexeme T_Comma = new SingleChar(',');

    public static final Lexeme T_SemiCol = new SingleChar(';');

    public static final Lexeme T_Eq = new SingleChar('=');

    public static final Lexeme T_LeftParen= new SingleChar('(');

    public static final Lexeme T_RightParen= new SingleChar(')');

    public static final Lexeme T_LeftCurlyBrace= new SingleChar('{');

    public static final Lexeme T_RightCurlyBrace= new SingleChar('}');

    public static final Lexeme T_ThreePoints = new Literal("...");

    public static Forward field = new Forward("field");
    public static Forward fieldList =  new Forward("fieldList");
    public static Forward exp =  new Forward("exp");
    public static Forward tableConstructor  =  new Forward("tableConstructor");
    public static Forward nameList =  new Forward("nameList");
    public static Forward parList =  new Forward("parList");
    public static Forward params  =  new Forward("params");
    public static Forward funcbody  =  new Forward("funcbody");
    public static Forward args =  new Forward("args");
    public static Forward var =  new Forward("var");

    private Lua5Grammar() {
//
//        %fallback  OPEN '(' .
//
//                chunk      ::= block .
//
//                semi       ::= ';' .
//                semi       ::= .
//
//        block      ::= scope statlist .
//                block      ::= scope statlist laststat semi .
//                ublock     ::= block 'until' exp .
//
//                scope      ::= .
//        scope      ::= scope statlist binding semi.
//
//                statlist   ::= .
//        statlist   ::= statlist stat semi .
//
//        stat       ::= 'do' block 'end' .
//                stat       ::= 'while' exp 'do' block 'end' .
//                stat       ::= repetition 'do' block 'end' .
//                stat       ::= 'repeat' ublock .
//                stat       ::= 'if' conds 'end' .
//                stat       ::= 'function' funcname funcbody .
//        stat       ::= setlist '=' explist1 .
//                stat       ::= functioncall .
//
//                repetition ::= 'for' NAME '=' explist23 .
//                repetition ::= 'for' namelist 'in' explist1 .
//
//                conds      ::= condlist .
//                conds      ::= condlist 'else' block .
//                condlist   ::= cond .
//                condlist   ::= condlist 'elseif' cond .
//                cond       ::= exp 'then' block .
//
//                laststat   ::= 'break' .
//                laststat   ::= 'return' .
//                laststat   ::= 'return' explist1 .
//
//                binding    ::= 'local' namelist .
//                binding    ::= 'local' namelist '=' explist1 .
//                binding    ::= 'local' 'function' NAME funcbody .
//
//        funcname   ::= dottedname .
//                funcname   ::= dottedname ':' NAME .
//
//                dottedname ::= NAME .
//                dottedname ::= dottedname '.' NAME .
//
//                namelist   ::= NAME .
//                namelist   ::= namelist ',' NAME .
//
//                explist1   ::= exp .
//                explist1   ::= explist1 ',' exp .
//                explist23  ::= exp ',' exp .
//                explist23  ::= exp ',' exp ',' exp .
//
//                %left      'or' .
//                %left      'and' .
//                %left      '<' '<=' '>' '>=' '==' '~=' .
//                %right     '..' .
//                %left      '+' '-' .
//                %left      '*' '/' '%' .
//                %right     'not' '#' .
//                %right     '^' .
//
//                exp        ::= 'nil'|'true'|'false'|NUMBER|STRING|'...' .
//                exp        ::= function .
//                exp        ::= prefixexp .
//                exp        ::= tableconstructor .
//                exp        ::= 'not'|'#'|'-' exp .         ['not']
//        exp        ::= exp 'or' exp .
//                exp        ::= exp 'and' exp .
//                exp        ::= exp '<'|'<='|'>'|'>='|'=='|'~=' exp .
//                exp        ::= exp '..' exp .
//                exp        ::= exp '+'|'-' exp .
//                exp        ::= exp '*'|'/'|'%' exp .
//                exp        ::= exp '^' exp .
//
//                setlist    ::= var .
//                setlist    ::= setlist ',' var .
//
//                var        ::= NAME .
//                var        ::= prefixexp '[' exp ']' .
//                var        ::= prefixexp '.' NAME .
//
//                prefixexp  ::= var .
//                prefixexp  ::= functioncall .
//                prefixexp  ::= OPEN exp ')' .
//
//                functioncall ::= prefixexp args .
//                functioncall ::= prefixexp ':' NAME args .
//
//        args        ::= '(' ')' .
//                args        ::= '(' explist1 ')' .
//                args        ::= tableconstructor .
//                args        ::= STRING .
//
//                function    ::= 'function' funcbody .
//
//                funcbody    ::= params block 'end' .
//
        //params ::= '(' parlist ')' .
        addRule(params, T_LeftParen, parList, T_RightParen);
        //parlist ::= .
        addRule(parList);

        //parlist     ::= namelist .
        addRule(parList, nameList);

        //parlist     ::= '...' .
        addRule(parList, T_ThreePoints);

        //parlist     ::= namelist ',' '...' .
        addRule(parList, nameList, T_Comma, T_ThreePoints);

        //tableconstructor ::= '{' '}' .
        addRule(tableConstructor, T_LeftCurlyBrace, T_LeftCurlyBrace);

        //tableconstructor ::= '{' fieldlist '}' .
        addRule(tableConstructor, T_LeftCurlyBrace, fieldList, T_LeftCurlyBrace);

        //tableconstructor ::= '{' fieldlist ','|';' '}' .
        addRule(tableConstructor, T_LeftCurlyBrace, fieldList, or(T_Comma, T_SemiCol), T_RightCurlyBrace);

        //fieldlist ::= field .
        addRule(fieldList, field);

        //fieldlist ::= fieldlist ','|';' field .
        addRule(fieldList, fieldList, or(T_Comma, T_SemiCol), field);

        //field ::= exp .
        addRule(field, exp);

        //field ::= NAME '=' exp .
        addRule(field, T_Name, T_Eq, exp);

        //field ::= '[' exp ']' '=' exp .
        addRule(field, new SingleChar('['), exp, new SingleChar(']'), T_Eq, exp);

    }
}
