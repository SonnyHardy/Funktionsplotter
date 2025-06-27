package lvp.functionPlotter.ast;

/**
 * Represents a variable in the Abstract Syntax Tree (AST) of the function plotter.
 * This class is used to denote a variable in mathematical expressions.
 */
public record Variable(String name) implements Expr {
}
