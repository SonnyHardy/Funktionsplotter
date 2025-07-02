package lvp.functionPlotter.evaluator;

import lvp.functionPlotter.ast.Expr;
import lvp.functionPlotter.parser.Parser;

import java.text.ParseException;
import java.util.Map;

public class Evaluator {

    public static void main(String[] args) {
        String expression = "sin(pi/2) * sqrt(9) + x";
        Map<String, Double> variable = Map.of("x", 3.0);
        try {
            Expr expr = Parser.parse(expression);
            assert expr != null;
            double result = expr.evaluate(expr ,variable);
            System.out.println("Result: " + result);
        }catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
