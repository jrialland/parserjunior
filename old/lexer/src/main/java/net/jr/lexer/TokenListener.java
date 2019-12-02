package net.jr.lexer;

/**
 * {@link TokenListener} receives the newly detected tokens, and may modify or discard them before they are "officialy" emitted.
 * a {@link TokenListener} is assigned to a Lexer using the {@link Lexer#setTokenListener(TokenListener)} method
 */
public interface TokenListener {

    /**
     * handles new tokens, if this method returns null the token is discarded
     *
     * @param token recognized token
     * @return the filtered token
     */
    Token onNewToken(Token token);

}
