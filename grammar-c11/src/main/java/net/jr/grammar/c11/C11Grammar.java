package net.jr.grammar.c11;

import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.parser.Forward;
import net.jr.parser.Grammar;
import net.jr.parser.Rule;

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
        Lexeme LeftSquareBrace = Lexemes.singleChar('[');
        Lexeme RightSquareBrace = Lexemes.singleChar(']');
        Lexeme Eq = Lexemes.singleChar('=');
        Lexeme MulEq = Lexemes.literal("*=");
        Lexeme DivEq = Lexemes.literal("/=");
        Lexeme ModEq = Lexemes.literal("%=");
        Lexeme PlusEq = Lexemes.literal("+=");
        Lexeme MinusEq = Lexemes.literal("-=");
        Lexeme ShiftLeftEq = Lexemes.literal("<<=");
        Lexeme ShiftRightEq = Lexemes.literal(">>=");
        Lexeme AndEq = Lexemes.literal("&=");
        Lexeme PowEq = Lexemes.literal("^=");
        Lexeme PipeEq = Lexemes.literal("|=");
        Lexeme DotComma = Lexemes.singleChar(';');
        Lexeme Typedef = Lexemes.literal("typedef");
        Lexeme Extern = Lexemes.literal("extern");
        Lexeme Static = Lexemes.literal("static");
        Lexeme _Thread_local = Lexemes.literal("_Thread_local");
        Lexeme Auto = Lexemes.literal("auto");
        Lexeme Register = Lexemes.literal("register");
        Lexeme __extension__ = Lexemes.literal("__extension__");
        Lexeme Void = Lexemes.literal("void");
        Lexeme Char = Lexemes.literal("char");
        Lexeme Short = Lexemes.literal("short");
        Lexeme Int = Lexemes.literal("int");
        Lexeme Long = Lexemes.literal("long");
        Lexeme Float = Lexemes.literal("float");
        Lexeme Double = Lexemes.literal("double");
        Lexeme Signed = Lexemes.literal("signed");
        Lexeme Unsigned = Lexemes.literal("unsigned");
        Lexeme _Bool = Lexemes.literal("_Bool");
        Lexeme _Complex = Lexemes.literal("_Complex");
        Lexeme __m128 = Lexemes.literal("__m128");
        Lexeme __m128d = Lexemes.literal("__m128d");
        Lexeme __m128i = Lexemes.literal("__m128i");
        Lexeme __typeof__ = Lexemes.literal("__typeof__");
        Lexeme Struct = Lexemes.literal("struct");
        Lexeme Union = Lexemes.literal("union");
        Lexeme Enum = Lexemes.literal("enum");
        Lexeme LeftCurlyBrace = Lexemes.singleChar('{');
        Lexeme RightCurlyBrace = Lexemes.singleChar('}');
        Lexeme _Atomic = Lexemes.singleChar('}');
        Lexeme Const = Lexemes.literal("const");
        Lexeme Restrict = Lexemes.literal("restrict");
        Lexeme Volatile = Lexemes.literal("volatile");
        Lexeme __volatile__ = Lexemes.literal("__volatile__");
        Lexeme _NoReturn = Lexemes.literal("_NoReturn");
        Lexeme __inline__ = Lexemes.literal("__inline__");
        Lexeme __stdcall = Lexemes.literal("__stdcall");
        Lexeme __declspec = Lexemes.literal("__declspec");
        Lexeme Inline = Lexemes.literal("inline");
        Lexeme _Alignas = Lexemes.literal("_Alignas");
        Lexeme DigitSequence = Lexemes.number();
        Lexeme __asm = Lexemes.literal("__asm");
        Lexeme __asm__ = Lexemes.literal("__asm__");
        Lexeme __attribute__ = Lexemes.literal("__attribute__");
        Lexeme ThreePoints = Lexemes.literal("...");
        Lexeme Dot = Lexemes.singleChar('.');
        Lexeme Arrow = Lexemes.literal("->");
        Lexeme _Static_assert = Lexemes.literal("_Static_assert");
        Lexeme Case = Lexemes.literal("case");
        Lexeme Default = Lexemes.literal("default");
        Lexeme While = Lexemes.literal("while");
        Lexeme Do = Lexemes.literal("do");
        Lexeme For = Lexemes.literal("for");
        Lexeme Switch = Lexemes.literal("switch");
        Lexeme If = Lexemes.literal("if");
        Lexeme Else = Lexemes.literal("else");
        Lexeme Goto = Lexemes.literal("goto");
        Lexeme Continue = Lexemes.literal("continue");
        Lexeme Break = Lexemes.literal("break");
        Lexeme Return = Lexemes.literal("return");
        Lexeme __builtin_va_arg = Lexemes.literal("__builtin_va_arg");
        Lexeme __builtin_offsetof = Lexemes.literal("__builtin_offsetof");
    }

    public static Forward PrimaryExpression = new Forward("PrimaryExpression");
    public static Forward Expression = new Forward("Expression");
    public static Forward GenericSelection = new Forward("GenericSelection");
    public static Forward GenericAssocList = new Forward("GenericAssocList");
    public static Forward UnaryExpression = new Forward("UnaryExpression");
    public static Forward PostfixExpression = new Forward("PostfixExpression");
    public static Forward Typename = new Forward("Typename");
    public static Forward UnaryOperator = new Forward("UnaryOperator");
    public static Forward CastExpression = new Forward("CastExpression");
    public static Forward MultiplicativeExpression = new Forward("MultiplicativeExpression");
    public static Forward AdditiveExpression = new Forward("AdditiveExpression");
    public static Forward ShiftExpression = new Forward("ShiftExpression");
    public static Forward RelationalExpression = new Forward("RelationalExpression");
    public static Forward EqualityExpression = new Forward("EqualityExpression");
    public static Forward AndExpression = new Forward("AndExpression");
    public static Forward ExclusiveOrExpression = new Forward("ExclusiveOrExpression");
    public static Forward InclusiveOrExpression = new Forward("InclusiveOrExpression");
    public static Forward LogicalAndExpression = new Forward("LogicalAndExpression");
    public static Forward LogicalOrExpression = new Forward("LogicalOrExpression");
    public static Forward ConditionalExpression = new Forward("ConditionalExpression");
    public static Forward ConstantExpression = new Forward("ConstantExpression");
    public static Forward Declaration = new Forward("Declaration");
    public static Forward InitDeclarator = new Forward("InitDeclarator");
    public static Forward StaticAssertDeclaration = new Forward("StaticAssertDeclaration");
    public static Forward DeclarationSpecifier2 = new Forward("DeclarationSpecifier2");
    public static Forward DeclarationSpecifier = new Forward("DeclarationSpecifier");
    public static Forward TypeSpecifier = new Forward("TypeSpecifier");
    public static Forward TypeQualifier = new Forward("TypeQualifier");
    public static Forward FunctionSpecifier = new Forward("FunctionSpecifier");
    public static Forward AlignmentSpecifier = new Forward("AlignmentSpecifier");
    public static Forward Declarator = new Forward("Declarator");
    public static Forward Initializer = new Forward("Initializer");
    public static Forward StorageClassSpecifier = new Forward("StorageClassSpecifier");
    public static Forward SimpleTypeSpecifier = new Forward("SimpleTypeSpecifier");
    public static Forward AtomicTypeSpecifier = new Forward("AtomicTypeSpecifier");
    public static Forward StructOrUnionTypeSpecifier = new Forward("StructOrUnionTypeSpecifier");
    public static Forward EnumSpecifier = new Forward("EnumSpecifier");
    public static Forward TypedefName = new Forward("TypedefName");
    public static Forward TypeOf = new Forward("TypeOf");
    public static Forward StructDeclaration = new Forward("StructDeclaration");
    public static Forward SpecifierQualifierList = new Forward("SpecifierQualifierList");
    public static Forward StructDeclaratorList = new Forward("StructDeclaratorList");
    public static Forward StructDeclarator = new Forward("StructDeclarator");
    public static Forward Enumerator = new Forward("Enumerator");
    public static Forward GccAttributeSpecifier = new Forward("GccAttributeSpecifier");
    public static Forward Pointer = new Forward("Pointer");
    public static Forward DirectDeclarator = new Forward("DirectDeclarator");
    public static Forward GccDeclaratorExtension = new Forward("GccDeclaratorExtension");
    public static Forward ParameterTypeList = new Forward("ParameterTypeList");
    public static Forward IdentifierList = new Forward("IdentifierList");
    public static Forward GccAttributeList = new Forward("GccAttributeList");
    public static Forward GccAttribute = new Forward("GccAttribute");
    public static Forward ParameterList = new Forward("ParameterList");
    public static Forward DirectAbstractDeclarator = new Forward("DirectAbstractDeclarator");
    public static Forward AbstractDeclarator = new Forward("AbstractDeclarator");
    public static Forward ParameterDeclaration = new Forward("ParameterDeclaration");
    public static Forward AssignmentExpression = new Forward("AssignmentExpression");
    public static Forward InitializerList = new Forward("InitializerList");
    public static Forward InitializerListElement = new Forward("InitializerListElement");
    public static Forward Designator = new Forward("Designator");
    public static Forward DesignatorList = new Forward("DesignatorList");
    public static Forward Designation = new Forward("Designation");
    public static Forward Statement = new Forward("Statement");
    public static Forward CompoundStatement = new Forward("CompoundStatement");
    public static Forward LabeledStatement = new Forward("LabeledStatement");
    public static Forward ExpressionStatement = new Forward("ExpressionStatement");
    public static Forward SelectionStatement = new Forward("SelectionStatement");
    public static Forward IterationStatement = new Forward("IterationStatement");
    public static Forward JumpStatement = new Forward("JumpStatement");
    public static Forward IfElseStatement = new Forward("IfElseStatement");
    public static Forward SwitchStatement = new Forward("SwitchStatement");
    public static Forward WhileStatement = new Forward("WhileStatement");
    public static Forward DoWhileStatement = new Forward("DoWhileStatement");
    public static Forward ForStatement = new Forward("ForStatement");
    public static Forward ForCondition = new Forward("ForCondition");
    public static Forward ForDeclaration = new Forward("ForDeclaration");
    public static Forward ForExpression = new Forward("ForExpression");
    public static Forward GotoStatement = new Forward("GotoStatement");
    public static Forward ContinueStatement = new Forward("ContinueStatement");
    public static Forward BreakStatement = new Forward("BreakStatement");
    public static Forward ReturnStatement = new Forward("ReturnStatement");
    public static Forward FunctionDefinition = new Forward("FunctionDefinition");
    public static Forward CompilationUnit = new Forward("CompilationUnit");
    public static Forward TranslationUnit = new Forward("TranslationUnit");
    public static Forward ExternalDeclaration = new Forward("ExternalDeclaration");
    public static Forward Constant = new Forward("Constant");
    public static Forward IntegerConstant = new Forward("IntegerConstant");
    public static Forward FloatingConstant = new Forward("FloatingConstant");
    public static Forward CharacterConstant = new Forward("CharacterConstant");
    public static Forward GenericAssociation = new Forward("GenericAssociation");

    C11Grammar() {

        setName("C11");

        target(PrimaryExpression)
                .def(Tokens.Identifier)
                .def(Constant)
                .def(oneOrMore(Tokens.StringLiteral))
                .def(Tokens.LeftBrace, Expression, Tokens.RightBrace)
                .def(GenericSelection)
                .def(optional(Tokens.__extension__), Tokens.LeftBrace, CompoundStatement, Tokens.RightBrace)
                .def(Tokens.__builtin_va_arg, Tokens.LeftBrace, UnaryExpression, Tokens.Comma, Typename, Tokens.RightBrace)
                .def(Tokens.__builtin_offsetof, Tokens.LeftBrace, Typename, Tokens.Comma, UnaryExpression, Tokens.RightBrace);

        target(Constant)
                .def(IntegerConstant)
                .def(FloatingConstant)
                .def(CharacterConstant);

        target(IntegerConstant)
                .def(Lexemes.cInteger())
                .def(Lexemes.cOctal())
                .def(Lexemes.cHexNumber())
                .def(Lexemes.cBinary());

        target(FloatingConstant)
                .def(Lexemes.cFloatingPoint());

        target(CharacterConstant)
                .def(Lexemes.cCharacter());

        target(GenericSelection)
                .def(Tokens._Generic, Tokens.LeftBrace, AssignmentExpression, Tokens.Comma, GenericAssocList, Tokens.RightBrace);

        target(GenericAssocList)
                .def(GenericAssociation)
                .def(list(Tokens.Comma, GenericAssociation));

        target(GenericAssociation)
                .def(Typename, Tokens.DualPoint, AssignmentExpression)
                .def(Tokens.Default, Tokens.DualPoint, AssignmentExpression);

        target(PostfixExpression)
                .def(PrimaryExpression)
                .def(PostfixExpression, Tokens.LeftSquareBrace, Expression, Tokens.RightSquareBrace)
                .def(PostfixExpression, Tokens.LeftBrace, list(Tokens.Comma, AssignmentExpression), Tokens.RightBrace)
                .def(PostfixExpression, Tokens.Dot, Tokens.Identifier)
                .def(PostfixExpression, Tokens.Arrow, Tokens.Identifier)
                .def(PostfixExpression, Tokens.PlusPlus)
                .def(PostfixExpression, Tokens.MinusMinus)
                .def(optional(Tokens.__extension__), Tokens.LeftBrace, Typename, Tokens.RightBrace, Tokens.LeftCurlyBrace, InitializerList, optional(Tokens.Comma), Tokens.RightCurlyBrace);

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
                .def(oneOf(Tokens.And, Tokens.Mult, Tokens.Plus, Tokens.Minus, Tokens.Tilde, Tokens.Exclamation));

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
                .def(UnaryExpression, oneOf(Tokens.Eq, Tokens.MinusEq, Tokens.PlusEq, Tokens.MulEq, Tokens.DivEq, Tokens.ModEq, Tokens.ShiftLeftEq, Tokens.ShiftRightEq, Tokens.AndEq, Tokens.PowEq, Tokens.PipeEq), AssignmentExpression);

        target(Expression)
                .def(AssignmentExpression)
                .def(Expression, Tokens.Comma, AssignmentExpression);

        target(ConstantExpression)
                .def(ConditionalExpression);


        target(Declaration)
                .def(oneOrMore(DeclarationSpecifier), optional(InitDeclarator), zeroOrMore(Tokens.Comma, InitDeclarator), Tokens.DotComma)
                .def(oneOrMore(DeclarationSpecifier))
                .def(StaticAssertDeclaration);


        target(DeclarationSpecifier2)
                .def(oneOrMore(DeclarationSpecifier));

        target(DeclarationSpecifier)
                .def(StorageClassSpecifier)
                .def(TypeSpecifier)
                .def(TypeQualifier)
                .def(FunctionSpecifier)
                .def(AlignmentSpecifier);

        target(InitDeclarator)
                .def(Declarator)
                .def(Declarator, Tokens.Eq, Initializer);

        target(StorageClassSpecifier)
                .def(Tokens.Typedef)
                .def(Tokens.Extern)
                .def(Tokens.Static)
                .def(Tokens._Thread_local)
                .def(Tokens.Auto)
                .def(Tokens.Register);

        target(TypeSpecifier)
                .def(SimpleTypeSpecifier)
                .def(AtomicTypeSpecifier)
                .def(StructOrUnionTypeSpecifier)
                .def(EnumSpecifier)
                .def(TypedefName)
                .def(TypeOf);

        target(SimpleTypeSpecifier)
                .def(oneOf(Tokens.Void, Tokens.Char, Tokens.Short, Tokens.Int, Tokens.Long, Tokens.Float, Tokens.Double, Tokens.Signed, Tokens.Unsigned, Tokens._Bool, Tokens._Complex, Tokens.__m128, Tokens.__m128i, Tokens.__m128d))
                .def(Tokens.__extension__, Tokens.LeftBrace, oneOf(Tokens.__m128, Tokens.__m128i, Tokens.__m128d), Tokens.RightBrace);

        target(TypeOf)
                .def(Tokens.__typeof__, Tokens.LeftBrace, ConstantExpression, Tokens.RightBrace);


        target(StructOrUnionTypeSpecifier)
                .def(oneOf(Tokens.Struct, Tokens.Union), optional(Tokens.Identifier), Tokens.LeftCurlyBrace, StructDeclaration, Tokens.RightCurlyBrace)
                .def(oneOf(Tokens.Struct, Tokens.Union), Tokens.Identifier);

        target(StructDeclaration)
                .def(SpecifierQualifierList, optional(StructDeclaratorList), Tokens.Comma)
                .def(StaticAssertDeclaration);

        target(SpecifierQualifierList)
                .def(TypeSpecifier, optional(SpecifierQualifierList))
                .def(TypeQualifier, optional(SpecifierQualifierList));

        target(StructDeclarator)
                .def(Declarator)
                .def(optional(Declarator), Tokens.DualPoint, ConstantExpression);

        target(EnumSpecifier)
                .def(Tokens.Enum, optional(Tokens.Identifier), optional(Tokens.LeftCurlyBrace, list(Tokens.Comma, Enumerator), optional(Tokens.Comma), Tokens.RightCurlyBrace));

        target(Enumerator)
                .def(Tokens.Identifier, optional(Tokens.Eq, ConstantExpression));

        target(AtomicTypeSpecifier)
                .def(Tokens._Atomic, Tokens.LeftBrace, Typename, Tokens.RightBrace);

        target(TypeQualifier)
                .def(oneOf(Tokens.Const, Tokens.Restrict, Tokens.Volatile, Tokens._Atomic));

        target(FunctionSpecifier)
                .def(oneOf(Tokens.Inline, Tokens._NoReturn, Tokens.__inline__, Tokens.__stdcall), GccAttributeSpecifier, Tokens.__declspec, Tokens.LeftBrace, Tokens.Identifier, Tokens.RightBrace);

        target(AlignmentSpecifier)
                .def(Tokens._Alignas, Tokens.LeftBrace, Typename, Tokens.RightBrace)
                .def(Tokens._Alignas, Tokens.LeftBrace, ConstantExpression, Tokens.RightBrace);

        target(Declarator)
                .def(optional(Pointer), DirectDeclarator, zeroOrMore(GccDeclaratorExtension));

        target(DirectDeclarator)
                .def(Tokens.Identifier)
                .def(Tokens.LeftBrace, Declarator, Tokens.RightBrace)
                .def(DirectDeclarator, Tokens.LeftSquareBrace, zeroOrMore(TypeQualifier), optional(AssignmentExpression), Tokens.RightSquareBrace)
                .def(DirectDeclarator, Tokens.LeftSquareBrace, Tokens.Static, zeroOrMore(TypeQualifier), AssignmentExpression, Tokens.RightSquareBrace)
                .def(DirectDeclarator, Tokens.LeftSquareBrace, oneOrMore(TypeQualifier), Tokens.Static, AssignmentExpression, Tokens.RightSquareBrace)
                .def(DirectDeclarator, Tokens.LeftSquareBrace, zeroOrMore(TypeQualifier), Tokens.Mult, Tokens.RightSquareBrace)
                .def(DirectDeclarator, Tokens.LeftBrace, ParameterTypeList, Tokens.RightBrace)
                .def(DirectDeclarator, Tokens.LeftBrace, optional(IdentifierList), Tokens.RightBrace)
                .def(Tokens.Identifier, Tokens.DualPoint, Tokens.DigitSequence);

        target(GccDeclaratorExtension)
                .def(Tokens.__asm, Tokens.LeftBrace, oneOrMore(Tokens.StringLiteral), Tokens.RightBrace)
                .def(GccAttributeSpecifier);

        target(GccAttributeSpecifier)
                .def(Tokens.__attribute__, Tokens.LeftBrace, Tokens.LeftBrace, GccAttributeList, Tokens.RightBrace, Tokens.RightBrace);

        target(GccAttributeList)
                .def(list(Tokens.Comma, GccAttribute));

        target(GccAttribute)
                .def(Tokens.LeftBrace, list(Tokens.Comma, AssignmentExpression), Tokens.RightBrace);

        target(Pointer)
                .def(oneOf(Tokens.Mult, Tokens.Pow), zeroOrMore(TypeQualifier), optional(Pointer));

        target(ParameterTypeList)
                .def(ParameterList)
                .def(ParameterList, Tokens.Comma, Tokens.ThreePoints);

        target(ParameterList)
                .def(ParameterDeclaration)
                .def(ParameterList, Tokens.Comma, ParameterDeclaration);

        target(ParameterDeclaration)
                .def(oneOrMore(DeclarationSpecifier), Declarator)
                .def(DeclarationSpecifier2, optional(AbstractDeclarator));

        target(IdentifierList)
                .def(list(Tokens.Comma, Tokens.Identifier));

        target(Typename)
                .def(SpecifierQualifierList, optional(AbstractDeclarator));

        target(AbstractDeclarator)
                .def(Pointer)
                .def(optional(Pointer), DirectAbstractDeclarator, GccDeclaratorExtension);

        target(DirectAbstractDeclarator)
                .def(Tokens.LeftBrace, AbstractDeclarator, Tokens.RightBrace, zeroOrMore(GccDeclaratorExtension))
                .def(Tokens.LeftSquareBrace, zeroOrMore(TypeQualifier), optional(AssignmentExpression), Tokens.RightSquareBrace)
                .def(Tokens.LeftSquareBrace, Tokens.Static, zeroOrMore(TypeQualifier), AssignmentExpression, Tokens.RightSquareBrace)
                .def(Tokens.LeftSquareBrace, oneOrMore(TypeQualifier), Tokens.Static, AssignmentExpression, Tokens.RightSquareBrace)
                .def(Tokens.LeftSquareBrace, Tokens.Mult, Tokens.RightSquareBrace)
                .def(Tokens.LeftBrace, optional(ParameterTypeList), Tokens.RightBrace, zeroOrMore(GccDeclaratorExtension))
                .def(DirectAbstractDeclarator, Tokens.LeftSquareBrace, zeroOrMore(TypeQualifier), Tokens.Static, AssignmentExpression, Tokens.RightSquareBrace)
                .def(DirectAbstractDeclarator, Tokens.LeftSquareBrace, Tokens.Static, zeroOrMore(TypeQualifier), AssignmentExpression, Tokens.RightSquareBrace)
                .def(DirectAbstractDeclarator, Tokens.LeftSquareBrace, Tokens.Mult, Tokens.RightSquareBrace)
                .def(DirectAbstractDeclarator, Tokens.LeftBrace, optional(ParameterTypeList), Tokens.RightBrace, zeroOrMore(GccDeclaratorExtension));

        target(TypedefName)
                .def(Tokens.Identifier);

        target(Initializer)
                .def(AssignmentExpression)
                .def(Tokens.LeftCurlyBrace, InitializerList, optional(Tokens.Comma), Tokens.RightCurlyBrace);

        target(InitializerList)
                .def(list(Tokens.Comma, InitializerListElement));

        target(InitializerListElement)
                .def(optional(Designation), Initializer);

        target(Designation)
                .def(DesignatorList, Tokens.Eq);

        target(DesignatorList)
                .def(Designator)
                .def(DesignatorList, Designator);

        target(Designator)
                .def(Tokens.LeftSquareBrace, ConstantExpression, Tokens.RightSquareBrace)
                .def(Tokens.Dot, Tokens.Identifier);

        target(StaticAssertDeclaration)
                .def(Tokens._Static_assert, Tokens.LeftBrace, ConstantExpression, Tokens.Comma, oneOrMore(Tokens.StringLiteral), Tokens.RightBrace, Tokens.DotComma);


        target(Statement)
                .def(LabeledStatement)
                .def(CompoundStatement)
                .def(ExpressionStatement)
                .def(SelectionStatement)
                .def(IterationStatement)
                .def(JumpStatement)
                .def(oneOf(Tokens.__asm, Tokens.__asm__), oneOf(Tokens.Volatile, Tokens.__volatile__), Tokens.LeftBrace, list(Tokens.DualPoint, list(Tokens.Comma, LogicalOrExpression)), Tokens.RightBrace, Tokens.DotComma);

        target(LabeledStatement)
                .def(Tokens.Identifier, Tokens.DualPoint)
                .def(Tokens.Case, ConstantExpression, Tokens.DualPoint)
                .def(Tokens.Default, Tokens.DualPoint);

        target(CompoundStatement)
                .def(Tokens.LeftCurlyBrace, zeroOrMore(oneOf(Declaration, Statement)), Tokens.RightCurlyBrace);

        target(ExpressionStatement)
                .def(optional(Expression), Tokens.DotComma);

        target(SelectionStatement)
                .def(IfElseStatement)
                .def(SwitchStatement);

        target(IfElseStatement)
                .def(Tokens.If, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement, optional(Tokens.Else, Statement));

        target(SwitchStatement)
                .def(Tokens.Switch, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement);

        target(IterationStatement)
                .def(WhileStatement)
                .def(DoWhileStatement)
                .def(ForStatement);

        target(WhileStatement)
                .def(Tokens.While, Tokens.LeftBrace, Expression, Tokens.RightBrace, Statement);

        target(DoWhileStatement)
                .def(Tokens.Do, Statement, Tokens.While, Tokens.LeftBrace, Expression, Tokens.RightBrace, Tokens.DotComma);

        target(ForStatement)
                .def(Tokens.For, Tokens.LeftBrace, ForCondition, Tokens.RightBrace, Statement);

        target(ForCondition)
                .def(ForDeclaration, Tokens.DotComma, optional(AssignmentExpression), Tokens.DotComma, optional(ForExpression))
                .def(optional(Expression), Tokens.DotComma, optional(AssignmentExpression), Tokens.DotComma, optional(ForExpression));

        target(ForDeclaration)
                .def(oneOrMore(DeclarationSpecifier), list(Tokens.Comma, InitDeclarator));

        target(ForExpression)
                .def(list(Tokens.Comma, AssignmentExpression));

        target(JumpStatement)
                .def(GotoStatement)
                .def(ContinueStatement)
                .def(BreakStatement)
                .def(ReturnStatement);

        target(GotoStatement)
                .def(Tokens.Goto, Tokens.Identifier, Tokens.DotComma)
                .def(Tokens.Goto, UnaryExpression, Tokens.DotComma);

        target(ContinueStatement)
                .def(Tokens.Continue, Tokens.DotComma);

        target(BreakStatement)
                .def(Tokens.Break, Tokens.DotComma);

        target(ReturnStatement)
                .def(Tokens.Return, optional(Expression), Tokens.DotComma);

        target(TranslationUnit)
                .def(ExternalDeclaration)
                .def(TranslationUnit, ExternalDeclaration);

        target(ExternalDeclaration)
                .def(FunctionDefinition)
                .def(Declaration)
                .def(Tokens.DotComma);

        target(FunctionDefinition)
                .def(zeroOrMore(DeclarationSpecifier), Declarator, zeroOrMore(Declaration), CompoundStatement);

        Rule targetRule = addRule(CompilationUnit, optional(TranslationUnit)).get();
        setTargetRule(targetRule);
        setTargetSymbol(CompilationUnit);
    }

    public Rule getCompilationUnitRule() {
        return getRulesTargeting(CompilationUnit).iterator().next();
    }

}
