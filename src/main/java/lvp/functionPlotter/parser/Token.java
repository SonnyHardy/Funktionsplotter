package lvp.functionPlotter.parser;


/**
 * Represents a token in the tokenizer for mathematical expressions.
 * This record holds the type of the token and its value.
 */
public record Token(TokenType type, String value) {

}
