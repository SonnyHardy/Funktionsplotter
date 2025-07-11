package lvp.functionPlotter.plotter;

import lvp.Clerk;
import lvp.views.Dot;
import lvp.views.Turtle;
import lvp.functionPlotter.ast.*;
import lvp.functionPlotter.parser.Parser;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ein Funktionsplotter, der eine mathematische Funktion visualisiert und den
 * zugehörigen abstrakten Syntaxbaum (AST) darstellt.
 */
public class FunctionPlotter {
    private Turtle turtle;
    private double viewWidth;
    private double viewHeight;
    private String interval;
    private double xFrom;
    private double xTo;
    private double yFrom;
    private double yTo;
    private double step;
    private double xFromPixel;
    private double xToPixel;
    private double yFromPixel;
    private double yToPixel;
    private boolean showGrid;

    /**
     * Erstellt einen neuen FunctionPlotter mit dem angegebenen Intervall.
     *
     * @param interval Das Intervall für die Darstellung im Format "[min;max]"
     */
    public FunctionPlotter(String interval) {
        this.interval = interval;
        this.viewWidth = 200;
        this.viewHeight = 200;
        this.showGrid = true;
        initializeCoordinates();
        initializeTurtle();
    }

    /**
     * Initialisiert die Koordinatenwerte basierend auf dem Intervall.
     */
    private void initializeCoordinates() {
        double[] limits = parseInterval(interval);
        this.xFrom = limits[0];
        this.xTo = limits[1];
        this.yFrom = limits[0];
        this.yTo = limits[1];

        this.step = calculateStep(xFrom, xTo, viewWidth);
        this.xFromPixel = xFrom * step;
        this.xToPixel = xTo * step;
        this.yFromPixel = yFrom * step;
        this.yToPixel = yTo * step;
    }

    /**
     * Initialisiert die Turtle und zeichnet das Koordinatensystem.
     */
    private void initializeTurtle() {
        double startX = 0;
        double startY = 0;
        double startAngle = 0;
        this.turtle = new Turtle(xFromPixel, xToPixel, yFromPixel, yToPixel, startX, startY, startAngle);
        drawCartesianSystem();
    }

    /**
     * Zeichnet den AST für einen mathematischen Ausdruck.
     *
     * @param expression Der mathematische Ausdruck
     */
    public void drawExpressionAST(String expression) {
        try {
            // Ausdruck analysieren, um den AST zu erstellen
            Expr ast = Parser.parse(expression);

            // Dot für die Zeichnung des Graphen initialisieren
            Dot dot = new Dot();

            // DOT-Graph für den AST generieren
            String dotGraph = generateASTDotGraph(ast);

            // Ursprünglichen Ausdruck über dem Graphen anzeigen
            Clerk.markdown("**Ausdruck**: `" + expression + "`");

            // Graph mit Dot zeichnen
            dot.draw(dotGraph);

        } catch (ParseException e) {
            Clerk.markdown("**Analysefehler**: " + e.getMessage());
        }
    }

    /**
     * Zeichnet eine mathematische Funktion mit der angegebenen Farbe.
     *
     * @param functionExpression Die mathematische Funktion zum Zeichnen als String
     * @param r Rotwert der Farbe (0-255)
     * @param g Grünwert der Farbe (0-255)
     * @param b Blauwert der Farbe (0-255)
     */
    public void plotFunction(String functionExpression, int r, int g, int b) {
        turtle.color(r, g, b).width(1.0);
        plotFunctionInternal(functionExpression);
    }

    /**
     * Gibt die Turtle-Ansicht aus.
     */
    public void writeTurtle() {
        turtle.write();
    }

    /**
     * Interne Methode zum Zeichnen einer mathematischen Funktion.
     *
     * @param functionExpression Die mathematische Funktion zum Zeichnen als String
     * @return Die Turtle-Instanz nach dem Zeichnen der Funktion
     */
    private Turtle plotFunctionInternal(String functionExpression) {
        turtle.push();

        boolean firstPoint = true;
        double prevXPixel = 0, prevYPixel = 0;
        final double stepSize = Math.min(0.05, (xTo - xFrom) / 1000.0);
        //final double stepSize = 0.1; // Schrittweite für die Auswertung der Funktion

        // Funktion-Ausdruck in ein Expr-Objekt parsen
        Expr function = null;
        try {
            function = Parser.parse(functionExpression);
        } catch (ParseException e) {
            System.out.println("Fehler beim Parsen des Funktionsausdrucks: " + e.getMessage());
            return turtle; // Turtle ohne Zeichnung zurückgeben, wenn das Parsen fehlschlägt
        } catch (Exception e) {
            System.out.println("Unerwarteter Fehler beim Parsen des Funktionsausdrucks: " + e.getMessage());
            return turtle; // Turtle ohne Zeichnung zurückgeben, wenn ein unerwarteter Fehler auftritt
        }

        assert function != null : "Funktionsausdruck darf nicht null sein";
        String variableName = extractVariableName(functionExpression);

        for (double xMath = xFrom; xMath <= xTo; xMath += stepSize) {
            try {
                // Berechne den Y-Wert der Funktion für den aktuellen X-Wert
                Map<String, Double> variable = Map.of(variableName, xMath);
                double yMath = function.evaluate(function, variable);

                // Prüfen ob Y-Wert im sichtbaren Bereich liegt (mathematische Koordinaten)
                if (yMath >= yFrom && yMath <= yTo) {
                    // Umrechnung der mathematischen Koordinaten in Pixelkoordinaten
                    double xPixel = xMath * step;
                    double yPixel = yMath * step;

                    if (!firstPoint) {
                        // Linie vom vorherigen Punkt zum aktuellen Punkt zeichnen
                        turtle.moveTo(prevXPixel, prevYPixel, xPixel, yPixel, true);
                    }
                    prevXPixel = xPixel;
                    prevYPixel = yPixel;
                    firstPoint = false;
                } else {
                    // Punkt außerhalb des sichtbaren Bereichs
                    firstPoint = true;
                }
            } catch (Exception e) {
                // Fehler bei der Funktionsauswertung (z.B. Division durch Null)
                firstPoint = true;
            }
        }

        turtle.pop();
        return turtle;
    }

    /**
     * Zeichnet ein kartesisches Koordinatensystem mit Achsen und optionaler Rasterung
     */
    private void drawCartesianSystem() {
        // Aktuellen Zustand speichern
        turtle.push();

        // Achsenfarbe (schwarz)
        turtle.color(0, 0, 0);

        // X-Achse zeichnen
        turtle.moveTo(xFromPixel, 0.0, xToPixel, 0.0, true);

        // Y-Achse zeichnen
        turtle.moveTo(0.0, yFromPixel, 0.0, yToPixel, true);

        if (showGrid) {
            // Rasterlinien zeichnen
            turtle.color(200, 200, 200).width(0.1); // Hellgrau, dünn

            // Vertikale Rasterlinien
            for (double x = xFromPixel; x <= xToPixel; x += step) {
                if (x != 0) { // Nicht die Y-Achse übermalen
                    turtle.moveTo(x, yFromPixel, x, yToPixel, true);
                }

                // Nummerierung der X-Achse
                if (x != 0) { // Nicht die Null bei (0,0) doppelt schreiben
                    turtle.color(0, 0, 0); // Schwarz für Text
                    String number = String.valueOf((int)(x / step));

                    double xPos = x;
                    if (x == xToPixel) xPos -= step * 0.4;   // Leicht nach innen verschieben, wenn am Rand

                    turtle.moveTo(xPos, step * 0.1, xPos, step * 0.1, false);     // Zur Position ohne Zeichnen und leicht nach oben
                    turtle.text(number, "3px Arial");
                }
            }

            // Horizontale Rasterlinien und Nummerierung der Y-Achse
            turtle.color(200, 200, 200).width(0.1); // Zurück zu Hellgrau für Raster
            for (double y = yFromPixel; y <= yToPixel; y += step) {
                if (y != 0) { // Nicht die X-Achse übermalen
                    turtle.moveTo(xFromPixel, y, xToPixel, y, true);
                }

                // Nummerierung der Y-Achse
                if (y != 0) { // Nicht die Null bei (0,0) doppelt schreiben
                    turtle.color(0, 0, 0); // Schwarz für Text
                    String number = String.valueOf((int)(y / step));

                    double yPos = y;
                    if (y == yToPixel) yPos -= step * 0.3;      // Leicht nach innen verschieben, wenn am Rand

                    turtle.moveTo(0, yPos, 0, yPos, false);   //Zur Position ohne Zeichnen
                    turtle.text(number, "3px Arial");
                }
            }

            // Null am Ursprung schreiben
            turtle.color(0, 0, 0);
            turtle.moveTo(0, step * 0.1, 0, step * 0.1, false);
            turtle.text("0", "3px Arial");
        }

        // Ursprünglichen Zustand wiederherstellen
        turtle.pop();
    }

    /**
     * Interpretiert einen Intervallstring im Format "[min;max]" und gibt die Min- und Max-Werte zurück.
     */
    private double[] parseInterval(String interval) {
        // Entferne die eckigen Klammern und teile den String in zwei Teile
        String cleaned = interval.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(";");

        double min = Double.parseDouble(parts[0]);
        double max = Double.parseDouble(parts[1]);

        return new double[]{min, max};
    }

    /**
     * Extrahiert den Variablennamen aus einem Funktionsausdruck.
     */
    private String extractVariableName(String expression) {
        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue; // Skip whitespace
            }

            if (Character.isLetter(c)) {
                StringBuilder value = new StringBuilder();
                while (i < expression.length() && Character.isLetterOrDigit(expression.charAt(i))) {
                    value.append(expression.charAt(i));
                    i++;
                }

                String tokenValue = value.toString();

                if (tokenValue.length() == 1) {
                    // Es ist eine Variable
                    return tokenValue;
                }
            }
            i++;
        }
        return "x"; // Fallback auf 'x', falls keine Variable gefunden wurde
    }

    /**
     * Berechnet die optimale Schrittweite für das Zeichnen.
     */
    private double calculateStep(double xFrom, double xTo, double viewWidth) {
        // Berechne die Schrittweite basierend auf der Breite der Ansicht und dem Bereich der x-Werte
        double maxAbsX = Math.max(Math.abs(xFrom), Math.abs(xTo));

        double range = -xFrom == xTo ? xTo - xFrom : 2 * maxAbsX;   // Bereich der x-Werte
        double idealStep = viewWidth / range;

        if (viewWidth % idealStep == 0) {
            return idealStep; // Wenn die Schrittweite genau passt, verwenden
        }

        // Ansonsten den Divisor finden, der am nächsten an der idealen Schrittweite liegt
        double closestDivisor = findClosestDivisor(viewWidth, idealStep);
        return closestDivisor;
    }

    /**
     * Findet den Teiler von 'number', der am nächsten an 'target' liegt.
     */
    private double findClosestDivisor(double number, double target) {
        double closestDivisor = 1.0;
        double minDifference = Math.abs(target - 1.0);

        // alle Divisoren von 'number' durchgehen
        for (double divisor = 1.0; divisor <= number; divisor += 1.0) {
            if (number % divisor == 0) { // divisor ist ein Teiler von number
                double difference = Math.abs(target - divisor);
                if (difference < minDifference) {
                    minDifference = difference;
                    closestDivisor = divisor;
                }
            }
        }
        return closestDivisor;
    }

    /**
     * Erzeugt eine DOT-Darstellung (für Graphviz) des abstrakten Syntaxbaums.
     */
    private String generateASTDotGraph(Expr ast) {
        StringBuilder dotBuilder = new StringBuilder();
        dotBuilder.append("digraph AST {\n");
        dotBuilder.append("  node [fontname=\"Arial\"];\n");
        dotBuilder.append("  edge [fontname=\"Arial\"];\n");
        dotBuilder.append("  rankdir=TB;\n"); // Richtung von oben nach unten

        // Zähler für die Erzeugung eindeutiger Kennungen für die Knoten
        AtomicInteger counter = new AtomicInteger(0);

        // Knoten und Kanten des Graphen rekursiv erzeugen
        generateDotNodes(ast, dotBuilder, counter, null, null);

        dotBuilder.append("}\n");
        return dotBuilder.toString();
    }

    /**
     * Erzeugt rekursiv die Knoten und Kanten des DOT-Graphen für den AST.
     */
    private String generateDotNodes(Expr expr, StringBuilder dotBuilder, AtomicInteger counter, 
                                   String parentId, String edgeLabel) {
        String nodeId = "node" + counter.getAndIncrement();
        String nodeLabel = "";
        String nodeColor = "";
        String shape = "ellipse";

        // Inhalt, Farbe und Form des Knotens je nach Typ bestimmen
        if (expr instanceof Constant c) {
            nodeLabel = String.valueOf(c.value());
            nodeColor = "#6495ED"; // Blau für Konstanten
        } else if (expr instanceof Variable v) {
            nodeLabel = v.name();
            nodeColor = "#32CD32"; // Grün für Variablen
            shape = "diamond";
        } else if (expr instanceof BinaryOp op) {
            nodeLabel = op.operator();
            nodeColor = "#FF4500"; // Rot für binäre Operatoren
            shape = "circle";

            // Kindknoten erstellen
            String leftId = generateDotNodes(op.left(), dotBuilder, counter, nodeId, "links");
            String rightId = generateDotNodes(op.right(), dotBuilder, counter, nodeId, "rechts");
        } else if (expr instanceof UnaryOp op) {
            nodeLabel = op.operator();
            nodeColor = "#FFA500"; // Orange für unäre Operatoren
            shape = "circle";

            // Kindknoten erstellen
            String childId = generateDotNodes(op.operand(), dotBuilder, counter, nodeId, "operand");
        } else if (expr instanceof FunctionCall func) {
            nodeLabel = func.functionName();
            nodeColor = "#9370DB"; // Violett für Funktionen
            shape = "box";

            // Argumentknoten erstellen
            for (int i = 0; i < func.arguments().size(); i++) {
                String argId = generateDotNodes(func.arguments().get(i), dotBuilder, counter, nodeId, "arg" + (i+1));
            }
        } else if (expr instanceof ComparisonExpr comp) {
            nodeLabel = comp.operator();
            nodeColor = "#FF6347"; // Tomato pour les comparaisons
            shape = "hexagon";

            // Kindknoten erstellen
            String leftId = generateDotNodes(comp.left(), dotBuilder, counter, nodeId, "links");
            String rightId = generateDotNodes(comp.right(), dotBuilder, counter, nodeId, "rechts");
        } else if (expr instanceof ConditionalExpr cond) {
            nodeLabel = "?:";
            nodeColor = "#FFD700"; // Or pour les conditionnelles
            shape = "trapezium";

            // Kindknoten erstellen
            String conditionId = generateDotNodes(cond.condition(), dotBuilder, counter, nodeId, "condition");
            String trueExprId = generateDotNodes(cond.trueExpr(), dotBuilder, counter, nodeId, "true");
            String falseExprId = generateDotNodes(cond.falseExpr(), dotBuilder, counter, nodeId, "false");
        }

        // Knoten zum Graphen hinzufügen
        dotBuilder.append("  " + nodeId + " [label=\"" + nodeLabel + "\", ");
        dotBuilder.append("style=filled, fillcolor=\"" + nodeColor + "\", ");
        dotBuilder.append("shape=" + shape + "];\n");

        // Kante vom übergeordneten Knoten hinzufügen, wenn es nicht der Wurzelknoten ist
        if (parentId != null) {
            dotBuilder.append("  " + parentId + " -> " + nodeId);
            if (edgeLabel != null) {
                dotBuilder.append(" [label=\"" + edgeLabel + "\"];");
            }
            dotBuilder.append("\n");
        }

        return nodeId;
    }
}

