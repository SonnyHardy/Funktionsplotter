package lvp.functionPlotter.ast;

import java.util.List;

/**
 * Represents a function call in the Abstract Syntax Tree (AST) of the function plotter.
 * This class is used to denote a call to a function, which may include arguments.
 * Example: sin(x), log(x, base), ...
 */
public record FunctionCall(String functionName, List<Expr> arguments) implements Expr {
}
