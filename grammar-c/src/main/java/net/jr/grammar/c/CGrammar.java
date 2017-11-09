package net.jr.grammar.c;

import net.jr.common.Symbol;
import net.jr.lexer.*;
import net.jr.lexer.impl.Word;
import net.jr.parser.*;
import net.jr.parser.ast.AstNode;

import java.util.Set;
import java.util.TreeSet;

public class CGrammar extends Grammar {

    public static final class Tokens {
        public static Lexeme Volatile = Lexemes.literal("volatile");
        public static Lexeme Minus = Lexemes.singleChar('-');
        public static Lexeme Ne_op = Lexemes.literal("!=");
        public static Lexeme Left_op = Lexemes.literal("<<");
        public static Lexeme RightBrace = Lexemes.singleChar(')');
        public static Lexeme Mod = Lexemes.singleChar('%');
        public static Lexeme Right_op = Lexemes.literal(">>");
        public static Lexeme Pipe = Lexemes.singleChar('|');
        public static Lexeme Do = Lexemes.literal("do");
        public static Lexeme ExclamationMark = Lexemes.singleChar('!');
        public static Lexeme Static = Lexemes.literal("static");
        public static Lexeme Gt = Lexemes.singleChar('>');
        public static Lexeme LeftSquareBrace = Lexemes.singleChar('[');
        public static Lexeme DualPoint = Lexemes.singleChar(':');
        public static Lexeme And_assign = Lexemes.literal("&=");
        public static Lexeme Const = Lexemes.literal("const");
        public static Lexeme Break = Lexemes.literal("break");
        public static Lexeme Or_assign = Lexemes.literal("|=");
        public static Lexeme Typedef = Lexemes.literal("typedef");
        public static Lexeme Else = Lexemes.literal("else");
        public static Lexeme Extern = Lexemes.literal("extern");
        public static Lexeme If = Lexemes.literal("if");
        public static Lexeme Dot = Lexemes.singleChar('.');
        public static Lexeme Register = Lexemes.literal("register");
        public static Lexeme Enum = Lexemes.literal("enum");
        public static Lexeme Right_assign = Lexemes.literal(">>=");
        public static Lexeme Mul = Lexemes.singleChar('*');
        public static Lexeme Eq_op = Lexemes.literal("==");
        public static Lexeme And = Lexemes.singleChar('&');
        public static Lexeme Le_op = Lexemes.literal("<=");
        public static Lexeme For = Lexemes.literal("for");
        public static Lexeme Dec_op = Lexemes.literal("--");
        public static Lexeme QuestionMark = Lexemes.singleChar('?');
        public static Lexeme Case = Lexemes.literal("case");
        public static Lexeme Auto = Lexemes.literal("auto");
        public static Lexeme RightCurlyBrace = Lexemes.singleChar('}');
        public static Lexeme DotComma = Lexemes.singleChar(';');
        public static Lexeme Ellipsis = Lexemes.literal("...");
        public static Lexeme Ptr_op = Lexemes.singleChar('*');
        public static Lexeme Switch = Lexemes.literal("switch");
        public static Lexeme Void = Lexemes.literal("void");
        public static Lexeme Struct = Lexemes.literal("struct");
        public static Lexeme Div = Lexemes.singleChar('/');
        public static Lexeme And_op = Lexemes.literal("&&");
        public static Lexeme Or_op = Lexemes.literal("||");
        public static Lexeme Float = Lexemes.literal("float");
        public static Lexeme Goto = Lexemes.literal("goto");
        public static Lexeme Plus = Lexemes.singleChar('+');
        public static Lexeme Div_assign = Lexemes.literal("/=");
        public static Lexeme Sub_assign = Lexemes.literal("-=");
        public static Lexeme Unsigned = Lexemes.literal("unsigned");
        public static Lexeme Sizeof = Lexemes.literal("sizeof");
        public static Lexeme Char = Lexemes.literal("char");
        public static Lexeme Int = Lexemes.literal("int");
        public static Lexeme Tilde = Lexemes.singleChar('~');
        public static Lexeme RightSquareBrace = Lexemes.singleChar(']');
        public static Lexeme Return = Lexemes.literal("return");
        public static Lexeme Lt = Lexemes.singleChar('<');
        public static Lexeme Signed = Lexemes.literal("signed");
        public static Lexeme Mul_assign = Lexemes.literal("*=");
        public static Lexeme Identifier = Lexemes.cIdentifier();
        public static Lexeme Add_assign = Lexemes.literal("+=");
        public static Lexeme Double = Lexemes.literal("double");
        public static Lexeme Long = Lexemes.literal("long");
        public static Lexeme Comma = Lexemes.singleChar(',');
        public static Lexeme Xor_assign = Lexemes.literal("^=");
        public static Lexeme LeftBrace = Lexemes.singleChar('(');
        public static Lexeme Ge_op = Lexemes.literal(">=");
        public static Lexeme Short = Lexemes.literal("short");
        public static Lexeme Pow = Lexemes.singleChar('^');
        public static Lexeme Continue = Lexemes.literal("continue");
        public static Lexeme Eq = Lexemes.singleChar('=');
        public static Lexeme LeftCurlyBrace = Lexemes.singleChar('{');
        public static Lexeme Mod_assign = Lexemes.literal("%=");
        public static Lexeme String_literal = Lexemes.cString();
        public static Lexeme Left_assign = Lexemes.literal("<<=");
        public static Lexeme While = Lexemes.literal("while");
        public static Lexeme Union = Lexemes.literal("union");
        public static Lexeme Inc_op = Lexemes.literal("++");
        public static Lexeme Default = Lexemes.literal("default");
        public static Lexeme TypeName = new Word("_" + Lexemes.Alpha, "_" + Lexemes.AlphaNum);
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

    CGrammar() {

        setName("C");

        addRule(Constant, oneOrMore(Tokens.String_literal));
        addRule(Constant, Lexemes.cInteger());
        addRule(Constant, Lexemes.cBinary());
        addRule(Constant, Lexemes.cOctal());
        addRule(Constant, Lexemes.cHexNumber());
        addRule(Constant, Lexemes.cFloatingPoint());

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
        addRule(AssignmentOperator, Tokens.Left_assign);
        addRule(AssignmentOperator, Tokens.Right_assign);
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
                for (AstNode initDeclarator : initDeclaratorList.find(InitDeclarator)) {
                    AstNode declarator = initDeclarator.getChildOfType(Declarator);

                    //find its name
                    String name = getDeclaratorName(declarator);

                    //update lexer context
                    Lexer lexer = parsingContext.getLexer();
                    LexerHack lh = (LexerHack) lexer.getTokenListener();
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

        Lexeme[] basicTypeNames = new Lexeme[]{
                Tokens.Void,
                Tokens.Char,
                Tokens.Short,
                Tokens.Int,
                Tokens.Long,
                Tokens.Float,
                Tokens.Double,
                Tokens.Signed,
                Tokens.Unsigned
        };

        addRule(TypeSpecifier, oneOf(basicTypeNames));
        addRule(TypeSpecifier, StructOrUnionSpecifier);
        addRule(TypeSpecifier, EnumSpecifier);

        //see comments on the LexerHack class for more infos on this rule
        addRule(TypeSpecifier, Tokens.TypeName);

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
        addRule(IterationStatement, Tokens.For, Tokens.LeftBrace, ExpressionStatement, ExpressionStatement, Tokens.RightBrace, Statement);
        addRule(IterationStatement, Tokens.For, Tokens.LeftBrace, ExpressionStatement, ExpressionStatement, Expression, Tokens.RightBrace, Statement);
        addRule(JumpStatement, Tokens.Goto, Tokens.Identifier, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Continue, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Break, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Return, Tokens.DotComma);
        addRule(JumpStatement, Tokens.Return, Expression, Tokens.DotComma);
        addRule(TranslationUnit, ExternalDeclaration);
        addRule(TranslationUnit, TranslationUnit, ExternalDeclaration);
        addRule(CompilationUnit, TranslationUnit);
        addRule(ExternalDeclaration, FunctionDefinition);
        addRule(ExternalDeclaration, Declaration);
        addRule(FunctionDefinition, DeclarationSpecifiers, Declarator, DeclarationList, CompoundStatement);
        addRule(FunctionDefinition, DeclarationSpecifiers, Declarator, CompoundStatement);
        addRule(FunctionDefinition, Declarator, DeclarationList, CompoundStatement);
        addRule(FunctionDefinition, Declarator, CompoundStatement);

        setTargetSymbol(CompilationUnit);
    }

    protected String getDeclaratorName(AstNode declarator) {
        Token t = declarator.getChildOfType(DirectDeclarator).asToken();
        if(t != null) {
            return t.getText();
        }
        throw new UnsupportedOperationException("");
    }

    @Override
    public Parser createParser(Symbol targetSymbol) {
        Parser parser = super.createParser(targetSymbol);
        Lexer lexer = parser.getDefaultLexer();
        lexer.filterOut(Lexemes.multilineComment("/*", "*/"));
        lexer.filterOut(Lexemes.lineComment("//"));
        lexer.filterOut(Lexemes.whitespace());
        lexer.filterOut(Lexemes.newLine());
        lexer.setTokenListener(new LexerHack());
        return parser;
    }

    /**
     * http://calculist.blogspot.fr/2009/02/c-typedef-parsing-problem.html
     * <p>
     * The well-known "typedef problem" with parsing C is that the standard C grammar is ambiguous unless
     * the lexer distinguishes identifiers bound by typedef and other identifiers as two separate lexical classes.
     */
    private static class LexerHack implements TokenListener {

        private Set<String> typeNames = new TreeSet<>();

        /**
         * New type names are added when we parse 'typedef ...;' rules.
         *
         * @param typeName
         */
        public void addTypeName(String typeName) {
            typeNames.add(typeName);
        }

        @Override
        public Token onNewToken(Token token) {
            String text = token.getText();
            //we have an identifier that matches a known type name
            if (text != null && typeNames.contains(text)) {
                //we return a modified token with the 'TypeName' type
                return new Token(Tokens.TypeName, token.getPosition(), text);
            } else {
                return token;
            }
        }

    }

    public Rule getStartRule() {
        return getRulesTargeting(getTargetSymbol()).iterator().next();
    }
}
