package net.jr.grammar.c11;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;

public class C11Grammar extends Grammar {

    public interface Tokens {
        Lexeme Identifier = Lexemes.cIdentifier();
        Lexeme StringLiteral = Lexemes.cString();
        Lexeme LeftBrace = Lexemes.singleChar('(');
        Lexeme RightBrace = Lexemes.singleChar(')');
        Lexeme _Generic = Lexemes.literal("_Generic");
        Lexeme Comma = Lexemes.singleChar(',');
        Lexeme AndAnd = Lexemes.literal("&&");
        Lexeme PlusPlus = Lexemes.literal("++");
        Lexeme MinusMinus = Lexemes.literal("--");
        Lexeme Sizeof = Lexemes.literal("sizeof");
        Lexeme _Alignof = Lexemes.literal("_Alignof");
        Lexeme And = Lexemes.singleChar('&');
        Lexeme Mult = Lexemes.singleChar('*');
        Lexeme Plus = Lexemes.singleChar('+');
        Lexeme Minus = Lexemes.singleChar('-');
        Lexeme Div = Lexemes.singleChar('/');
        Lexeme Modulo = Lexemes.singleChar('%');
        Lexeme Tilde = Lexemes.singleChar('~');
        Lexeme Exclamation = Lexemes.singleChar('!');
        Lexeme Question = Lexemes.singleChar('?');
        Lexeme __extension__ = Lexemes.literal("__extension__");
        Lexeme ShiftRight = Lexemes.literal(">>");
        Lexeme ShiftLeft = Lexemes.literal("<<");
        Lexeme Gt = Lexemes.literal(">");
        Lexeme Lt = Lexemes.literal("<");
        Lexeme Gte = Lexemes.literal(">=");
        Lexeme Lte = Lexemes.literal("<=");
        Lexeme EqEq = Lexemes.literal("==");
        Lexeme NEq = Lexemes.literal("!=");
        Lexeme Pow = Lexemes.singleChar('^');
        Lexeme Pipe = Lexemes.singleChar('|');
        Lexeme PipePipe = Lexemes.literal("||");
        Lexeme DualPoint = Lexemes.singleChar(':');

        Lexeme Eq = Lexemes.singleChar('=');
        Lexeme MulEq = Lexemes.literal("*=");
        Lexeme DivEq = Lexemes.literal("/=");
        Lexeme ModEq = Lexemes.literal("%=");
        Lexeme PlusEq = Lexemes.literal("+=");
        Lexeme MinusEq= Lexemes.literal("-=");
        Lexeme ShiftLeftEq = Lexemes.literal("<<=");
        Lexeme ShiftRightEq = Lexemes.literal(">>=");
        Lexeme AndEq = Lexemes.literal("&=");
        Lexeme PowEq = Lexemes.literal("^=");
        Lexeme PipeEq = Lexemes.literal("|=");
        Lexeme DotComma = Lexemes.singleChar(';');
    }

    public static Forward PrimaryExpression;
    public static Forward Expression;
    public static Forward GenericSelection;
    public static Forward AssignmentExpression;
    public static Forward GenericAssocList;
    public static Forward UnaryExpression;
    public static Forward PostfixExpression;
    public static Forward Typename;
    public static Forward UnaryOperator;
    public static Forward CastExpression;
    public static Forward MultiplicativeExpression;
    public static Forward AdditiveExpression;

    public static Forward ShiftExpression;
    public static Forward RelationalExpression;
    public static Forward EqualityExpression;
    public static Forward AndExpression;
    public static Forward ExclusiveOrExpression;
    public static Forward InclusiveOrExpression;
    public static Forward LogicalAndExpression;
    public static Forward LogicalOrExpression;
    public static Forward ConditionalExpression;
    public static Forward ConstantExpression;

    public static Forward Declaration;
    public static Forward DeclarationSpecifier;
    public static Forward InitDeclarator;
    public static Forward StaticAssertDeclaration;
    public static Forward DeclarationSpecifier2;
    C11Grammar() {

        addRule(PrimaryExpression, Tokens.Identifier);
        //addRule(PrimaryExpression, Constant);
        addRule(PrimaryExpression, oneOrMore(Tokens.StringLiteral));
        addRule(PrimaryExpression, Tokens.LeftBrace, Expression, Tokens.RightBrace);
        addRule(PrimaryExpression, GenericSelection);
        //|   '__extension__'? '(' compoundStatement ')' // Blocks (GCC extension)
        //|   '__builtin_va_arg' '(' unaryExpression ',' typeName ')'
        //| '__builtin_offsetof' '(' typeName ',' unaryExpression ')'


        addRule(GenericSelection, Tokens._Generic, Tokens.LeftBrace, AssignmentExpression, Tokens.Comma, GenericAssocList, Tokens.RightBrace);

        target(UnaryExpression)
                .def(PostfixExpression)
                .def(Tokens.PlusPlus, UnaryExpression)
                .def(Tokens.MinusMinus, UnaryExpression)
                .def(UnaryOperator, CastExpression)
                .def(Tokens.Sizeof, UnaryExpression)
                .def(Tokens.Sizeof, Tokens.LeftBrace, Typename, Tokens.RightBrace)
                .def(Tokens._Alignof, Tokens.LeftBrace, Typename, Tokens.RightBrace)
                .def(Tokens.AndAnd, Tokens.Identifier);

        target(UnaryOperator)
                .def(or(Tokens.And, Tokens.Mult, Tokens.Plus, Tokens.Minus, Tokens.Tilde, Tokens.Exclamation));

        target(CastExpression)
                .def(UnaryExpression)
                .def(Tokens.LeftBrace, Typename, Tokens.RightBrace, CastExpression)
                .def(Tokens.__extension__, Tokens.LeftBrace, Typename, Tokens.RightBrace, CastExpression);

        target(MultiplicativeExpression)
                .def(CastExpression)
                .def(MultiplicativeExpression, Tokens.Mult, CastExpression)
                .def(MultiplicativeExpression, Tokens.Div, CastExpression)
                .def(MultiplicativeExpression, Tokens.Modulo, CastExpression);

        target(AdditiveExpression)
                .def(MultiplicativeExpression)
                .def(AdditiveExpression, Tokens.Plus, MultiplicativeExpression)
                .def(AdditiveExpression, Tokens.Minus, MultiplicativeExpression);

        target(ShiftExpression)
                .def(AdditiveExpression)
                .def(ShiftExpression, Tokens.ShiftRight, AdditiveExpression)
                .def(ShiftExpression, Tokens.ShiftLeft, AdditiveExpression);

        target(RelationalExpression)
                .def(ShiftExpression)
                .def(RelationalExpression, Tokens.Lt, ShiftExpression)
                .def(RelationalExpression, Tokens.Gt, ShiftExpression)
                .def(RelationalExpression, Tokens.Lte, ShiftExpression)
                .def(RelationalExpression, Tokens.Gte, ShiftExpression);

        target(EqualityExpression)
                .def(RelationalExpression)
                .def(EqualityExpression, Tokens.EqEq, RelationalExpression)
                .def(EqualityExpression, Tokens.NEq, RelationalExpression);

        target(AndExpression)
                .def(EqualityExpression)
                .def(AndExpression, Tokens.And, EqualityExpression);

        target(ExclusiveOrExpression)
                .def(AndExpression)
                .def(ExclusiveOrExpression, Tokens.Pow, AndExpression);

        target(InclusiveOrExpression)
                .def(ExclusiveOrExpression)
                .def(InclusiveOrExpression, Tokens.Pipe, ExclusiveOrExpression);

        target(LogicalAndExpression)
                .def(InclusiveOrExpression)
                .def(LogicalAndExpression, Tokens.AndAnd, InclusiveOrExpression);

        target(LogicalOrExpression)
                .def(LogicalAndExpression)
                .def(LogicalOrExpression, Tokens.PipePipe, LogicalAndExpression);

        target(ConditionalExpression)
                .def(LogicalOrExpression)
                .def(LogicalOrExpression, Tokens.Question, Expression, Tokens.DualPoint, Expression);

        target(AssignmentExpression)
                .def(ConditionalExpression)
                .def(UnaryExpression, or(Tokens.Eq, Tokens.MinusEq, Tokens.PlusEq, Tokens.MulEq, Tokens.DivEq, Tokens.ModEq, Tokens.ShiftLeftEq, Tokens.ShiftRightEq, Tokens.AndEq, Tokens.PowEq, Tokens.PipeEq), AssignmentExpression);
        //  .def(digitSequence)

        target(Expression)
           .def(AssignmentExpression)
           .def(Expression, Tokens.Comma, AssignmentExpression);

        target(ConstantExpression)
           .def(ConditionalExpression);


        target(Declaration)
           .def(oneOrMore(DeclarationSpecifier), optional(InitDeclarator), zeroOrMore(Tokens.Comma, InitDeclarator), Tokens.DotComma)
           .def(oneOrMore(DeclarationSpecifier))
           .def(StaticAssertDeclaration);
    }


}
