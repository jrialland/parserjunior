package net.jr.codegen.cgx;

import net.jr.codegen.java.ParserGenerator;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Terminal;
import net.jr.lexer.basicterminals.QuotedString;
import net.jr.lexer.basicterminals.Word;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;;

import java.io.IOException;
import java.io.PrintWriter;;

public class CgxGrammar extends Grammar {

    public static NonTerminal ELEMENT = new NonTerminal("ELEMENT");

    public static NonTerminal BLOC = new NonTerminal("BLOC");

    public static NonTerminal TYPE_PRIMITIF = new NonTerminal("TYPE_PRIMITIF");

    public static NonTerminal CLE_VALEUR = new NonTerminal("CLE_VALEUR");

    public static NonTerminal BOOL = new NonTerminal("BOOL");

    public static Terminal LParen = Lexemes.singleChar('(').withName("lparen");

    public static Terminal RParen = Lexemes.singleChar(')').withName("rparen");

    public static Terminal DotComma = Lexemes.singleChar(';').withName("DotComma");

    public static Terminal Eq = Lexemes.singleChar('=').withName("Eq");

    public static Terminal String = new QuotedString('\'', '\'', '\\', new char[]{}).withName("String");

    public static Terminal Null = Lexemes.literal("null").withName("null");

    public static Terminal Number = new Word("0123456789").withName("number");

    public static Terminal BoolTrue = Lexemes.literal("true").withName("true");

    public static Terminal BoolFalse = Lexemes.literal("false").withName("false");

    public CgxGrammar() {
        addRule(ELEMENT, oneOf(BLOC, TYPE_PRIMITIF, CLE_VALEUR));
        addRule(BLOC, LParen, list(true, DotComma, ELEMENT), RParen);
        addRule(BOOL, oneOf(BoolTrue, BoolFalse));
        addRule(TYPE_PRIMITIF, oneOf(Number, BOOL, Null, String));
        addRule(CLE_VALEUR, String, Eq, oneOf(BLOC, TYPE_PRIMITIF));
    }

    public static void main(String... args) throws IOException {

        ParserGenerator pg = new ParserGenerator();
        pg.generate(new CgxGrammar(), new PrintWriter(System.out));
    }

}
