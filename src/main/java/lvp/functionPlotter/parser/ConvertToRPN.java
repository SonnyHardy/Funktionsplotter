package lvp.functionPlotter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Converts an infix expression represented by a list of tokens into Reverse Polish Notation (RPN).
 * This class handles the conversion while considering operator precedence and associativity,
 * including unary operators.
 */
public class ConvertToRPN {

    public static List<Token> toRPN(List<Token> tokens) {
        List<Token> processedTokens = preprocessUnaryOperators(tokens);
        List<Token> output = new ArrayList<>();
        Stack<Token> operatorStack = new Stack<>();

        for (Token token : processedTokens) {
            switch (token.type()) {
                case NUMBER, VARIABLE -> output.add(token);

                case FUNCTION -> operatorStack.push(token);

                case COMMA -> {
                    while (!operatorStack.isEmpty() &&
                            operatorStack.peek().type() != TokenType.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }
                }

                case OPERATOR, UNARY_OPERATOR -> {
                    while (!operatorStack.isEmpty() &&
                            shouldPopOperator(operatorStack.peek(), token)) {
                        output.add(operatorStack.pop());
                    }
                    operatorStack.push(token);
                }

                case LEFT_PAREN -> operatorStack.push(token);

                case RIGHT_PAREN -> {
                    while (!operatorStack.isEmpty() &&
                            operatorStack.peek().type() != TokenType.LEFT_PAREN) {
                        output.add(operatorStack.pop());
                    }

                    if (!operatorStack.isEmpty() &&
                            operatorStack.peek().type() == TokenType.LEFT_PAREN) {
                        operatorStack.pop(); // Supprime '('
                    }

                    if (!operatorStack.isEmpty() &&
                            operatorStack.peek().type() == TokenType.FUNCTION) {
                        output.add(operatorStack.pop());
                    }
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }

        return output;
    }


    /**
     * Preprocesses the tokens to handle unary operators.
     * Converts unary operators into a specific format (e.g., "u-" for unary minus).
     *
     * @param tokens The list of tokens to preprocess.
     * @return A new list of tokens with unary operators processed.
     */
    private static List<Token> preprocessUnaryOperators(List<Token> tokens) {
        List<Token> processed = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Token prevToken = i > 0 ? tokens.get(i - 1) : null;

            if (isUnaryOperator(token, prevToken)) {
                // Convertir l'opérateur binaire en opérateur unaire
                processed.add(new Token(TokenType.UNARY_OPERATOR,
                        "u" + token.value()));     // u- pour unaire minus, u+ pour unaire plus
            } else {
                processed.add(token);
            }
        }

        return processed;
    }


    /**
     * Determines if the top operator on the stack should be popped based on the current token.
     *
     * @param stackTop The top token on the operator stack.
     * @param current The current token being processed.
     * @return true if the top operator should be popped, false otherwise.
     */
    private static boolean shouldPopOperator(Token stackTop, Token current) {
        if (stackTop.type() == TokenType.LEFT_PAREN) return false;
        if (stackTop.type() == TokenType.FUNCTION) return false;

        // Les opérateurs unaires ont une associativité à droite
        if (current.type() == TokenType.UNARY_OPERATOR) {
            return precedence(stackTop) > precedence(current);
        }

        // Les opérateurs binaires ont une associativité à gauche
        return precedence(stackTop) >= precedence(current);
    }


    /**
     * Checks if a token is a unary operator based on the previous token.
     *
     * @param token The current token to check.
     * @param prevToken The previous token in the expression.
     * @return true if the token is a unary operator, false otherwise.
     */
    private static boolean isUnaryOperator(Token token, Token prevToken) {
        if (token.type() != TokenType.OPERATOR || !"+-".contains(token.value())) {
            return false;
        }

        // Opérateur unaire si:
        // 1. Au début de l'expression
        if (prevToken == null) return true;

        // 2. Après une parenthèse ouvrante
        if (prevToken.type() == TokenType.LEFT_PAREN) return true;

        // 3. Après un autre opérateur
        if (prevToken.type() == TokenType.OPERATOR) return true;

        // 4. Après une virgule (dans les fonctions)
        if (prevToken.type() == TokenType.COMMA) return true;

        return false;
    }


    /**
     * Determines the precedence of a given token.
     *
     * @param token The token for which to determine the precedence.
     * @return An integer representing the precedence level of the token.
     */
    private static int precedence(Token token) {
        String operator = token.value();
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            case "u+", "u-" -> 4; // Unary operators have higher precedence
            default -> 0;
        };
    }

}
