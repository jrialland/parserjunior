package net.jr.grammar.c;

import net.jr.common.Symbol;
import net.jr.lexer.*;
import net.jr.parser.Associativity;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;
import net.jr.parser.Parser;
import net.jr.parser.ast.AstNode;
import net.jr.parser.impl.LRParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;

/**
 * ANSI C Grammar
 */
public class CGrammar extends Grammar {

    private static final Logger LOGGER = LoggerFactory.getLogger(CGrammar.class);

    private static final Logger getLog() {
        return LOGGER;
    }

    public static final class Tokens {
        public static Lexeme Volatile = Lexemes.literal("volatile", "volatile");
        public static Lexeme Minus = Lexemes.singleChar('-', "minus");
        public static Lexeme Ne_op = Lexemes.literal("!=", "notEq");
        public static Lexeme Left_op = Lexemes.literal("<<", "shiftLeft");
        public static Lexeme RightBrace = Lexemes.singleChar(')', "rightParen");
        public static Lexeme Mod = Lexemes.singleChar('%', "modulo");
        public static Lexeme Right_op = Lexemes.literal(">>", "shiftRight");
        public static Lexeme Pipe = Lexemes.singleChar('|', "bitwiseOr");
        public static Lexeme Do = Lexemes.literal("do", "do");
        public static Lexeme ExclamationMark = Lexemes.singleChar('!', "exclamationMark");
        public static Lexeme Static = Lexemes.literal("static", "static");
        public static Lexeme Gt = Lexemes.singleChar('>', "gt");
        public static Lexeme LeftSquareBrace = Lexemes.singleChar('[', "leftSquareBracket");
        public static Lexeme DualPoint = Lexemes.singleChar(':', "twoPoints");
        public static Lexeme And_assign = Lexemes.literal("&=", "andEq");
        public static Lexeme Const = Lexemes.literal("const", "const");
        public static Lexeme Break = Lexemes.literal("break", "break");
        public static Lexeme Or_assign = Lexemes.literal("|=", "bitwiseOrEq");
        public static Lexeme Typedef = Lexemes.literal("typedef", "typedef");
        public static Lexeme Else = Lexemes.literal("else", "else");
        public static Lexeme Extern = Lexemes.literal("extern", "extern");
        public static Lexeme If = Lexemes.literal("if", "if");
        public static Lexeme Dot = Lexemes.singleChar('.', "dot");
        public static Lexeme Register = Lexemes.literal("register", "register");
        public static Lexeme Enum = Lexemes.literal("enum", "enum");
        public static Lexeme ShiftRight_assign = Lexemes.literal(">>=", "rightShiftEq");
        public static Lexeme Mul = Lexemes.singleChar('*', "mult");
        public static Lexeme Eq_op = Lexemes.literal("==", "eqEq");
        public static Lexeme And = Lexemes.singleChar('&', "bitwiseAnd");
        public static Lexeme Le_op = Lexemes.literal("<=", "lte");
        public static Lexeme For = Lexemes.literal("for", "for");
        public static Lexeme Dec_op = Lexemes.literal("--", "minusMinus");
        public static Lexeme QuestionMark = Lexemes.singleChar('?', "questionMark");
        public static Lexeme Case = Lexemes.literal("case", "case");
        public static Lexeme Auto = Lexemes.literal("auto", "auto");
        public static Lexeme RightCurlyBrace = Lexemes.singleChar('}', "rightCurlyBrace");
        public static Lexeme DotComma = Lexemes.singleChar(';', "dotComma");
        public static Lexeme Ellipsis = Lexemes.literal("...", "threePoints");
        public static Lexeme Ptr_op = Lexemes.literal("->", "arrow");
        public static Lexeme Switch = Lexemes.literal("switch", "switch");
        public static Lexeme Void = Lexemes.literal("void", "void");
        public static Lexeme Struct = Lexemes.literal("struct", "struct");
        public static Lexeme Div = Lexemes.singleChar('/', "slash");
        public static Lexeme And_op = Lexemes.literal("&&", "logicalAnd");
        public static Lexeme Or_op = Lexemes.literal("||", "logicalOr");
        public static Lexeme Float = Lexemes.literal("float", "float");
        public static Lexeme Goto = Lexemes.literal("goto", "goto");
        public static Lexeme Plus = Lexemes.singleChar('+', "plus");
        public static Lexeme Div_assign = Lexemes.literal("/=", "divAssign");
        public static Lexeme Sub_assign = Lexemes.literal("-=","minusAssign");
        public static Lexeme Unsigned = Lexemes.literal("unsigned", "unsigned");
        public static Lexeme Sizeof = Lexemes.literal("sizeof", "sizeof");
        public static Lexeme Char = Lexemes.literal("char", "char");
        public static Lexeme Int = Lexemes.literal("int", "int");
        public static Lexeme Tilde = Lexemes.singleChar('~', "tilde");
        public static Lexeme RightSquareBrace = Lexemes.singleChar(']', "rightSquareBracket");
        public static Lexeme Return = Lexemes.literal("return", "return");
        public static Lexeme Lt = Lexemes.singleChar('<', "lt");
        public static Lexeme Signed = Lexemes.literal("signed", "signed");
        public static Lexeme Mul_assign = Lexemes.literal("*=", "mulAssign");
        public static Lexeme Identifier = Lexemes.cIdentifier();
        public static Lexeme Add_assign = Lexemes.literal("+=", "plusAssign");
        public static Lexeme Double = Lexemes.literal("double", "double");
        public static Lexeme Long = Lexemes.literal("long", "long");
        public static Lexeme Comma = Lexemes.singleChar(',', "comma");
        public static Lexeme Xor_assign = Lexemes.literal("^=", "bitwiseNotAssign");
        public static Lexeme LeftBrace = Lexemes.singleChar('(', "leftParen");
        public static Lexeme Ge_op = Lexemes.literal(">=", "gte");
        public static Lexeme Short = Lexemes.literal("short", "short");
        public static Lexeme Pow = Lexemes.singleChar('^',"bitwiseNot");
        public static Lexeme Continue = Lexemes.literal("continue", "continue");
        public static Lexeme Eq = Lexemes.singleChar('=', "eq");
        public static Lexeme LeftCurlyBrace = Lexemes.singleChar('{', "leftCurlyBrace");
        public static Lexeme Mod_assign = Lexemes.literal("%=", "moduloEq");
        public static Lexeme String_literal = Lexemes.cString();
        public static Lexeme ShiftLeft_assign = Lexemes.literal("<<=", "leftShiftAssign");
        public static Lexeme While = Lexemes.literal("while", "while");
        public static Lexeme Union = Lexemes.literal("union", "union");
        public static Lexeme Inc_op = Lexemes.literal("++", "plusPlus");
        public static Lexeme Default = Lexemes.literal("default", "default");
        public static Lexeme TypeName = Lexemes.artificial("typeName");
    }

    public static final Forward CastExpression = new Forward("CastExpression");
    public static final Forward CompoundStatement = new Forward("CompoundStatement");
    public static final Forward TypeName = new Forward("TypeName");
    public static final Forward InitDeclarator = new Forward("InitDeclarator");
    public static final Forward PostfixExpression = new Forward("PostfixExpression");
    public static final Forward DirectDeclarator = new Forward("DirectDeclarator");
    public static final Forward UnaryExpression = new Forward("UnaryExpression");
    public static final Forward LogicalOrExpression = new Forward("LogicalOrExpression");
    public static final Forward StructDeclarationList = new Forward("StructDeclarationList");
    public static final Forward AndExpression = new Forward("AndExpression");
    public static final Forward IterationStatement = new Forward("IterationStatement");
    public static final Forward AdditiveExpression = new Forward("AdditiveExpression");
    public static final Forward AbstractDeclarator = new Forward("AbstractDeclarator");
    public static final Forward DirectAbstractDeclarator = new Forward("DirectAbstractDeclarator");
    public static final Forward AssignmentOperator = new Forward("AssignmentOperator");
    public static final Forward MultiplicativeExpression = new Forward("MultiplicativeExpression");
    public static final Forward EnumSpecifier = new Forward("EnumSpecifier");
    public static final Forward PrimaryExpression = new Forward("PrimaryExpression");
    public static final Forward Pointer = new Forward("Pointer");
    public static final Forward UnaryOperator = new Forward("UnaryOperator");
    public static final Forward DeclarationList = new Forward("DeclarationList");
    public static final Forward IdentifierList = new Forward("IdentifierList");
    public static final Forward EnumeratorList = new Forward("EnumeratorList");
    public static final Forward StructOrUnionSpecifier = new Forward("StructOrUnionSpecifier");
    public static final Forward ExclusiveOrExpression = new Forward("ExclusiveOrExpression");
    public static final Forward DeclarationSpecifiers = new Forward("DeclarationSpecifiers");
    public static final Forward TypeQualifierList = new Forward("TypeQualifierList");
    public static final Forward StructDeclarator = new Forward("StructDeclarator");
    public static final Forward StructOrUnion = new Forward("StructOrUnion");
    public static final Forward EqualityExpression = new Forward("EqualityExpression");
    public static final Forward InitializerList = new Forward("InitializerList");
    public static final Forward StructDeclaratorList = new Forward("StructDeclaratorList");
    public static final Forward ParameterTypeList = new Forward("ParameterTypeList");
    public static final Forward TranslationUnit = new Forward("TranslationUnit");
    public static final Forward InitDeclaratorList = new Forward("InitDeclaratorList");
    public static final Forward InclusiveOrExpression = new Forward("InclusiveOrExpression");
    public static final Forward ConditionalExpression = new Forward("ConditionalExpression");
    public static final Forward SelectionStatement = new Forward("SelectionStatement");
    public static final Forward ConstantExpression = new Forward("ConstantExpression");
    public static final Forward SpecifierQualifierList = new Forward("SpecifierQualifierList");
    public static final Forward Statement = new Forward("Statement");
    public static final Forward TypeQualifier = new Forward("TypeQualifier");
    public static final Forward ShiftExpression = new Forward("ShiftExpression");
    public static final Forward Enumerator = new Forward("Enumerator");
    public static final Forward LabeledStatement = new Forward("LabeledStatement");
    public static final Forward StatementList = new Forward("StatementList");
    public static final Forward ExternalDeclaration = new Forward("ExternalDeclaration");
    public static final Forward Expression = new Forward("Expression");
    public static final Forward TypeSpecifier = new Forward("TypeSpecifier");
    public static final Forward ExpressionStatement = new Forward("ExpressionStatement");
    public static final Forward ArgumentExpressionList = new Forward("ArgumentExpressionList");
    public static final Forward ParameterDeclaration = new Forward("ParameterDeclaration");
    public static final Forward AssignmentExpression = new Forward("AssignmentExpression");
    public static final Forward Declaration = new Forward("Declaration");
    public static final Forward Declarator = new Forward("Declarator");
    public static final Forward Initializer = new Forward("Initializer");
    public static final Forward StructDeclaration = new Forward("StructDeclaration");
    public static final Forward StorageClassSpecifier = new Forward("StorageClassSpecifier");
    public static final Forward LogicalAndExpression = new Forward("LogicalAndExpression");
    public static final Forward RelationalExpression = new Forward("RelationalExpression");
    public static final Forward ParameterList = new Forward("ParameterList");
    public static final Forward JumpStatement = new Forward("JumpStatement");
    public static final Forward FunctionDefinition = new Forward("FunctionDefinition");
    public static final Forward CompilationUnit = new Forward("CompilationUnit");
    public static final Forward Constant = new Forward("Constant");

    public static final Forward ForDeclaration = new Forward("ForDeclaration");
    public static final Forward ForCondition = new Forward("ForCondition");
    public static final Forward ForExpression = new Forward("ForExpression");

    private Lexer lexer;

    public CGrammar() {

        setName("C");

        setTargetRule(addRule(CompilationUnit, TranslationUnit).get());

        addRule(Constant, oneOrMore(Tokens.String_literal));
        addRule(Constant, Lexemes.cInteger());
        addRule(Constant, Lexemes.cBinary());
        addRule(Constant, Lexemes.cOctal());
        addRule(Constant, Lexemes.cHexNumber());
        addRule(Constant, Lexemes.cFloatingPoint());
        addRule(Constant, Lexemes.cCharacter());

        addRule(PrimaryExpression, Tokens.Identifier);
        addRule(PrimaryExpression, Constant);
        addRule(PrimaryExpression, Tokens.String_literal);
        addRule(PrimaryExpression, Tokens.LeftBrace, Expression, Tokens.RightBrace);
        addRule(PostfixExpression, PrimaryExpression);
        addRule(PostfixExpression, PostfixExpression, Tokens.LeftSquareBrace, Expression, Tokens.RightSquareBrace);
        addRule(PostfixExpression, PostfixExpression, Tokens.LeftBrace, Tokens.RightBrace);
        addRule(PostfixExpression, PostfixExpression, Tokens.LeftBrace, ArgumentExpressionList, Tokens.RightBrace);
        addRule(PostfixExpression, PostfixExpression, Tokens.Dot, Tokens.Identifier);
        addRule(PostfixExpression, PostfixExpression, Tokens.Ptr_op, Tokens.Identifier);
        addRule(PostfixExpression, PostfixExpression, Tokens.Inc_op);
        addRule(PostfixExpression, PostfixExpression, Tokens.Dec_op);
        addRule(ArgumentExpressionList, AssignmentExpression);
        addRule(ArgumentExpressionList, ArgumentExpressionList, Tokens.Comma, AssignmentExpression);
        addRule(UnaryExpression, PostfixExpression);
        addRule(UnaryExpression, Tokens.Inc_op, UnaryExpression);
        addRule(UnaryExpression, Tokens.Dec_op, UnaryExpression);
        addRule(UnaryExpression, UnaryOperator, CastExpression);
        addRule(UnaryExpression, Tokens.Sizeof, UnaryExpression);
        addRule(UnaryExpression, Tokens.Sizeof, Tokens.LeftBrace, TypeName, Tokens.RightBrace);
        addRule(UnaryOperator, Tokens.And);
        addRule(UnaryOperator, Tokens.Mul);
        addRule(UnaryOperator, Tokens.Plus);
        addRule(UnaryOperator, Tokens.Minus);
        addRule(UnaryOperator, Tokens.Tilde);
        addRule(UnaryOperator, Tokens.ExclamationMark);
        addRule(CastExpression, UnaryExpression);
        addRule(CastExpression, Tokens.LeftBrace, TypeName, Tokens.RightBrace, CastExpression);
        addRule(MultiplicativeExpression, CastExpression);
        addRule(MultiplicativeExpression, MultiplicativeExpression, Tokens.Mul, CastExpression);
        addRule(MultiplicativeExpression, MultiplicativeExpression, Tokens.Div, CastExpression);
        addRule(MultiplicativeExpression, MultiplicativeExpression, Tokens.Mod, CastExpression);
        addRule(AdditiveExpression, MultiplicativeExpression);
        addRule(AdditiveExpression, AdditiveExpression, Tokens.Plus, MultiplicativeExpression);
        addRule(AdditiveExpression, AdditiveExpression, Tokens.Minus, MultiplicativeExpression);
        addRule(ShiftExpression, AdditiveExpression);
        addRule(ShiftExpression, ShiftExpression, Tokens.Left_op, AdditiveExpression);
        addRule(ShiftExpression, ShiftExpression, Tokens.Right_op, AdditiveExpression);
        addRule(RelationalExpression, ShiftExpression);
        addRule(RelationalExpression, RelationalExpression, Tokens.Lt, ShiftExpression);
        addRule(RelationalExpression, RelationalExpression, Tokens.Gt, ShiftExpression);
        addRule(RelationalExpression, RelationalExpression, Tokens.Le_op, ShiftExpression);
        addRule(RelationalExpression, RelationalExpression, Tokens.Ge_op, ShiftExpression);
        addRule(EqualityExpression, RelationalExpression);
        addRule(EqualityExpression, EqualityExpression, Tokens.Eq_op, RelationalExpression);
        addRule(EqualityExpression, EqualityExpression, Tokens.Ne_op, RelationalExpression);
        addRule(AndExpression, EqualityExpression);
        addRule(AndExpression, AndExpression, Tokens.And, EqualityExpression);
        addRule(ExclusiveOrExpression, AndExpression);
        addRule(ExclusiveOrExpression, ExclusiveOrExpression, Tokens.Pow, AndExpression);
        addRule(InclusiveOrExpression, ExclusiveOrExpression);
        addRule(InclusiveOrExpression, InclusiveOrExpression, Tokens.Pipe, ExclusiveOrExpression);
        addRule(LogicalAndExpression, InclusiveOrExpression);
        addRule(LogicalAndExpression, LogicalAndExpression, Tokens.And_op, InclusiveOrExpression);
        addRule(LogicalOrExpression, LogicalAndExpression);
        addRule(LogicalOrExpression, LogicalOrExpression, Tokens.Or_op, LogicalAndExpression);
        addRule(ConditionalExpression, LogicalOrExpression);
        addRule(ConditionalExpression, LogicalOrExpression, Tokens.QuestionMark, Expression, Tokens.DualPoint, ConditionalExpression);
        addRule(AssignmentExpression, ConditionalExpression);
        addRule(AssignmentExpression, UnaryExpression, AssignmentOperator, AssignmentExpression);
        addRule(AssignmentOperator, Tokens.Eq);
        addRule(AssignmentOperator, Tokens.Mul_assign);
        addRule(AssignmentOperator, Tokens.Div_assign);
        addRule(AssignmentOperator, Tokens.Mod_assign);
        addRule(AssignmentOperator, Tokens.Add_assign);
        addRule(AssignmentOperator, Tokens.Sub_assign);
        addRule(AssignmentOperator, Tokens.ShiftLeft_assign);
        addRule(AssignmentOperator, Tokens.ShiftRight_assign);
        addRule(AssignmentOperator, Tokens.And_assign);
        addRule(AssignmentOperator, Tokens.Xor_assign);
        addRule(AssignmentOperator, Tokens.Or_assign);
        addRule(Expression, AssignmentExpression);
        addRule(Expression, Expression, Tokens.Comma, AssignmentExpression);
        addRule(ConstantExpression, ConditionalExpression);

        addRule(Declaration, DeclarationSpecifiers, Tokens.DotComma);

        //This hacky action for handling the 'typedef-name' issue in C grammar (more comments on this in the LexerHack class)
        addRule(Declaration, DeclarationSpecifiers, InitDeclaratorList, Tokens.DotComma).withAction(parsingContext -> {

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

        });

        addRule(DeclarationSpecifiers, StorageClassSpecifier);
        addRule(DeclarationSpecifiers, StorageClassSpecifier, DeclarationSpecifiers);

        addRule(DeclarationSpecifiers, TypeSpecifier);
        addRule(DeclarationSpecifiers, TypeSpecifier, DeclarationSpecifiers);

        addRule(DeclarationSpecifiers, TypeQualifier);
        addRule(DeclarationSpecifiers, TypeQualifier, DeclarationSpecifiers);

        addRule(InitDeclaratorList, InitDeclarator);
        addRule(InitDeclaratorList, InitDeclaratorList, Tokens.Comma, InitDeclarator);

        addRule(InitDeclarator, Declarator);
        addRule(InitDeclarator, Declarator, Tokens.Eq, Initializer);

        addRule(StorageClassSpecifier, Tokens.Typedef);
        addRule(StorageClassSpecifier, Tokens.Extern);
        addRule(StorageClassSpecifier, Tokens.Static);
        addRule(StorageClassSpecifier, Tokens.Auto);
        addRule(StorageClassSpecifier, Tokens.Register);

        Lexeme[] typeNames = new Lexeme[]{
                Tokens.Void,
                Tokens.Char,
                Tokens.Short,
                Tokens.Int,
                Tokens.Long,
                Tokens.Float,
                Tokens.Double,
                Tokens.Signed,
                Tokens.Unsigned,
                Tokens.TypeName // token type introduced by 'LexerHack'
        };

        addRule(TypeSpecifier, oneOf(typeNames));
        addRule(TypeSpecifier, StructOrUnionSpecifier);
        addRule(TypeSpecifier, EnumSpecifier);

        addRule(StructOrUnionSpecifier, StructOrUnion, Tokens.Identifier, Tokens.LeftCurlyBrace, StructDeclarationList, Tokens.RightCurlyBrace);
        addRule(StructOrUnionSpecifier, StructOrUnion, Tokens.LeftCurlyBrace, StructDeclarationList, Tokens.RightCurlyBrace);
        addRule(StructOrUnionSpecifier, StructOrUnion, Tokens.Identifier);
        addRule(StructOrUnion, Tokens.Struct);
        addRule(StructOrUnion, Tokens.Union);
        addRule(StructDeclarationList, StructDeclaration);
        addRule(StructDeclarationList, StructDeclarationList, StructDeclaration);
        addRule(StructDeclaration, SpecifierQualifierList, StructDeclaratorList, Tokens.DotComma);
        addRule(SpecifierQualifierList, TypeSpecifier, SpecifierQualifierList);
        addRule(SpecifierQualifierList, TypeSpecifier);
        addRule(SpecifierQualifierList, TypeQualifier, SpecifierQualifierList);
        addRule(SpecifierQualifierList, TypeQualifier);
        addRule(StructDeclaratorList, StructDeclarator);
        addRule(StructDeclaratorList, StructDeclaratorList, Tokens.Comma, StructDeclarator);
        addRule(StructDeclarator, Declarator);
        addRule(StructDeclarator, Tokens.DualPoint, ConstantExpression);
        addRule(StructDeclarator, Declarator, Tokens.DualPoint, ConstantExpression);
        addRule(EnumSpecifier, Tokens.Enum, Tokens.LeftCurlyBrace, EnumeratorList, Tokens.RightCurlyBrace);
        addRule(EnumSpecifier, Tokens.Enum, Tokens.Identifier, Tokens.LeftCurlyBrace, EnumeratorList, Tokens.RightCurlyBrace);
        addRule(EnumSpecifier, Tokens.Enum, Tokens.Identifier);
        addRule(EnumeratorList, Enumerator);
        addRule(EnumeratorList, EnumeratorList, Tokens.Comma, Enumerator);
        addRule(Enumerator, Tokens.Identifier);
        addRule(Enumerator, Tokens.Identifier, Tokens.Eq, ConstantExpression);
        addRule(TypeQualifier, Tokens.Const);
        addRule(TypeQualifier, Tokens.Volatile);
        addRule(Declarator, Pointer, DirectDeclarator);
        addRule(Declarator, DirectDeclarator);
        addRule(DirectDeclarator, Tokens.Identifier);
        addRule(DirectDeclarator, Tokens.LeftBrace, Declarator, Tokens.RightBrace);
        addRule(DirectDeclarator, DirectDeclarator, Tokens.LeftSquareBrace, ConstantExpression, Tokens.RightSquareBrace);
        addRule(DirectDeclarator, DirectDeclarator, Tokens.LeftSquareBrace, Tokens.RightSquareBrace);
        addRule(DirectDeclarator, DirectDeclarator, Tokens.LeftBrace, ParameterTypeList, Tokens.RightBrace);
        addRule(DirectDeclarator, DirectDeclarator, Tokens.LeftBrace, IdentifierList, Tokens.RightBrace);
        addRule(DirectDeclarator, DirectDeclarator, Tokens.LeftBrace, Tokens.RightBrace);
        addRule(Pointer, Tokens.Mul);
        addRule(Pointer, Tokens.Mul, TypeQualifierList);
        addRule(Pointer, Tokens.Mul, Pointer);
        addRule(Pointer, Tokens.Mul, TypeQualifierList, Pointer);
        addRule(TypeQualifierList, TypeQualifier);
        addRule(TypeQualifierList, TypeQualifierList, TypeQualifier);
        addRule(ParameterTypeList, ParameterList);
        addRule(ParameterTypeList, ParameterList, Tokens.Comma, Tokens.Ellipsis);
        addRule(ParameterList, ParameterDeclaration);
        addRule(ParameterList, ParameterList, Tokens.Comma, ParameterDeclaration);
        addRule(ParameterDeclaration, DeclarationSpecifiers, Declarator);
        addRule(ParameterDeclaration, DeclarationSpecifiers, AbstractDeclarator);
        addRule(ParameterDeclaration, DeclarationSpecifiers);
        addRule(IdentifierList, Tokens.Identifier);
        addRule(IdentifierList, IdentifierList, Tokens.Comma, Tokens.Identifier);
        addRule(TypeName, SpecifierQualifierList);
        addRule(TypeName, SpecifierQualifierList, AbstractDeclarator);
        addRule(AbstractDeclarator, Pointer);
        addRule(AbstractDeclarator, DirectAbstractDeclarator);
        addRule(AbstractDeclarator, Pointer, DirectAbstractDeclarator);
        addRule(DirectAbstractDeclarator, Tokens.LeftBrace, AbstractDeclarator, Tokens.RightBrace);
        addRule(DirectAbstractDeclarator, Tokens.LeftSquareBrace, Tokens.RightSquareBrace);
        addRule(DirectAbstractDeclarator, Tokens.LeftSquareBrace, ConstantExpression, Tokens.RightSquareBrace);
        addRule(DirectAbstractDeclarator, DirectAbstractDeclarator, Tokens.LeftSquareBrace, Tokens.RightSquareBrace);
        addRule(DirectAbstractDeclarator, DirectAbstractDeclarator, Tokens.LeftSquareBrace, ConstantExpression, Tokens.RightSquareBrace);
        addRule(DirectAbstractDeclarator, Tokens.LeftBrace, Tokens.RightBrace);
        addRule(DirectAbstractDeclarator, Tokens.LeftBrace, ParameterTypeList, Tokens.RightBrace);
        addRule(DirectAbstractDeclarator, DirectAbstractDeclarator, Tokens.LeftBrace, Tokens.RightBrace);
        addRule(DirectAbstractDeclarator, DirectAbstractDeclarator, Tokens.LeftBrace, ParameterTypeList, Tokens.RightBrace);
        addRule(Initializer, AssignmentExpression);
        addRule(Initializer, Tokens.LeftCurlyBrace, InitializerList, Tokens.RightCurlyBrace);
        addRule(Initializer, Tokens.LeftCurlyBrace, InitializerList, Tokens.Comma, Tokens.RightCurlyBrace);
        addRule(InitializerList, Initializer);
        addRule(InitializerList, InitializerList, Tokens.Comma, Initializer);
        addRule(Statement, LabeledStatement);
        addRule(Statement, CompoundStatement);
        addRule(Statement, ExpressionStatement);
        addRule(Statement, SelectionStatement);
        addRule(Statement, IterationStatement);
        addRule(Statement, JumpStatement);
        addRule(LabeledStatement, Tokens.Identifier, Tokens.DualPoint, Statement);
        addRule(LabeledStatement, Tokens.Case, ConstantExpression, Tokens.DualPoint, Statement);
        addRule(LabeledStatement, Tokens.Default, Tokens.DualPoint, Statement);
        addRule(CompoundStatement, Tokens.LeftCurlyBrace, Tokens.RightCurlyBrace);
        addRule(CompoundStatement, Tokens.LeftCurlyBrace, StatementList, Tokens.RightCurlyBrace);
        addRule(CompoundStatement, Tokens.LeftCurlyBrace, DeclarationList, Tokens.RightCurlyBrace);
        addRule(CompoundStatement, Tokens.LeftCurlyBrace, DeclarationList, StatementList, Tokens.RightCurlyBrace);
        addRule(DeclarationList, Declaration);
        addRule(DeclarationList, DeclarationList, Declaration);
        addRule(StatementList, Statement);
        addRule(StatementList, StatementList, Statement);
        addRule(ExpressionStatement, Tokens.DotComma);
        addRule(ExpressionStatement, Expression, Tokens.DotComma);
        addRule(SelectionStatement, Tokens.If, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement);
        addRule(SelectionStatement, Tokens.If, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement, Tokens.Else, Statement);
        addRule(SelectionStatement, Tokens.Switch, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement);
        addRule(IterationStatement, Tokens.While, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement);
        addRule(IterationStatement, Tokens.Do, Statement, Tokens.While, Tokens.LeftBrace, Expression, Tokens.RightBrace, Tokens.DotComma);
        addRule(IterationStatement, Tokens.For, Tokens.LeftBrace, ForCondition, Tokens.RightBrace, Statement);

        addRule(ForCondition, ForDeclaration, Tokens.DotComma, ForExpression, Tokens.DotComma, ForExpression);
        addRule(ForCondition, Expression, Tokens.DotComma, ForExpression, Tokens.DotComma, ForExpression);
        addRule(ForExpression, list(true, Tokens.Comma, AssignmentExpression));

        addRule(ForDeclaration, DeclarationSpecifiers, InitDeclaratorList);
        addRule(ForDeclaration, DeclarationSpecifiers);

        addRule(JumpStatement, Tokens.Goto, Tokens.Identifier, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Continue, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Break, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Return, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Return, Expression, Tokens.DotComma);
        addRule(TranslationUnit, ExternalDeclaration);
        addRule(TranslationUnit, TranslationUnit, ExternalDeclaration);

        addRule(ExternalDeclaration, FunctionDefinition);
        addRule(ExternalDeclaration, Declaration);
        addRule(FunctionDefinition, DeclarationSpecifiers, Declarator, DeclarationList, CompoundStatement);
        addRule(FunctionDefinition, DeclarationSpecifiers, Declarator, CompoundStatement);
        addRule(FunctionDefinition, Declarator, DeclarationList, CompoundStatement);
        addRule(FunctionDefinition, Declarator, CompoundStatement);

        lexer = Lexer.forLexemes(getTerminals());
        lexer.setFilteredOut(Lexemes.multilineComment("/*", "*/"));
        lexer.setFilteredOut(Lexemes.lineComment("//"));
        lexer.setFilteredOut(Lexemes.whitespace());
        lexer.setFilteredOut(Lexemes.newLine());
        lexer.setTokenListener(new LexerHack());

    }

    protected String getDeclaratorName(AstNode declarator) {
        AstNode directDeclarator = declarator.getChildOfType(DirectDeclarator);
        if(directDeclarator == null) {
            AstNode childDeclarator = declarator.getChildOfType(Declarator);
            if(childDeclarator != null) {
                return getDeclaratorName(childDeclarator);
            } else {
                throw new UnsupportedOperationException(declarator.repr());
            }
        }
        Token t = directDeclarator.asToken();
        if (t != null) {
            return t.getText();
        } else {
            return getDeclaratorName(directDeclarator);
        }
    }

    @Override
    public Parser createParser(Symbol targetSymbol, boolean useCache) {
        LRParser parser = (LRParser) super.createParser(targetSymbol, useCache);
        parser.setLexer(lexer);
        return parser;
    }

    /**
     * <a href="http://calculist.blogspot.fr/2009/02/c-typedef-parsing-problem.html">http://calculist.blogspot.fr/2009/02/c-typedef-parsing-problem.html</a>
     * <p>
     * The well-known "typedef problem" with parsing C is that the standard C grammar is ambiguous unless
     * the lexer distinguishes identifiers bound by typedef and other identifiers as two separate lexical classes.
     * </p>
     * <p>
     * This 'hack' handle a list of names parsed by the typedef statements, and changes the type of incoming lexer tokens
     * each time it matches one of these names
     * </p>
     */
    private static class LexerHack implements TokenListener {

        private Set<String> typeNames = new TreeSet<>();

        /**
         * New type names are added when we parse 'typedef ...;' rules.
         *
         * @param typeName
         */
        public void addTypeName(String typeName) {
            getLog().debug("addTypeName " + typeName);
            typeNames.add(typeName);
        }

        /**
         * Change the token's type ({@link Token#getTokenType()}) to {@link Tokens#TypeName} when its text matches a known name;
         *
         * @param token incoming token
         * @return possibly modified token
         */
        @Override
        public Token onNewToken(Token token) {
            if (token.getTokenType() == Tokens.Identifier) {
                String text = token.getText();
                if (typeNames.contains(text)) {
                    token = new Token(Tokens.TypeName, token.getPosition(), text);
                }
            }
            return token;
        }
    }

}
