package lvp.functionPlotter.parser;

import lvp.functionPlotter.ast.*;

import java.text.ParseException;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;


/**
 * Parses a mathematical expression represented as a string into an abstract syntax tree (AST).
 * This class handles the conversion from infix notation to Reverse Polish Notation (RPN),
 * and constructs the corresponding AST nodes for constants, variables, binary operations,
 * unary operations, and function calls.
 */
public class Parser {
    public static void main(String[] args) {
        // Example usage
        try {
            Expr expr = parse("-3 + 4 * x - sin(pi / 2)");
            System.out.println(expr);
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
        }
    }


    /**
     * Parses the input string into an expression tree.
     *
     * @param input The mathematical expression as a string.
     * @return An Expr object representing the parsed expression.
     * @throws ParseException If the input cannot be parsed into a valid expression.
     */
    public static Expr parse(String input) throws ParseException {
        List<Token> tokens = Tokenizer.tokenize(input);
        List<Token> rpnTokens = ConvertToRPN.toRPN(tokens);

        if (rpnTokens.isEmpty()) {
            throw new ParseException("Empty expression", 0);
        }

        Stack<Expr> stack = new Stack<>();

        for (int i = 0; i < rpnTokens.size(); i++) {
            Token rpnToken = rpnTokens.get(i);

            try {
                switch (rpnToken.type()) {
                    case NUMBER -> stack.push(new Constant(parseNumber(rpnToken.value())));

                    case VARIABLE -> stack.push(new Variable(rpnToken.value()));

                    case OPERATOR -> {
                        if (stack.size() < 2) {
                            throw new ParseException("Insufficient operands for operator '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        Expr right = stack.pop();
                        Expr left = stack.pop();
                        stack.push(new BinaryOp(rpnToken.value(), left, right));
                    }

                    case UNARY_OPERATOR -> {
                        if (stack.isEmpty()) {
                            throw new ParseException("Insufficient operands for unary operator '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        Expr operand = stack.pop();
                        // Enlever le préfixe 'u' pour l'opérateur unaire
                        String op = rpnToken.value().substring(1);
                        stack.push(new UnaryOp(op, operand));
                    }

                    case FUNCTION -> {
                        if (stack.isEmpty()) {
                            throw new ParseException("Insufficient arguments for function '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        // Pour l'instant, on assume les fonctions à un argument
                        // À adapter selon le nombre d'arguments requis par chaque fonction
                        Expr arg = stack.pop();
                        stack.push(new FunctionCall(rpnToken.value(), List.of(arg)));
                    }

                    default -> throw new ParseException("Unexpected rpnToken type: " +
                            rpnToken.type() + " at position " + i, i);
                }
            } catch (EmptyStackException e) {
                throw new ParseException("Malformed RPN expression: insufficient operands at position " + i, i);
            }
        }

        if (stack.size() != 1) {
            throw new ParseException("Invalid RPN expression: expected 1 result, got " +
                    stack.size() + " elements remaining", input.length());
        }

        return stack.pop();
    }


    /**
     * Parses a string representation of a number, handling special cases like "pi" and "e".
     *
     * @param value The string to parse.
     * @return The parsed double value.
     */
    private static double parseNumber(String value) {
        return switch (value.toLowerCase()) {
            case "pi" -> Math.PI;
            case "e" -> Math.E;
            default -> Double.parseDouble(value);
        };
    }

}
