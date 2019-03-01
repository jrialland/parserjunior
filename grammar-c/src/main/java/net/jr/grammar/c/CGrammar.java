package net.jr.grammar.c;

import net.jr.common.Symbol;
import net.jr.lexer.*;
import net.jr.parser.Grammar;
import net.jr.parser.NonTerminal;
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

    public static final NonTerminal CastExpression = new NonTerminal("CastExpression");
    public static final NonTerminal CompoundStatement = new NonTerminal("CompoundStatement");
    public static final NonTerminal TypeName = new NonTerminal("TypeName");
    public static final NonTerminal InitDeclarator = new NonTerminal("InitDeclarator");
    public static final NonTerminal PostfixExpression = new NonTerminal("PostfixExpression");
    public static final NonTerminal DirectDeclarator = new NonTerminal("DirectDeclarator");
    public static final NonTerminal UnaryExpression = new NonTerminal("UnaryExpression");
    public static final NonTerminal LogicalOrExpression = new NonTerminal("LogicalOrExpression");
    public static final NonTerminal StructDeclarationList = new NonTerminal("StructDeclarationList");
    public static final NonTerminal AndExpression = new NonTerminal("AndExpression");
    public static final NonTerminal IterationStatement = new NonTerminal("IterationStatement");
    public static final NonTerminal AdditiveExpression = new NonTerminal("AdditiveExpression");
    public static final NonTerminal AbstractDeclarator = new NonTerminal("AbstractDeclarator");
    public static final NonTerminal DirectAbstractDeclarator = new NonTerminal("DirectAbstractDeclarator");
    public static final NonTerminal AssignmentOperator = new NonTerminal("AssignmentOperator");
    public static final NonTerminal MultiplicativeExpression = new NonTerminal("MultiplicativeExpression");
    public static final NonTerminal EnumSpecifier = new NonTerminal("EnumSpecifier");
    public static final NonTerminal PrimaryExpression = new NonTerminal("PrimaryExpression");
    public static final NonTerminal Pointer = new NonTerminal("Pointer");
    public static final NonTerminal UnaryOperator = new NonTerminal("UnaryOperator");
    public static final NonTerminal DeclarationList = new NonTerminal("DeclarationList");
    public static final NonTerminal IdentifierList = new NonTerminal("IdentifierList");
    public static final NonTerminal EnumeratorList = new NonTerminal("EnumeratorList");
    public static final NonTerminal StructOrUnionSpecifier = new NonTerminal("StructOrUnionSpecifier");
    public static final NonTerminal ExclusiveOrExpression = new NonTerminal("ExclusiveOrExpression");
    public static final NonTerminal DeclarationSpecifiers = new NonTerminal("DeclarationSpecifiers");
    public static final NonTerminal TypeQualifierList = new NonTerminal("TypeQualifierList");
    public static final NonTerminal StructDeclarator = new NonTerminal("StructDeclarator");
    public static final NonTerminal StructOrUnion = new NonTerminal("StructOrUnion");
    public static final NonTerminal EqualityExpression = new NonTerminal("EqualityExpression");
    public static final NonTerminal InitializerList = new NonTerminal("InitializerList");
    public static final NonTerminal StructDeclaratorList = new NonTerminal("StructDeclaratorList");
    public static final NonTerminal ParameterTypeList = new NonTerminal("ParameterTypeList");
    public static final NonTerminal TranslationUnit = new NonTerminal("TranslationUnit");
    public static final NonTerminal InitDeclaratorList = new NonTerminal("InitDeclaratorList");
    public static final NonTerminal InclusiveOrExpression = new NonTerminal("InclusiveOrExpression");
    public static final NonTerminal ConditionalExpression = new NonTerminal("ConditionalExpression");
    public static final NonTerminal SelectionStatement = new NonTerminal("SelectionStatement");
    public static final NonTerminal ConstantExpression = new NonTerminal("ConstantExpression");
    public static final NonTerminal SpecifierQualifierList = new NonTerminal("SpecifierQualifierList");
    public static final NonTerminal Statement = new NonTerminal("Statement");
    public static final NonTerminal TypeQualifier = new NonTerminal("TypeQualifier");
    public static final NonTerminal ShiftExpression = new NonTerminal("ShiftExpression");
    public static final NonTerminal Enumerator = new NonTerminal("Enumerator");
    public static final NonTerminal LabeledStatement = new NonTerminal("LabeledStatement");
    public static final NonTerminal StatementList = new NonTerminal("StatementList");
    public static final NonTerminal ExternalDeclaration = new NonTerminal("ExternalDeclaration");
    public static final NonTerminal Expression = new NonTerminal("Expression");
    public static final NonTerminal TypeSpecifier = new NonTerminal("TypeSpecifier");
    public static final NonTerminal ExpressionStatement = new NonTerminal("ExpressionStatement");
    public static final NonTerminal ArgumentExpressionList = new NonTerminal("ArgumentExpressionList");
    public static final NonTerminal ParameterDeclaration = new NonTerminal("ParameterDeclaration");
    public static final NonTerminal AssignmentExpression = new NonTerminal("AssignmentExpression");
    public static final NonTerminal Declaration = new NonTerminal("Declaration");
    public static final NonTerminal Declarator = new NonTerminal("Declarator");
    public static final NonTerminal Initializer = new NonTerminal("Initializer");
    public static final NonTerminal StructDeclaration = new NonTerminal("StructDeclaration");
    public static final NonTerminal StorageClassSpecifier = new NonTerminal("StorageClassSpecifier");
    public static final NonTerminal LogicalAndExpression = new NonTerminal("LogicalAndExpression");
    public static final NonTerminal RelationalExpression = new NonTerminal("RelationalExpression");
    public static final NonTerminal ParameterList = new NonTerminal("ParameterList");
    public static final NonTerminal JumpStatement = new NonTerminal("JumpStatement");
    public static final NonTerminal FunctionDefinition = new NonTerminal("FunctionDefinition");
    public static final NonTerminal CompilationUnit = new NonTerminal("CompilationUnit");
    public static final NonTerminal Constant = new NonTerminal("Constant");
    public static final NonTerminal ForDeclaration = new NonTerminal("ForDeclaration");
    public static final NonTerminal ForCondition = new NonTerminal("ForCondition");
    public static final NonTerminal ForExpression = new NonTerminal("ForExpression");
    private static final Logger LOGGER = LoggerFactory.getLogger(CGrammar.class);
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

        Terminal[] typeNames = new Terminal[]{
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
        lexer.setFilteredOut(Lexemes.multilineComment("/*", "*/").withName("multilineComment"));
        lexer.setFilteredOut(Lexemes.lineComment("//").withName("lineComment"));
        lexer.setFilteredOut(Lexemes.whitespace());
        lexer.setFilteredOut(Lexemes.newLine());
        lexer.setTokenListener(new LexerHack());

    }

    private static Logger getLog() {
        return LOGGER;
    }

    private String getDeclaratorName(AstNode declarator) {
        AstNode directDeclarator = declarator.getChildOfType(DirectDeclarator);
        if (directDeclarator == null) {
            AstNode childDeclarator = declarator.getChildOfType(Declarator);
            if (childDeclarator != null) {
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

    public static final class Tokens {
        public static Terminal Volatile = Lexemes.literal("volatile", "volatile");
        public static Terminal Minus = Lexemes.singleChar('-', "minus");
        public static Terminal Ne_op = Lexemes.literal("!=", "notEq");
        public static Terminal Left_op = Lexemes.literal("<<", "shiftLeft");
        public static Terminal RightBrace = Lexemes.singleChar(')', "rightParen");
        public static Terminal Mod = Lexemes.singleChar('%', "modulo");
        public static Terminal Right_op = Lexemes.literal(">>", "shiftRight");
        public static Terminal Pipe = Lexemes.singleChar('|', "bitwiseOr");
        public static Terminal Do = Lexemes.literal("do", "do");
        public static Terminal ExclamationMark = Lexemes.singleChar('!', "exclamationMark");
        public static Terminal Static = Lexemes.literal("static", "static");
        public static Terminal Gt = Lexemes.singleChar('>', "gt");
        public static Terminal LeftSquareBrace = Lexemes.singleChar('[', "leftSquareBracket");
        public static Terminal DualPoint = Lexemes.singleChar(':', "twoPoints");
        public static Terminal And_assign = Lexemes.literal("&=", "andEq");
        public static Terminal Const = Lexemes.literal("const", "const");
        public static Terminal Break = Lexemes.literal("break", "break");
        public static Terminal Or_assign = Lexemes.literal("|=", "bitwiseOrEq");
        public static Terminal Typedef = Lexemes.literal("typedef", "typedef");
        public static Terminal Else = Lexemes.literal("else", "else");
        public static Terminal Extern = Lexemes.literal("extern", "extern");
        public static Terminal If = Lexemes.literal("if", "if");
        public static Terminal Dot = Lexemes.singleChar('.', "dot");
        public static Terminal Register = Lexemes.literal("register", "register");
        public static Terminal Enum = Lexemes.literal("enum", "enum");
        public static Terminal ShiftRight_assign = Lexemes.literal(">>=", "rightShiftEq");
        public static Terminal Mul = Lexemes.singleChar('*', "mult");
        public static Terminal Eq_op = Lexemes.literal("==", "eqEq");
        public static Terminal And = Lexemes.singleChar('&', "bitwiseAnd");
        public static Terminal Le_op = Lexemes.literal("<=", "lte");
        public static Terminal For = Lexemes.literal("for", "for");
        public static Terminal Dec_op = Lexemes.literal("--", "minusMinus");
        public static Terminal QuestionMark = Lexemes.singleChar('?', "questionMark");
        public static Terminal Case = Lexemes.literal("case", "case");
        public static Terminal Auto = Lexemes.literal("auto", "auto");
        public static Terminal RightCurlyBrace = Lexemes.singleChar('}', "rightCurlyBrace");
        public static Terminal DotComma = Lexemes.singleChar(';', "dotComma");
        public static Terminal Ellipsis = Lexemes.literal("...", "threePoints");
        public static Terminal Ptr_op = Lexemes.literal("->", "arrow");
        public static Terminal Switch = Lexemes.literal("switch", "switch");
        public static Terminal Void = Lexemes.literal("void", "void");
        public static Terminal Struct = Lexemes.literal("struct", "struct");
        public static Terminal Div = Lexemes.singleChar('/', "slash");
        public static Terminal And_op = Lexemes.literal("&&", "logicalAnd");
        public static Terminal Or_op = Lexemes.literal("||", "logicalOr");
        public static Terminal Float = Lexemes.literal("float", "float");
        public static Terminal Goto = Lexemes.literal("goto", "goto");
        public static Terminal Plus = Lexemes.singleChar('+', "plus");
        public static Terminal Div_assign = Lexemes.literal("/=", "divAssign");
        public static Terminal Sub_assign = Lexemes.literal("-=", "minusAssign");
        public static Terminal Unsigned = Lexemes.literal("unsigned", "unsigned");
        public static Terminal Sizeof = Lexemes.literal("sizeof", "sizeof");
        public static Terminal Char = Lexemes.literal("char", "char");
        public static Terminal Int = Lexemes.literal("int", "int");
        public static Terminal Tilde = Lexemes.singleChar('~', "tilde");
        public static Terminal RightSquareBrace = Lexemes.singleChar(']', "rightSquareBracket");
        public static Terminal Return = Lexemes.literal("return", "return");
        public static Terminal Lt = Lexemes.singleChar('<', "lt");
        public static Terminal Signed = Lexemes.literal("signed", "signed");
        public static Terminal Mul_assign = Lexemes.literal("*=", "mulAssign");
        public static Terminal Identifier = Lexemes.cIdentifier().withPriority(0);
        public static Terminal Add_assign = Lexemes.literal("+=", "plusAssign");
        public static Terminal Double = Lexemes.literal("double", "double");
        public static Terminal Long = Lexemes.literal("long", "long");
        public static Terminal Comma = Lexemes.singleChar(',', "comma");
        public static Terminal Xor_assign = Lexemes.literal("^=", "bitwiseNotAssign");
        public static Terminal LeftBrace = Lexemes.singleChar('(', "leftParen");
        public static Terminal Ge_op = Lexemes.literal(">=", "gte");
        public static Terminal Short = Lexemes.literal("short", "short");
        public static Terminal Pow = Lexemes.singleChar('^', "bitwiseNot");
        public static Terminal Continue = Lexemes.literal("continue", "continue");
        public static Terminal Eq = Lexemes.singleChar('=', "eq");
        public static Terminal LeftCurlyBrace = Lexemes.singleChar('{', "leftCurlyBrace");
        public static Terminal Mod_assign = Lexemes.literal("%=", "moduloEq");
        public static Terminal String_literal = Lexemes.cString();
        public static Terminal ShiftLeft_assign = Lexemes.literal("<<=", "leftShiftAssign");
        public static Terminal While = Lexemes.literal("while", "while");
        public static Terminal Union = Lexemes.literal("union", "union");
        public static Terminal Inc_op = Lexemes.literal("++", "plusPlus");
        public static Terminal Default = Lexemes.literal("default", "default");
        public static Terminal TypeName = Lexemes.artificial("typeName");
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
