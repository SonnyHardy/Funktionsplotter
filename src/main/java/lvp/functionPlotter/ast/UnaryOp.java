package lvp.functionPlotter.ast;

/**
 * Represents a unary operation in the Abstract Syntax Tree (AST) of the function plotter.
 * This class is used to denote operations that involve a single operand, such as negation or square root.
 */
public record UnaryOp(String operator, Expr operand) implements Expr {
}
