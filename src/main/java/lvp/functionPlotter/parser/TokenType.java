package lvp.functionPlotter.parser;

public enum TokenType {

        NUMBER, // Represents a numeric value
        VARIABLE, // Represents a variable (e.g., x, y)
        OPERATOR, // Represents an operator (e.g., +, -, *, /)
        UNARY_OPERATOR, // Represents a unary operator (e.g., - for negation)
        FUNCTION, // Represents a function (e.g., sin, cos)
        LEFT_PAREN, // Represents a left parenthesis '('
        RIGHT_PAREN, // Represents a right parenthesis ')'
        COMMA // Represents a comma in function arguments
}
