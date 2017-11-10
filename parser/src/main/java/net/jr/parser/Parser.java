package net.jr.parser;

import net.jr.lexer.Lexer;
import net.jr.parser.ast.AstNode;

import java.io.Reader;
import java.io.StringReader;

/**
 * a {@link Parser} can read an input text and build an abstract syntax tree from it.
 * Concrete parsers can be instanciated using {@link Grammar#createParser()}
 */
public interface Parser {

    /**
     * @param lexer  a custom lexer that know how recognize words
     * @param reader
     * @return
     */
    AstNode parse(Lexer lexer, Reader reader);

    /**
     * @return The default lexer, i.e the one that is used by default when calling Parser{@link #parse(Reader)}
     */
    Lexer getDefaultLexer();

    /**
     * sugar for parse(getDefaultLexer(), new StringReader(txt))
     *
     * @param txt
     * @return
     */
    default AstNode parse(String txt) {
        return parse(getDefaultLexer(), new StringReader(txt));
    }

    /**
     * sugar for parse(getDefaultLexer(), reader))
     *
     * @param reader
     * @return
     */
    default AstNode parse(Reader reader) {
        return parse(getDefaultLexer(), reader);
    }
}
