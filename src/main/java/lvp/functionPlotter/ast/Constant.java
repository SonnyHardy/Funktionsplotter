package lvp.functionPlotter.ast;

/**
 * Represents a constant expression in the Abstract Syntax Tree (AST) of the function plotter.
 * This class is used to denote a constant value in mathematical expressions.
 */
public record Constant(double value) implements Expr {
}
