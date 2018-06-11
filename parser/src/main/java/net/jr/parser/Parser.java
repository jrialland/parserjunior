package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.parser.ast.AstNode;
import net.jr.parser.ast.AstNodeFactory;

import java.io.Reader;
import java.io.StringReader;

/**
 * a {@link Parser} can read an input text and build an abstract syntax tree from it.
 * Concrete parsers can be instanciated using {@link Grammar#createParser()}
 */
public interface Parser {

    /**
     * @return The default lexer, i.e the one that is used by default when calling Parser{@link #parse(Reader)}
     */
    Lexer getLexer();

    void setLexer(Lexer lexer);

    ParserListener getParserListener();

    /**
     * Sets a {@link ParserListener} that will be notified on different phases
     *
     * @param parserListener
     */
    void setParserListener(ParserListener parserListener);

    AstNodeFactory getAstNodeFactory();

    /**
     * The instanciation of the nodes may be delegated to a custom {@link AstNodeFactory}
     */
    void setAstNodeFactory(AstNodeFactory astNodeFactory);

    /**
     * sugar for parse(getLexer(), new StringReader(txt))
     *
     * @param txt
     * @return
     */
    default AstNode parse(String txt) {
        return parse(new StringReader(txt));
    }

    /**
     * sugar for parse(getLexer(), reader))
     *
     * @param reader
     * @return
     */
    AstNode parse(Reader reader);
}
