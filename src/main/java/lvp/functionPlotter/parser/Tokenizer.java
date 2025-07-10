package lvp.functionPlotter.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * Tokenizer class for breaking down a mathematical expression into tokens.
 * This class is responsible for parsing the input string and returning a list of tokens.
 */
public class Tokenizer {

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        String foundVariable = null; // Pour stocker la première variable trouvée

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

                String tokenValue = value.toString();

                if (tokenValue.equals("pi") || tokenValue.equals("e") || tokenValue.equals("π") || tokenValue.equals("ℯ")) {
                    // Handle special constants like pi and e
                    tokens.add(new Token(TokenType.NUMBER, tokenValue));
                } else if (tokenValue.length() == 1) {
                    // C'est une variable
                    if (foundVariable == null) {
                        // Première variable trouvée
                        foundVariable = tokenValue;
                        tokens.add(new Token(TokenType.VARIABLE, tokenValue));
                    } else if (foundVariable.equals(tokenValue)) {
                        // Même variable que celle déjà trouvée
                        tokens.add(new Token(TokenType.VARIABLE, tokenValue));
                    } else {
                        // Variable différente de celle déjà trouvée
                        System.out.println("Multiple variables not allowed. Found '" +
                                foundVariable + "' and '" + tokenValue + "' in the same expression.");
                        throw new IllegalArgumentException("Multiple variables not allowed. Found '" +
                                foundVariable + "' and '" + tokenValue + "' in the same expression.");
                    }
                } else {
                    // C'est une fonction
                    tokens.add(new Token(TokenType.FUNCTION, tokenValue));
                }

            } else {
                switch (c) {
                    case '+', '-', '*', '/', '^' -> tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                    case ',' -> tokens.add(new Token(TokenType.COMMA, ","));
                    case '(' -> tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                    case ')' -> tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                    default -> {
                        System.out.println("Unexpected character: " + c);
                        throw new IllegalArgumentException("Unexpected character: " + c);
                    }
                }
                i++;
            }
        }

        return tokens;
    }

}