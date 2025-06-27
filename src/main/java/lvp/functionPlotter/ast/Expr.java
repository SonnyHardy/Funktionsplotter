package lvp.functionPlotter.ast;

import java.util.Map;


/**
 * Represents a mathematical expression in an abstract syntax tree (AST) format.
 * This interface defines the structure for various types of expressions, including
 * binary operations, unary operations, constants, variables, and function calls.
 */
public sealed interface Expr permits BinaryOp, Constant, FunctionCall, Variable, UnaryOp {


    /**
     * Evaluates the given expression based on the provided variable mappings and computes the result.
     * The method supports evaluation of constants, variables, binary operations, unary operations, and function calls.
     *
     * @param expr The expression to evaluate. It could be a constant, variable, binary operation, unary operation, or function call.
     * @param var A map of variable names to their corresponding values. Used to resolve variables in the expression.
     * @return The result of evaluating the expression as a double.
     * @throws IllegalArgumentException If a variable is not defined in the provided map, an unknown operator is encountered,
     *                                  or if an unsupported function is called.
     */
     default double evaluate(Expr expr, Map<String, Double> var) {
         return switch (expr){
             case Constant constant -> constant.value();
             case Variable variable -> {
                 String name = variable.name();
                 if (!var.containsKey(name)) {
                     throw new IllegalArgumentException("Variable not defined : " + name);
                 }
                 yield var.get(name);
             }
             case  BinaryOp binaryOp -> {
                 double left = evaluate(binaryOp.left(), var);
                 double right = evaluate(binaryOp.right(), var);
                 yield applyOperator(binaryOp.operator(), left, right);
             }
             case UnaryOp unaryOp -> {
                 double operand = evaluate(unaryOp.operand(), var);
                 yield applyOperator(unaryOp.operator(), operand);
             }
             case  FunctionCall functionCall -> {
                 String func = functionCall.functionName().toLowerCase();
                 double arg = evaluate(functionCall.arguments().get(0), var);
                 yield applyFunction(func, arg);
             }
         };

    }


    /**
     * Applies a binary operator to two operands.
     *
     * @param operator The operator to apply (e.g., "+", "-", "*", "/", "^").
     * @param left     The left operand.
     * @param right    The right operand.
     * @return The result of the operation.
     */
    static double applyOperator(String operator, double left, double right) {
        return switch (operator) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> {
                if (right == 0) {
                    throw new IllegalArgumentException("Division by zero is not allowed.");
                }
                yield left / right;
            }
            case "^" -> Math.pow(left, right);
            default -> throw new IllegalArgumentException("Unknown operator : " + operator);
        };
    }


    /**
     * Applies a unary operator to a single operand.
     *
     * @param operator The unary operator to apply (e.g., "+", "-").
     * @param operand  The operand to which the operator is applied.
     * @return The result of the unary operation.
     */
    static double applyOperator(String operator, double operand) {
        return switch (operator) {
            case "+" -> operand; // Unary plus, no change
            case "-" -> -operand; // Unary minus
            default -> throw new IllegalArgumentException("Unknown operator : " + operator);
        };
    }


    /**
     * Applies a mathematical function to a single argument.
     *
     * @param func The name of the function (e.g., "sin", "cos", "tan", "log", "sqrt", "abs").
     * @param arg  The argument to which the function is applied.
     * @return The result of the function application.
     */
    static double applyFunction(String func, double arg) {
        return switch (func.toLowerCase()) {
            case "sin" -> Math.sin(arg);
            case "cos" -> Math.cos(arg);
            case "tan" -> Math.tan(arg);
            case "log" -> Math.log(arg);
            case "sqrt" -> {
                if (arg < 0) {
                    throw new IllegalArgumentException("Cannot compute square root of a negative number: " + arg);
                }
                yield Math.sqrt(arg);
            }
            case "abs" -> Math.abs(arg);
            default -> throw new IllegalArgumentException("Unknown function: " + func);
        };
    }

}
