package net.jr.parser.ast;

import net.jr.lexer.Token;
import net.jr.parser.Rule;

/**
 * a {@link net.jr.parser.Parser} may delegate the construction of new {@link AstNode}s to instances
 * of this interface.
 *
 * @see net.jr.parser.Parser#setAstNodeFactory(AstNodeFactory)
 */
public interface AstNodeFactory {

    AstNode newNonLeafNode(Rule rule);

    AstNode newLeafNode(Token token);

}
