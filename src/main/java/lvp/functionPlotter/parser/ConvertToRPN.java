package lvp.functionPlotter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/**
 * Converts an infix expression represented by a list of tokens into Reverse Polish Notation (RPN).
 * This class handles the conversion while considering operator precedence and associativity,
 * including unary operators and conditional expressions.
 */
public class ConvertToRPN {

    // Structure pour stocker les expressions conditionnelles pendant la conversion
    private static class ConditionalData {
        Token questionMark;
        Token colon;
        int conditionIndex;
        int trueExprIndex;
        int falseExprIndex;

        ConditionalData(Token questionMark) {
            this.questionMark = questionMark;
        }
    }

    public static List<Token> toRPN(List<Token> tokens) {
        // Prétraitement des opérateurs unaires
        List<Token> processedTokens = preprocessUnaryOperators(tokens);

        // Si l'expression contient des opérateurs conditionnels (? et :), on utilise une approche spéciale
        if (hasConditionalOperators(processedTokens)) {
            return handleConditionalExpression(processedTokens);
        }

        // Pour les expressions non conditionnelles, utiliser l'algorithme RPN standard
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

                case OPERATOR, UNARY_OPERATOR, COMPARISON -> {
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

                default -> {
                    throw new IllegalArgumentException("Unexpected token type in standard RPN conversion: " + token.type());
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }

        return output;
    }

    /**
     * Vérifie si la liste de tokens contient des opérateurs conditionnels (? et :).
     */
    private static boolean hasConditionalOperators(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.type() == TokenType.QUESTION_MARK || token.type() == TokenType.COLON) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gère les expressions conditionnelles en les décomposant en sous-expressions
     * et en les convertissant en notation RPN spéciale pour les conditionnelles.
     */
    private static List<Token> handleConditionalExpression(List<Token> tokens) {
        // Trouver les positions des opérateurs ? et :
        int questionMarkPos = -1;
        int colonPos = -1;

        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type() == TokenType.QUESTION_MARK) {
                if (questionMarkPos != -1) {
                    throw new IllegalArgumentException("Nested conditional expressions are not supported");
                }
                questionMarkPos = i;
            } else if (tokens.get(i).type() == TokenType.COLON) {
                if (questionMarkPos == -1) {
                    throw new IllegalArgumentException("Colon without matching question mark");
                }
                colonPos = i;
                break; // On prend le premier : après le ?
            }
        }

        if (questionMarkPos == -1) {
            throw new IllegalArgumentException("Conditional expression expected but no question mark found");
        }
        if (colonPos == -1) {
            throw new IllegalArgumentException("Conditional expression is missing colon");
        }

        // Extraire les trois parties de l'expression conditionnelle
        List<Token> conditionTokens = new ArrayList<>(tokens.subList(0, questionMarkPos));
        List<Token> trueExprTokens = new ArrayList<>(tokens.subList(questionMarkPos + 1, colonPos));
        List<Token> falseExprTokens = new ArrayList<>(tokens.subList(colonPos + 1, tokens.size()));

        // Convertir chaque partie en RPN
        List<Token> conditionRPN = toRPN(conditionTokens);
        List<Token> trueExprRPN = toRPN(trueExprTokens);
        List<Token> falseExprRPN = toRPN(falseExprTokens);

        // Combiner les résultats avec des marqueurs spéciaux pour l'analyse ultérieure
        List<Token> result = new ArrayList<>(conditionRPN);
        result.addAll(trueExprRPN);
        result.addAll(falseExprRPN);
        result.add(new Token(TokenType.OPERATOR, "?:"));  // Marqueur spécial pour une expression conditionnelle

        return result;
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
        if (token.type() == TokenType.COMPARISON) {
            return 0; // Les comparaisons ont la priorité la plus basse
        }
        if (token.type() == TokenType.QUESTION_MARK || token.type() == TokenType.COLON) {
            return -1; // Encore plus basse que les comparaisons
        }

        String operator = token.value();
        return switch (operator) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            case "^" -> 3;
            case "u+", "u-" -> 4; // Unary operators have higher precedence
            case "?:" -> -1; // L'opérateur conditionnel a la priorité la plus basse
            default -> 0;
        };
    }

}
