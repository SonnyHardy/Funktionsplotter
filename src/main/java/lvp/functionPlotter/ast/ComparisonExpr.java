package lvp.functionPlotter.ast;

/**
 * Represents a comparison expression in the abstract syntax tree.
 * This includes operations like <, >, <=, >=, ==, and !=.
 */
public record ComparisonExpr(String operator, Expr left, Expr right) implements Expr {
    /**
     * Creates a new comparison expression with the specified operator and operands.
     *
     * @param operator The comparison operator (<, >, <=, >=, ==, !=)
     * @param left The left operand of the comparison
     * @param right The right operand of the comparison
     */
    public ComparisonExpr {
        // Validate parameters
        if (operator == null || operator.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }
        if (!isValidOperator(operator)) {
            throw new IllegalArgumentException("Invalid comparison operator: " + operator);
        }
        if (left == null) {
            throw new IllegalArgumentException("Left operand cannot be null");
        }
        if (right == null) {
            throw new IllegalArgumentException("Right operand cannot be null");
        }
    }

    /**
     * Checks if the given string is a valid comparison operator.
     *
     * @param operator The operator to check
     * @return true if the operator is valid, false otherwise
     */
    private static boolean isValidOperator(String operator) {
        return operator.equals("<") || 
               operator.equals(">") || 
               operator.equals("<=") || 
               operator.equals(">=") || 
               operator.equals("==") || 
               operator.equals("!=");
    }
}
