package net.jr.parser;

/**
 * Implementationns of this interface may be used to get notifications on what is being
 * done by a parser, see {@link Parser#setParserListener(ParserListener)}
 */
public interface ParserListener {

    void onParseError(ParseError parseError, ParsingContext parsingContext);

    void onReduce(Rule rule, ParsingContext parsingContext);

}
