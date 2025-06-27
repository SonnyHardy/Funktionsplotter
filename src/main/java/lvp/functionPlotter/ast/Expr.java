package lvp.functionPlotter.ast;

/**
 * Marker interface for all expression types in the AST (Abstract Syntax Tree).
 * This interface is used to represent different kinds of expressions in the function plotter.
 */
public sealed interface Expr permits BinaryOp, Constant, FunctionCall, Variable, UnaryOp {

}
