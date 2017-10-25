package net.jr.parser.nekovm;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.SingleChar;
import net.jr.lexer.impl.Word;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;

import java.io.StringReader;

public class NekoGrammar extends Grammar {

    public static final Lexeme T_True = new Literal("true");

    public static final Lexeme T_False = new Literal("false");

    public static final Lexeme T_Null = new Literal("null");

    public static final Lexeme T_This = new Literal("this");

    public static final Lexeme T_Eq = new SingleChar('=');

    public static final Lexeme T_Ident = new Word("@" + Lexemes.Alpha, "@" + Lexemes.AlphaNum);

    public static final Lexeme T_Comma = new SingleChar(',');

    public static final Lexeme T_Dollar = new SingleChar('$');

    public static final Lexeme T_Semi = new SingleChar(';');

    public static final Lexeme T_Default = new Literal("default");

    public static final Lexeme T_Arrow = new Literal("=>");

    public static final Forward all = new Forward("all");

    public static final Forward program = new Forward("program");

    public static final Forward value = new Forward("value");

    public static final Forward expr = new Forward("expr");

    public static final Forward variables = new Forward("expr");

    public static final Forward parameters = new Forward("parameters");

    public static final Forward parameterNames = new Forward("parameterNames");

    public static final Forward switchCase = new Forward("switchCase");

    private Lexer lexer;

    public NekoGrammar() {

        addRule(all, program);

        //program :=
        //	| expr program
        //	| SEMICOLON program
        //	| _
        addRule(program, expr, program);
        addRule(program, T_Semi, program);
        addEmptyRule(program);

        //value :=
        //	| [0-9]+
        //  | 0x[0-9A-Fa-f]+
        //  | [0-9]+ DOT [0-9]*
        //  | DOT [0-9]+
        //  | DOUBLEQUOTE characters DOUBLEQUOTE
        //	| DOLLAR ident
        //	| true
        //  | false
        //  | null
        //  | this
        //  | ident
        addRule(value, new Word(Lexemes.Numbers));
        addRule(value, Lexemes.hexNumber());
        addRule(value, Lexemes.simpleFloat());
        addRule(value, Lexemes.cString());
        addRule(value, T_Dollar, T_Ident);
        addRule(value, T_True);
        addRule(value, T_False);
        addRule(value, T_Null);
        addRule(value, T_This);
        addRule(value, T_Ident);


        //variables :=
        //	| ident [= expr] variables
        //    | COMMA variables
        //    | _
        addRule(variables, T_Ident, optional(T_Eq, expr), variables);
        addRule(variables, T_Comma, variables);
        addEmptyRule(variables);

        //parameters :=
        //	| expr parameters
        //	| COMMA parameters
        //	| _
        addRule(parameters, expr, parameters);
        addRule(parameters, T_Comma, parameters);
        addEmptyRule(parameters);

        //parameters-names :=
        //	| ident parameters-names
        //    | COMMA parameters-names
        //    | _
        addRule(parameterNames, T_Ident, parameterNames);
        addRule(parameterNames, T_Comma, parameterNames);
        addEmptyRule(parameterNames);

        //switch-case :=
        //	| default => expr
        //    | expr => expr
        addRule(switchCase, T_Default, T_Arrow, expr);
        addRule(switchCase, expr, T_Arrow, expr);

        lexer = new Lexer(getTerminals());
        lexer.filterOut(Lexemes.whitespace());
        lexer.filterOut(Lexemes.lineComment("//"));
        lexer.filterOut(Lexemes.multilineComment("/*", "*/"));

    }

    public Lexer getLexer() {
        return lexer;
    }

    public void parse(String s) {
        createParser().parse(lexer.iterator(new StringReader(s)));
    }
}
