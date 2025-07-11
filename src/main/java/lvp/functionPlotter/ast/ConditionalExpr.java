package lvp.functionPlotter.ast;

/**
 * Represents a conditional expression in the form of condition ? trueExpr : falseExpr.
 * This allows for conditional values based on a comparison operation.
 */
public record ConditionalExpr(Expr condition, Expr trueExpr, Expr falseExpr) implements Expr {
    /**
     * Creates a new conditional expression with the specified condition, true expression, and false expression.
     *
     * @param condition The condition to evaluate
     * @param trueExpr The expression to evaluate if the condition is true
     * @param falseExpr The expression to evaluate if the condition is false
     */
    public ConditionalExpr {
        // Validate parameters
        if (condition == null) {
            throw new IllegalArgumentException("Condition expression cannot be null");
        }
        if (trueExpr == null) {
            throw new IllegalArgumentException("True expression cannot be null");
        }
        if (falseExpr == null) {
            throw new IllegalArgumentException("False expression cannot be null");
        }
    }
}
