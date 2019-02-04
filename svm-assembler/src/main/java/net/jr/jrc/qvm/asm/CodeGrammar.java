package net.jr.jrc.qvm.asm;

import net.jr.common.Symbol;
import net.jr.jrc.qvm.QvmInstruction;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.Terminal;
import net.jr.lexer.Token;
import net.jr.parser.*;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.AstNodeFactory;

import java.util.List;

public class CodeGrammar extends Grammar {

    private static NonTerminal Label = new NonTerminal("Label");

    private static NonTerminal CodeSection = new NonTerminal("CodeSection");

    private static NonTerminal Instruction = new NonTerminal("Instruction");

    private static NonTerminal Expression = new NonTerminal("Expression");

    private static Terminal Identifier = Lexemes.cIdentifier();

    private static Terminal Number = Lexemes.cInteger();

    private static Terminal StartExpr = Lexemes.literal("${");

    private static Terminal EndExpr = Lexemes.singleChar('}');

    private static Terminal At = Lexemes.singleChar('@');

    CodeGrammar() {

        addRule(Label, Identifier, Lexemes.singleChar(':'));

        for (QvmInstruction.OpCode opcode : QvmInstruction.OpCode.values()) {
            Terminal t = Lexemes.literal(opcode.name());
            if (opcode.getParameterSize() == 0) {
                addRule(Instruction, t);
            } else {
                addRule(Instruction, t, Expression);
            }
        }

        addRule(Expression, Number);
        addRule(Expression, At, Identifier);
        addRule(Expression, StartExpr, EndExpr);

        Rule mainRule = addRule(CodeSection, zeroOrMore(oneOf(Label, Instruction))).get();

        setTargetRule(mainRule);
    }

    @Override
    public Lexer getLexer() {
        return super.getLexer().ignoringWhiteSpaces().ignoringNls();
    }

    public static void main(String[] args) {

        String txt = "CONST 21\nCONST 21\nADD\nBREAK";


        Parser parser = new CodeGrammar().createParser();
        parser.setParserListener(new ParserListener() {
            @Override
            public void onParseError(ParseError parseError, ParsingContext parsingContext) {

            }

            @Override
            public void onReduce(Rule rule, ParsingContext parsingContext) {
                List<AstNode> list = parsingContext.getAstNode().getDescendants();
                System.out.println( rule.getTarget() + "  " + list);
            }
        });


        parser.parse(txt);

    }


}
