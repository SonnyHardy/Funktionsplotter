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
 * unary operations, function calls, comparisons and conditional expressions.
 */
public class Parser {

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
            System.out.println("Empty expression provided for parsing.");
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
                        // Vérifier si c'est l'opérateur spécial conditionnel
                        if (rpnToken.value().equals("?:")) {
                            // Structure d'une expression conditionnelle: condition, trueExpr, falseExpr, "?:"
                            if (stack.size() < 3) {
                                throw new ParseException("Insufficient operands for conditional expression at position " + i, i);
                            }
                            Expr falseExpr = stack.pop();
                            Expr trueExpr = stack.pop();
                            Expr condition = stack.pop();
                            stack.push(new ConditionalExpr(condition, trueExpr, falseExpr));
                        } else {
                            // Opérateur binaire normal
                            if (stack.size() < 2) {
                                System.out.println("Insufficient operands for operator '" +
                                        rpnToken.value() + "' at position " + i);
                                throw new ParseException("Insufficient operands for operator '" +
                                        rpnToken.value() + "' at position " + i, i);
                            }
                            Expr right = stack.pop();
                            Expr left = stack.pop();
                            stack.push(new BinaryOp(rpnToken.value(), left, right));
                        }
                    }

                    case UNARY_OPERATOR -> {
                        if (stack.isEmpty()) {
                            System.out.println("Insufficient operands for unary operator '" +
                                    rpnToken.value() + "' at position " + i);
                            throw new ParseException("Insufficient operands for unary operator '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        Expr operand = stack.pop();
                        // Remove the leading '-' or '+' from the operator
                        String op = rpnToken.value().substring(1);
                        stack.push(new UnaryOp(op, operand));
                    }

                    case FUNCTION -> {
                        if (stack.isEmpty()) {
                            System.out.println("Insufficient arguments for function '" +
                                    rpnToken.value() + "' at position " + i);
                            throw new ParseException("Insufficient arguments for function '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        Expr arg = stack.pop();
                        stack.push(new FunctionCall(rpnToken.value(), List.of(arg)));
                    }

                    case COMPARISON -> {
                        if (stack.size() < 2) {
                            System.out.println("Insufficient operands for comparison '" +
                                    rpnToken.value() + "' at position " + i);
                            throw new ParseException("Insufficient operands for comparison '" +
                                    rpnToken.value() + "' at position " + i, i);
                        }
                        Expr right = stack.pop();
                        Expr left = stack.pop();
                        stack.push(new ComparisonExpr(rpnToken.value(), left, right));
                    }

                    default -> {
                        System.out.println("Unexpected token type: " + rpnToken.type() +
                                " at position " + i);
                        throw new ParseException("Unexpected rpnToken type: " +
                                rpnToken.type() + " at position " + i, i);
                    }
                }
            } catch (EmptyStackException e) {
                System.out.println("Malformed RPN expression: insufficient operands at position " + i);
                throw new ParseException("Malformed RPN expression: insufficient operands at position " + i, i);
            }
        }

        if (stack.size() != 1) {
            System.out.println("Invalid RPN expression: expected 1 result, got " + stack.size() +
                    " elements remaining at the end of parsing.");
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
            case "pi", "π" -> Math.PI;
            case "e", "ℯ" -> Math.E;
            default -> Double.parseDouble(value);
        };
    }

}
