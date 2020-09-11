
import { Terminal } from "../../common/Terminal";
import { NonTerminal } from "../../common/NonTerminal";
import { SpecialTerminal } from "../../common/SpecialTerminal";

import { Literal } from "../../lexer/Literal";
import { SingleChar } from "../../lexer/SingleChar";

import { Grammar } from "../../parser/Grammar";
import { QuotedString } from "../../lexer/QuotedString";
import { RegexTerminal } from "../../lexer/RegexTerminal";


export class C89Grammar extends Grammar {

    //Terminals
    static Volatile: Terminal = new Literal("volatile");
    static Minus: Terminal = new SingleChar('-');
    static Ne_op: Terminal = new Literal("!=");
    static Left_op: Terminal = new Literal("<<");
    static RightBrace: Terminal = new SingleChar(')');
    static Mod: Terminal = new SingleChar('%');
    static Right_op: Terminal = new Literal(">>");
    static Pipe: Terminal = new SingleChar('|');
    static Do: Terminal = new Literal("do");
    static ExclamationMark: Terminal = new SingleChar('!');
    static Static: Terminal = new Literal("static");
    static Gt: Terminal = new SingleChar('>');
    static LeftSquareBrace: Terminal = new SingleChar('[');
    static DualPoint: Terminal = new SingleChar(':');
    static And_assign: Terminal = new Literal("&=");
    static Const: Terminal = new Literal("const");
    static Break: Terminal = new Literal("break");
    static Or_assign: Terminal = new Literal("|=");
    static Typedef: Terminal = new Literal("typedef");
    static Else: Terminal = new Literal("else");
    static Extern: Terminal = new Literal("extern");
    static If: Terminal = new Literal("if");
    static Dot: Terminal = new SingleChar('.');
    static Register: Terminal = new Literal("register");
    static Enum: Terminal = new Literal("enum");
    static ShiftRight_assign: Terminal = new Literal(">>=");
    static Mul: Terminal = new SingleChar('*');
    static Eq_op: Terminal = new Literal("==");
    static And: Terminal = new SingleChar('&');
    static Le_op: Terminal = new Literal("<=");
    static For: Terminal = new Literal("for");
    static Dec_op: Terminal = new Literal("--");
    static QuestionMark: Terminal = new SingleChar('?');
    static Case: Terminal = new Literal("case");
    static Auto: Terminal = new Literal("auto");
    static RightCurlyBrace: Terminal = new SingleChar('}');
    static DotComma: Terminal = new SingleChar(';');
    static Ellipsis: Terminal = new Literal("...");
    static Ptr_op: Terminal = new Literal("->");
    static Switch: Terminal = new Literal("switch");
    static Void: Terminal = new Literal("void");
    static Struct: Terminal = new Literal("struct");
    static Div: Terminal = new SingleChar('/');
    static And_op: Terminal = new Literal("&&");
    static Or_op: Terminal = new Literal("||");
    static Float: Terminal = new Literal("float");
    static Goto: Terminal = new Literal("goto");
    static Plus: Terminal = new SingleChar('+');
    static Div_assign: Terminal = new Literal("/=");
    static Sub_assign: Terminal = new Literal("-=")
    static Unsigned: Terminal = new Literal("unsigned");
    static Sizeof: Terminal = new Literal("sizeof");
    static Char: Terminal = new Literal("char");
    static Int: Terminal = new Literal("int");
    static Tilde: Terminal = new SingleChar('~');
    static RightSquareBrace: Terminal = new SingleChar(']');
    static Return: Terminal = new Literal("return");
    static Lt: Terminal = new SingleChar('<');
    static Signed: Terminal = new Literal("signed");
    static Mul_assign: Terminal = new Literal("*=");
    static Identifier: Terminal = new RegexTerminal("identifier", "('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_')*");
    static Add_assign: Terminal = new Literal("+=");
    static Double: Terminal = new Literal("double");
    static Long: Terminal = new Literal("long");
    static Comma: Terminal = new SingleChar(',');
    static Xor_assign: Terminal = new Literal("^=");
    static LeftBrace: Terminal = new SingleChar('(');
    static Ge_op: Terminal = new Literal(">=");
    static Short: Terminal = new Literal("short");
    static Pow: Terminal = new SingleChar('^');
    static Continue: Terminal = new Literal("continue");
    static Eq: Terminal = new SingleChar('=');
    static LeftCurlyBrace: Terminal = new SingleChar('{');
    static Mod_assign: Terminal = new Literal("%=");
    static String_literal: Terminal = new QuotedString('"', '"', '\\\\', 'r');
    static ShiftLeft_assign: Terminal = new Literal("<<=");
    static While: Terminal = new Literal("while");
    static Union: Terminal = new Literal("union");
    static Inc_op: Terminal = new Literal("++");
    static Default: Terminal = new Literal("default");
    static TypeName: Terminal = new SpecialTerminal("typename");

    static CInteger: Terminal = new RegexTerminal("integer", "\"i\""); // FIXME
    static CBinary: Terminal = new RegexTerminal("binary", "\"i\""); // FIXME
    static COctal: Terminal = new RegexTerminal("octal", "\"i\""); // FIXME
    static CHexNumber: Terminal = new RegexTerminal("hexnumber", "\"i\""); // FIXME
    static CFloatingPoint: Terminal = new RegexTerminal("floating", "\"i\""); // FIXME
    static CCharacter: Terminal = new RegexTerminal("character", "\"i\""); // FIXME

    constructor() {
        super();

        const CastExpression = new NonTerminal("CastExpression ");
        const CompoundStatement = new NonTerminal("CompoundStatement ");
        const TypeName = new NonTerminal("TypeName ");
        const InitDeclarator = new NonTerminal("InitDeclarator ");
        const PostfixExpression = new NonTerminal("PostfixExpression ");
        const DirectDeclarator = new NonTerminal("DirectDeclarator ");
        const UnaryExpression = new NonTerminal("UnaryExpression ");
        const LogicalOrExpression = new NonTerminal("LogicalOrExpression ");
        const StructDeclarationList = new NonTerminal("StructDeclarationList ");
        const AndExpression = new NonTerminal("AndExpression ");
        const IterationStatement = new NonTerminal("IterationStatement ");
        const AdditiveExpression = new NonTerminal("AdditiveExpression ");
        const AbstractDeclarator = new NonTerminal("AbstractDeclarator ");
        const DirectAbstractDeclarator = new NonTerminal("DirectAbstractDeclarator ");
        const AssignmentOperator = new NonTerminal("AssignmentOperator ");
        const MultiplicativeExpression = new NonTerminal("MultiplicativeExpression ");
        const EnumSpecifier = new NonTerminal("EnumSpecifier ");
        const PrimaryExpression = new NonTerminal("PrimaryExpression ");
        const Pointer = new NonTerminal("Pointer ");
        const UnaryOperator = new NonTerminal("UnaryOperator ");
        const DeclarationList = new NonTerminal("DeclarationList ");
        const IdentifierList = new NonTerminal("IdentifierList ");
        const EnumeratorList = new NonTerminal("EnumeratorList ");
        const StructOrUnionSpecifier = new NonTerminal("StructOrUnionSpecifier ");
        const ExclusiveOrExpression = new NonTerminal("ExclusiveOrExpression ");
        const DeclarationSpecifiers = new NonTerminal("DeclarationSpecifiers ");
        const TypeQualifierList = new NonTerminal("TypeQualifierList ");
        const StructDeclarator = new NonTerminal("StructDeclarator ");
        const StructOrUnion = new NonTerminal("StructOrUnion ");
        const EqualityExpression = new NonTerminal("EqualityExpression ");
        const InitializerList = new NonTerminal("InitializerList ");
        const StructDeclaratorList = new NonTerminal("StructDeclaratorList ");
        const ParameterTypeList = new NonTerminal("ParameterTypeList ");
        const TranslationUnit = new NonTerminal("TranslationUnit ");
        const InitDeclaratorList = new NonTerminal("InitDeclaratorList ");
        const InclusiveOrExpression = new NonTerminal("InclusiveOrExpression ");
        const ConditionalExpression = new NonTerminal("ConditionalExpression ");
        const SelectionStatement = new NonTerminal("SelectionStatement ");
        const ConstantExpression = new NonTerminal("ConstantExpression ");
        const SpecifierQualifierList = new NonTerminal("SpecifierQualifierList ");
        const Statement = new NonTerminal("Statement ");
        const TypeQualifier = new NonTerminal("TypeQualifier ");
        const ShiftExpression = new NonTerminal("ShiftExpression ");
        const Enumerator = new NonTerminal("Enumerator ");
        const LabeledStatement = new NonTerminal("LabeledStatement ");
        const StatementList = new NonTerminal("StatementList ");
        const ExternalDeclaration = new NonTerminal("ExternalDeclaration ");
        const Expression = new NonTerminal("Expression ");
        const TypeSpecifier = new NonTerminal("TypeSpecifier ");
        const ExpressionStatement = new NonTerminal("ExpressionStatement ");
        const ArgumentExpressionList = new NonTerminal("ArgumentExpressionList ");
        const ParameterDeclaration = new NonTerminal("ParameterDeclaration ");
        const AssignmentExpression = new NonTerminal("AssignmentExpression ");
        const Declaration = new NonTerminal("Declaration ");
        const Declarator = new NonTerminal("Declarator ");
        const Initializer = new NonTerminal("Initializer ");
        const StructDeclaration = new NonTerminal("StructDeclaration ");
        const StorageClassSpecifier = new NonTerminal("StorageClassSpecifier ");
        const LogicalAndExpression = new NonTerminal("LogicalAndExpression ");
        const RelationalExpression = new NonTerminal("RelationalExpression ");
        const ParameterList = new NonTerminal("ParameterList ");
        const JumpStatement = new NonTerminal("JumpStatement ");
        const FunctionDefinition = new NonTerminal("FunctionDefinition ");
        const CompilationUnit = new NonTerminal("CompilationUnit ");
        const Constant = new NonTerminal("Constant ");
        const ForDeclaration = new NonTerminal("ForDeclaration ");
        const ForCondition = new NonTerminal("ForCondition ");
        const ForExpression = new NonTerminal("ForExpression ");

        this.setTargetRule(this.defineRule(CompilationUnit, [TranslationUnit]));
        this.defineRule(Constant, [this.oneOrMore(C89Grammar.String_literal)]);
        this.defineRule(Constant, [C89Grammar.CInteger]);
        this.defineRule(Constant, [C89Grammar.CBinary]);
        this.defineRule(Constant, [C89Grammar.COctal]);
        this.defineRule(Constant, [C89Grammar.CHexNumber]);
        this.defineRule(Constant, [C89Grammar.CFloatingPoint]);
        this.defineRule(Constant, [C89Grammar.CCharacter]);
        this.defineRule(PrimaryExpression, [C89Grammar.Identifier]);
        this.defineRule(PrimaryExpression, [Constant]);
        this.defineRule(PrimaryExpression, [C89Grammar.String_literal]);
        this.defineRule(PrimaryExpression, [C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace]);
        this.defineRule(PostfixExpression, [PrimaryExpression]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.LeftSquareBrace, Expression, C89Grammar.RightSquareBrace]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.LeftBrace, C89Grammar.RightBrace]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.LeftBrace, ArgumentExpressionList, C89Grammar.RightBrace]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.Dot, C89Grammar.Identifier]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.Ptr_op, C89Grammar.Identifier]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.Inc_op]);
        this.defineRule(PostfixExpression, [PostfixExpression, C89Grammar.Dec_op]);
        this.defineRule(ArgumentExpressionList, [AssignmentExpression]);
        this.defineRule(ArgumentExpressionList, [ArgumentExpressionList, C89Grammar.Comma, AssignmentExpression]);
        this.defineRule(UnaryExpression, [PostfixExpression]);
        this.defineRule(UnaryExpression, [C89Grammar.Inc_op, UnaryExpression]);
        this.defineRule(UnaryExpression, [C89Grammar.Dec_op, UnaryExpression]);
        this.defineRule(UnaryExpression, [UnaryOperator, CastExpression]);
        this.defineRule(UnaryExpression, [C89Grammar.Sizeof, UnaryExpression]);
        this.defineRule(UnaryExpression, [C89Grammar.Sizeof, C89Grammar.LeftBrace, TypeName, C89Grammar.RightBrace]);
        this.defineRule(UnaryOperator, [C89Grammar.And]);
        this.defineRule(UnaryOperator, [C89Grammar.Mul]);
        this.defineRule(UnaryOperator, [C89Grammar.Plus]);
        this.defineRule(UnaryOperator, [C89Grammar.Minus]);
        this.defineRule(UnaryOperator, [C89Grammar.Tilde]);
        this.defineRule(UnaryOperator, [C89Grammar.ExclamationMark]);
        this.defineRule(CastExpression, [UnaryExpression]);
        this.defineRule(CastExpression, [C89Grammar.LeftBrace, TypeName, C89Grammar.RightBrace, CastExpression]);
        this.defineRule(MultiplicativeExpression, [CastExpression]);
        this.defineRule(MultiplicativeExpression, [MultiplicativeExpression, C89Grammar.Mul, CastExpression]);
        this.defineRule(MultiplicativeExpression, [MultiplicativeExpression, C89Grammar.Div, CastExpression]);
        this.defineRule(MultiplicativeExpression, [MultiplicativeExpression, C89Grammar.Mod, CastExpression]);
        this.defineRule(AdditiveExpression, [MultiplicativeExpression]);
        this.defineRule(AdditiveExpression, [AdditiveExpression, C89Grammar.Plus, MultiplicativeExpression]);
        this.defineRule(AdditiveExpression, [AdditiveExpression, C89Grammar.Minus, MultiplicativeExpression]);
        this.defineRule(ShiftExpression, [AdditiveExpression]);
        this.defineRule(ShiftExpression, [ShiftExpression, C89Grammar.Left_op, AdditiveExpression]);
        this.defineRule(ShiftExpression, [ShiftExpression, C89Grammar.Right_op, AdditiveExpression]);
        this.defineRule(RelationalExpression, [ShiftExpression]);
        this.defineRule(RelationalExpression, [RelationalExpression, C89Grammar.Lt, ShiftExpression]);
        this.defineRule(RelationalExpression, [RelationalExpression, C89Grammar.Gt, ShiftExpression]);
        this.defineRule(RelationalExpression, [RelationalExpression, C89Grammar.Le_op, ShiftExpression]);
        this.defineRule(RelationalExpression, [RelationalExpression, C89Grammar.Ge_op, ShiftExpression]);
        this.defineRule(EqualityExpression, [RelationalExpression]);
        this.defineRule(EqualityExpression, [EqualityExpression, C89Grammar.Eq_op, RelationalExpression]);
        this.defineRule(EqualityExpression, [EqualityExpression, C89Grammar.Ne_op, RelationalExpression]);
        this.defineRule(AndExpression, [EqualityExpression]);
        this.defineRule(AndExpression, [AndExpression, C89Grammar.And, EqualityExpression]);
        this.defineRule(ExclusiveOrExpression, [AndExpression]);
        this.defineRule(ExclusiveOrExpression, [ExclusiveOrExpression, C89Grammar.Pow, AndExpression]);
        this.defineRule(InclusiveOrExpression, [ExclusiveOrExpression]);
        this.defineRule(InclusiveOrExpression, [InclusiveOrExpression, C89Grammar.Pipe, ExclusiveOrExpression]);
        this.defineRule(LogicalAndExpression, [InclusiveOrExpression]);
        this.defineRule(LogicalAndExpression, [LogicalAndExpression, C89Grammar.And_op, InclusiveOrExpression]);
        this.defineRule(LogicalOrExpression, [LogicalAndExpression]);
        this.defineRule(LogicalOrExpression, [LogicalOrExpression, C89Grammar.Or_op, LogicalAndExpression]);
        this.defineRule(ConditionalExpression, [LogicalOrExpression]);
        this.defineRule(ConditionalExpression, [LogicalOrExpression, C89Grammar.QuestionMark, Expression, C89Grammar.DualPoint, ConditionalExpression]);
        this.defineRule(AssignmentExpression, [ConditionalExpression]);
        this.defineRule(AssignmentExpression, [UnaryExpression, AssignmentOperator, AssignmentExpression]);
        this.defineRule(AssignmentOperator, [C89Grammar.Eq]);
        this.defineRule(AssignmentOperator, [C89Grammar.Mul_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Div_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Mod_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Add_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Sub_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.ShiftLeft_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.ShiftRight_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.And_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Xor_assign]);
        this.defineRule(AssignmentOperator, [C89Grammar.Or_assign]);
        this.defineRule(Expression, [AssignmentExpression]);
        this.defineRule(Expression, [Expression, C89Grammar.Comma, AssignmentExpression]);
        this.defineRule(ConstantExpression, [ConditionalExpression]);
        this.defineRule(Declaration, [DeclarationSpecifiers, C89Grammar.DotComma]);
        this.defineRule(DeclarationSpecifiers, [StorageClassSpecifier]);
        this.defineRule(DeclarationSpecifiers, [StorageClassSpecifier, DeclarationSpecifiers]);
        this.defineRule(DeclarationSpecifiers, [TypeSpecifier]);
        this.defineRule(DeclarationSpecifiers, [TypeSpecifier, DeclarationSpecifiers]);
        this.defineRule(DeclarationSpecifiers, [TypeQualifier]);
        this.defineRule(DeclarationSpecifiers, [TypeQualifier, DeclarationSpecifiers]);
        this.defineRule(InitDeclaratorList, [InitDeclarator]);
        this.defineRule(InitDeclaratorList, [InitDeclaratorList, C89Grammar.Comma, InitDeclarator]);
        this.defineRule(InitDeclarator, [Declarator]);
        this.defineRule(InitDeclarator, [Declarator, C89Grammar.Eq, Initializer]);
        this.defineRule(StorageClassSpecifier, [C89Grammar.Typedef]);
        this.defineRule(StorageClassSpecifier, [C89Grammar.Extern]);
        this.defineRule(StorageClassSpecifier, [C89Grammar.Static]);
        this.defineRule(StorageClassSpecifier, [C89Grammar.Auto]);
        this.defineRule(StorageClassSpecifier, [C89Grammar.Register]);
        this.defineRule(TypeSpecifier, [this.oneOf(C89Grammar.Void,
            C89Grammar.Char,
            C89Grammar.Short,
            C89Grammar.Int,
            C89Grammar.Long,
            C89Grammar.Float,
            C89Grammar.Double,
            C89Grammar.Signed,
            C89Grammar.Unsigned,
            C89Grammar.TypeName)]);
        this.defineRule(TypeSpecifier, [StructOrUnionSpecifier]);
        this.defineRule(TypeSpecifier, [EnumSpecifier]);
        this.defineRule(StructOrUnionSpecifier, [StructOrUnion, C89Grammar.Identifier, C89Grammar.LeftCurlyBrace, StructDeclarationList, C89Grammar.RightCurlyBrace]);
        this.defineRule(StructOrUnionSpecifier, [StructOrUnion, C89Grammar.LeftCurlyBrace, StructDeclarationList, C89Grammar.RightCurlyBrace]);
        this.defineRule(StructOrUnionSpecifier, [StructOrUnion, C89Grammar.Identifier]);
        this.defineRule(StructOrUnion, [C89Grammar.Struct]);
        this.defineRule(StructOrUnion, [C89Grammar.Union]);
        this.defineRule(StructDeclarationList, [StructDeclaration]);
        this.defineRule(StructDeclarationList, [StructDeclarationList, StructDeclaration]);
        this.defineRule(StructDeclaration, [SpecifierQualifierList, StructDeclaratorList, C89Grammar.DotComma]);
        this.defineRule(SpecifierQualifierList, [TypeSpecifier, SpecifierQualifierList]);
        this.defineRule(SpecifierQualifierList, [TypeSpecifier]);
        this.defineRule(SpecifierQualifierList, [TypeQualifier, SpecifierQualifierList]);
        this.defineRule(SpecifierQualifierList, [TypeQualifier]);
        this.defineRule(StructDeclaratorList, [StructDeclarator]);
        this.defineRule(StructDeclaratorList, [StructDeclaratorList, C89Grammar.Comma, StructDeclarator]);
        this.defineRule(StructDeclarator, [Declarator]);
        this.defineRule(StructDeclarator, [C89Grammar.DualPoint, ConstantExpression]);
        this.defineRule(StructDeclarator, [Declarator, C89Grammar.DualPoint, ConstantExpression]);
        this.defineRule(EnumSpecifier, [C89Grammar.Enum, C89Grammar.LeftCurlyBrace, EnumeratorList, C89Grammar.RightCurlyBrace]);
        this.defineRule(EnumSpecifier, [C89Grammar.Enum, C89Grammar.Identifier, C89Grammar.LeftCurlyBrace, EnumeratorList, C89Grammar.RightCurlyBrace]);
        this.defineRule(EnumSpecifier, [C89Grammar.Enum, C89Grammar.Identifier]);
        this.defineRule(EnumeratorList, [Enumerator]);
        this.defineRule(EnumeratorList, [EnumeratorList, C89Grammar.Comma, Enumerator]);
        this.defineRule(Enumerator, [C89Grammar.Identifier]);
        this.defineRule(Enumerator, [C89Grammar.Identifier, C89Grammar.Eq, ConstantExpression]);
        this.defineRule(TypeQualifier, [C89Grammar.Const]);
        this.defineRule(TypeQualifier, [C89Grammar.Volatile]);
        this.defineRule(Declarator, [Pointer, DirectDeclarator]);
        this.defineRule(Declarator, [DirectDeclarator]);
        this.defineRule(DirectDeclarator, [C89Grammar.Identifier]);
        this.defineRule(DirectDeclarator, [C89Grammar.LeftBrace, Declarator, C89Grammar.RightBrace]);
        this.defineRule(DirectDeclarator, [DirectDeclarator, C89Grammar.LeftSquareBrace, ConstantExpression, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectDeclarator, [DirectDeclarator, C89Grammar.LeftSquareBrace, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectDeclarator, [DirectDeclarator, C89Grammar.LeftBrace, ParameterTypeList, C89Grammar.RightBrace]);
        this.defineRule(DirectDeclarator, [DirectDeclarator, C89Grammar.LeftBrace, IdentifierList, C89Grammar.RightBrace]);
        this.defineRule(DirectDeclarator, [DirectDeclarator, C89Grammar.LeftBrace, C89Grammar.RightBrace]);
        this.defineRule(Pointer, [C89Grammar.Mul]);
        this.defineRule(Pointer, [C89Grammar.Mul, TypeQualifierList]);
        this.defineRule(Pointer, [C89Grammar.Mul, Pointer]);
        this.defineRule(Pointer, [C89Grammar.Mul, TypeQualifierList, Pointer]);
        this.defineRule(TypeQualifierList, [TypeQualifier]);
        this.defineRule(TypeQualifierList, [TypeQualifierList, TypeQualifier]);
        this.defineRule(ParameterTypeList, [ParameterList]);
        this.defineRule(ParameterTypeList, [ParameterList, C89Grammar.Comma, C89Grammar.Ellipsis]);
        this.defineRule(ParameterList, [ParameterDeclaration]);
        this.defineRule(ParameterList, [ParameterList, C89Grammar.Comma, ParameterDeclaration]);
        this.defineRule(ParameterDeclaration, [DeclarationSpecifiers, Declarator]);
        this.defineRule(ParameterDeclaration, [DeclarationSpecifiers, AbstractDeclarator]);
        this.defineRule(ParameterDeclaration, [DeclarationSpecifiers]);
        this.defineRule(IdentifierList, [C89Grammar.Identifier]);
        this.defineRule(IdentifierList, [IdentifierList, C89Grammar.Comma, C89Grammar.Identifier]);
        this.defineRule(TypeName, [SpecifierQualifierList]);
        this.defineRule(TypeName, [SpecifierQualifierList, AbstractDeclarator]);
        this.defineRule(AbstractDeclarator, [Pointer]);
        this.defineRule(AbstractDeclarator, [DirectAbstractDeclarator]);
        this.defineRule(AbstractDeclarator, [Pointer, DirectAbstractDeclarator]);
        this.defineRule(DirectAbstractDeclarator, [C89Grammar.LeftBrace, AbstractDeclarator, C89Grammar.RightBrace]);
        this.defineRule(DirectAbstractDeclarator, [C89Grammar.LeftSquareBrace, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectAbstractDeclarator, [C89Grammar.LeftSquareBrace, ConstantExpression, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectAbstractDeclarator, [DirectAbstractDeclarator, C89Grammar.LeftSquareBrace, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectAbstractDeclarator, [DirectAbstractDeclarator, C89Grammar.LeftSquareBrace, ConstantExpression, C89Grammar.RightSquareBrace]);
        this.defineRule(DirectAbstractDeclarator, [C89Grammar.LeftBrace, C89Grammar.RightBrace]);
        this.defineRule(DirectAbstractDeclarator, [C89Grammar.LeftBrace, ParameterTypeList, C89Grammar.RightBrace]);
        this.defineRule(DirectAbstractDeclarator, [DirectAbstractDeclarator, C89Grammar.LeftBrace, C89Grammar.RightBrace]);
        this.defineRule(DirectAbstractDeclarator, [DirectAbstractDeclarator, C89Grammar.LeftBrace, ParameterTypeList, C89Grammar.RightBrace]);
        this.defineRule(Initializer, [AssignmentExpression]);
        this.defineRule(Initializer, [C89Grammar.LeftCurlyBrace, InitializerList, C89Grammar.RightCurlyBrace]);
        this.defineRule(Initializer, [C89Grammar.LeftCurlyBrace, InitializerList, C89Grammar.Comma, C89Grammar.RightCurlyBrace]);
        this.defineRule(InitializerList, [Initializer]);
        this.defineRule(InitializerList, [InitializerList, C89Grammar.Comma, Initializer]);
        this.defineRule(Statement, [LabeledStatement]);
        this.defineRule(Statement, [CompoundStatement]);
        this.defineRule(Statement, [ExpressionStatement]);
        this.defineRule(Statement, [SelectionStatement]);
        this.defineRule(Statement, [IterationStatement]);
        this.defineRule(Statement, [JumpStatement]);
        this.defineRule(LabeledStatement, [C89Grammar.Identifier, C89Grammar.DualPoint, Statement]);
        this.defineRule(LabeledStatement, [C89Grammar.Case, ConstantExpression, C89Grammar.DualPoint, Statement]);
        this.defineRule(LabeledStatement, [C89Grammar.Default, C89Grammar.DualPoint, Statement]);
        this.defineRule(CompoundStatement, [C89Grammar.LeftCurlyBrace, C89Grammar.RightCurlyBrace]);
        this.defineRule(CompoundStatement, [C89Grammar.LeftCurlyBrace, StatementList, C89Grammar.RightCurlyBrace]);
        this.defineRule(CompoundStatement, [C89Grammar.LeftCurlyBrace, DeclarationList, C89Grammar.RightCurlyBrace]);
        this.defineRule(CompoundStatement, [C89Grammar.LeftCurlyBrace, DeclarationList, StatementList, C89Grammar.RightCurlyBrace]);
        this.defineRule(DeclarationList, [Declaration]);
        this.defineRule(DeclarationList, [DeclarationList, Declaration]);
        this.defineRule(StatementList, [Statement]);
        this.defineRule(StatementList, [StatementList, Statement]);
        this.defineRule(ExpressionStatement, [C89Grammar.DotComma]);
        this.defineRule(ExpressionStatement, [Expression, C89Grammar.DotComma]);
        this.defineRule(SelectionStatement, [C89Grammar.If, C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace, Statement]);
        this.defineRule(SelectionStatement, [C89Grammar.If, C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace, Statement, C89Grammar.Else, Statement]);
        this.defineRule(SelectionStatement, [C89Grammar.Switch, C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace, Statement]);
        this.defineRule(IterationStatement, [C89Grammar.While, C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace, Statement]);
        this.defineRule(IterationStatement, [C89Grammar.Do, Statement, C89Grammar.While, C89Grammar.LeftBrace, Expression, C89Grammar.RightBrace, C89Grammar.DotComma]);
        this.defineRule(IterationStatement, [C89Grammar.For, C89Grammar.LeftBrace, ForCondition, C89Grammar.RightBrace, Statement]);
        this.defineRule(ForCondition, [ForDeclaration, C89Grammar.DotComma, ForExpression, C89Grammar.DotComma, ForExpression]);
        this.defineRule(ForCondition, [Expression, C89Grammar.DotComma, ForExpression, C89Grammar.DotComma, ForExpression]);
        this.defineRule(ForExpression, [this.listOf(AssignmentExpression, C89Grammar.Comma, true)]);
        this.defineRule(ForDeclaration, [DeclarationSpecifiers, InitDeclaratorList]);
        this.defineRule(ForDeclaration, [DeclarationSpecifiers]);
        this.defineRule(JumpStatement, [C89Grammar.Goto, C89Grammar.Identifier, C89Grammar.DotComma]);
        this.defineRule(JumpStatement, [C89Grammar.Continue, C89Grammar.DotComma]);
        this.defineRule(JumpStatement, [C89Grammar.Break, C89Grammar.DotComma]);
        this.defineRule(JumpStatement, [C89Grammar.Return, C89Grammar.DotComma]);
        this.defineRule(JumpStatement, [C89Grammar.Return, Expression, C89Grammar.DotComma]);
        this.defineRule(TranslationUnit, [ExternalDeclaration]);
        this.defineRule(TranslationUnit, [TranslationUnit, ExternalDeclaration]);
        this.defineRule(ExternalDeclaration, [FunctionDefinition]);
        this.defineRule(ExternalDeclaration, [Declaration]);
        this.defineRule(FunctionDefinition, [DeclarationSpecifiers, Declarator, DeclarationList, CompoundStatement]);
        this.defineRule(FunctionDefinition, [DeclarationSpecifiers, Declarator, CompoundStatement]);
        this.defineRule(FunctionDefinition, [Declarator, DeclarationList, CompoundStatement]);
        this.defineRule(FunctionDefinition, [Declarator, CompoundStatement]);

         // The well-known "typedef problem" with parsing C is that the standard C grammar is ambiguous unless the lexer distinguishes identifiers bound by typedef and other identifiers as two separate lexical classes.
         // http://calculist.blogspot.fr/2009/02/c-typedef-parsing-problem.html
         //
         let typedefRule = this.defineRule(Declaration, [DeclarationSpecifiers, InitDeclaratorList, C89Grammar.DotComma]);
         typedefRule.setReduceAction( node => {
             /*
                         AstNode astNode = parsingContext.getAstNode();
            AstNode storageClass = astNode.getChildOfType(DeclarationSpecifiers).getChildOfType(StorageClassSpecifier);

            //when the declaration is a typedef
            if (storageClass != null && storageClass.repr().equals("typedef")) {
                AstNode initDeclaratorList = astNode.getChildOfType(InitDeclaratorList);

                //for each declarator
                for (AstNode initDeclarator : initDeclaratorList.getDescendantsOfType(InitDeclarator)) {
                    AstNode declarator = initDeclarator.getChildOfType(Declarator);

                    //find its name
                    String name = getDeclaratorName(declarator);

                    //update lexer context
                    LexerStream lexerStream = parsingContext.getLexerStream();
                    LexerHack lh = (LexerHack) lexerStream.getLexer().getTokenListener();
                    lh.addTypeName(name);
                }
            }
             */
         });
    }
};