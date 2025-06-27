package lvp.functionPlotter.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * Tokenizer class for breaking down a mathematical expression into tokens.
 * This class is responsible for parsing the input string and returning a list of tokens.
 */
public class Tokenizer {

    public static List<Token> tokenize(String input) {
        // This method should implement the logic to tokenize the input string
        List<Token> tokens = new ArrayList<>();

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue; // Skip whitespace
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    number.append(input.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, number.toString()));

            } else if (Character.isLetter(c)) {
                StringBuilder value = new StringBuilder();
                while (i < input.length() && Character.isLetterOrDigit(input.charAt(i))) {
                    value.append(input.charAt(i));
                    i++;
                }
                if (value.toString().equals("pi") || value.toString().equals("e")) {
                    // Handle special constants like pi and e
                    tokens.add(new Token(TokenType.NUMBER, value.toString()));
                } else if (value.toString().length() == 1) {
                    tokens.add(new Token(TokenType.VARIABLE, value.toString()));
                } else {
                    tokens.add(new Token(TokenType.FUNCTION, value.toString()));
                }

            } else {
                switch (c) {
                    case '+', '-', '*', '/', '^' -> tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                    case ',' -> tokens.add(new Token(TokenType.COMMA, ","));
                    case '(' -> tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    case ')' -> tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    default -> throw new IllegalArgumentException("Unexpected character: " + c);
                }
                i++;
            }
        }

        return tokens;
    }

}
