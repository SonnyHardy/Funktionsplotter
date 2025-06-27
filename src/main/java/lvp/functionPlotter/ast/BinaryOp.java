package lvp.functionPlotter.ast;

/**
 * Represents a binary operation in the Abstract Syntax Tree (AST) of the function plotter.
 * This class is used to denote operations that involve two operands, such as addition, subtraction, multiplication, and division.
 */
public record BinaryOp(String operator, Expr left, Expr right) implements Expr {
}
