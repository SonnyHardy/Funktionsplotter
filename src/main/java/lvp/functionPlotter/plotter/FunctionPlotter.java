package lvp.functionPlotter.plotter;

import lvp.Clerk;
import lvp.functionPlotter.ast.*;
import lvp.functionPlotter.parser.Parser;
import lvp.skills.Text;
import lvp.skills.Interaction;
import lvp.views.Dot;
import lvp.views.Turtle;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Ein Funktionsplotter, der eine mathematische Funktion visualisiert und den
 * zugehörigen abstrakten Syntaxbaum (AST) darstellt.
 */
public class FunctionPlotter {

    /**
     * Hauptmethode zur Ausführung des Funktionsplotters.
     */
    //public static void main(String[] args) {

    //}

    public static void main(String[] args) {
        Clerk.clear();
        // Markdown 1
        Clerk.markdown(Text.fillOut("""
    # Interaktive LVP Demo

    ## Markdown
    Die Markdown-View erlaubt es, Markdown-Text direkt im Browser darzustellen. Der folgende Code zeigt ein einfaches Beispiel, wie Text im Markdown-Format 
    an den Browser gesendet und dort automatisch als HTML gerendert wird:
    ```java
    ${0}
    ```
    Der Aufruf `Clerk.markdown(text)` elaubt den einfachen Zugriff auf die Markdown-View.
    In diesem Beispiel werden zusätzlich zwei unterstützende Skills verwendet:
    - `Text.fillOut(...)`: Zum Befüllen von String-Vorlagen mit dynamischen Inhalten, indem Platzhalter (z.B. ${2}) durch die Auswertung von übergebenen Ausdrücken ersetzt werden.
    - `Text.codeBlock(...)`: Zum Einbinden von Codeabschnitten als interaktive Blöcke im Markdown-Text.

    ## Turtle
    Die Turtle-View ermöglicht das Zeichnen und Anzeigen von SVG-Grafiken im Browser. Diese können Schritt für Schritt aufgebaut werden:
    ```java
    ${1}
    ```
    """, Text.codeBlock("./demo2.java", "// Markdown 1"), Text.codeBlock("./demo2.java", "// Turtle 1"), "${0}"));
        // Markdown 1

        // Label Turtle 1
        // Turtle 1
        double viewWidth = 200;
        double viewHeight = 200;
        String interval = "[-15;15]";    // Beispielintervall

        double[] limits = parseInterval(interval);
        double xFrom = limits[0];
        double xTo = limits[1];
        double yFrom = limits[0];
        double yTo = limits[1];

        double step = calculateStep(xFrom, xTo, viewWidth);
        double xFromPixel = xFrom * step;
        double xToPixel = xTo * step;
        double yFromPixel = yFrom * step;
        double yToPixel = yTo * step;

        double startX = 0;
        double startY = 0;
        double startAngle = 0;
        boolean showGrid = true;       // Raster anzeigen

        var turtle = new Turtle(xFromPixel, xToPixel, yFromPixel, yToPixel, startX, startY, startAngle);
        drawCartesianSystem(turtle, xFromPixel, xToPixel, yFromPixel, yToPixel, viewWidth, viewHeight, step, showGrid);

        // Draw function f(x) = x^2 with red color
        String function1 = "x * x"; // Beispiel: f(x) = x^2
        turtle.color(255, 0, 0).width(1.0);

        try {
            System.out.println("Plotting function: " + function1);
            plotFunction(turtle, function1, xFrom, xTo, yFrom, yTo, step);
            System.out.println("Plotted function: " + function1);
        } catch (Exception e) {
            System.out.println("Error plotting function: " + e.getMessage());
            e.printStackTrace();
        }

        // Draw function f(x) = sin(x) with green color
        //turtle.color(0, 255, 0).width(1.0);
        //plotFunction(turtle, x -> Math.sin(x), xFrom, yFrom, viewWidth, viewHeight, 0.1);

        // Draw function f(x) = x³ - 2x with blue color
        //turtle.color(0, 0, 255).width(2.0);
        //plotFunction(turtle, x -> x * x * x - 2 * x, xFrom, yFrom, viewWidth, viewHeight, 0.1);

        turtle.write();
        // Turtle 1
        // Label Turtle 1

        Clerk.markdown("""
            Darunter befinden sich drei Buttons, die jeweils die Farbe der Turtle ändern. Die zu ersetzende Stelle im Quellcode ist durch das Label `// turtle color` markiert. Beim Klick auf einen Button wird
            dieser Teil des Codes automatisch angepasst.
            """);

        // Buttons
        Clerk.write(Interaction.button("Red", Interaction.eventFunction("./demo2.java", "// turtle color", "turtle.color(255, i * 256 / 37, i * 256 / 37, 1);")));
        Clerk.write(Interaction.button("Green", Interaction.eventFunction("./demo2.java", "// turtle color", "turtle.color(i * 256 / 37, 255, i * 256 / 37, 1);")));
        Clerk.write(Interaction.button("Blue", Interaction.eventFunction("./demo2.java", "// turtle color", "turtle.color(i * 256 / 37, i * 256 / 37, 255, 1);")));
        // Buttons

    }


    /**
     * Plots a mathematical function using a Turtle.
     *
     * @param turtle The Turtle instance to use for drawing.
     * @param functionExpression The mathematical function to plot, represented as a String.
     * @param xFromMath The starting x-coordinate in mathematical coordinates.
     * @param xToMath The ending x-coordinate in mathematical coordinates.
     * @param yFromMath The starting y-coordinate in mathematical coordinates.
     * @param yToMath The ending y-coordinate in mathematical coordinates.
     * @param step The step size for converting mathematical coordinates to pixel coordinates.
     * @return The Turtle instance after plotting the function (for method chaining).
     */
    public static Turtle plotFunction(Turtle turtle, String functionExpression,
                                      double xFromMath, double xToMath, double yFromMath, double yToMath,
                                      double step) {

        System.out.println("DEBUG: Entering plotFunction");
        turtle.push();

        boolean firstPoint = true;
        double prevXPixel = 0, prevYPixel = 0;
        //final double stepSize = Math.min(0.05, (xToMath - xFromMath) / 1000.0);
        final  double stepSize = 0.1;

        // Parse the function expression into an Expr object
        Expr function = null;
        try {
            System.out.println("DEBUG: About to parse expression: " + functionExpression);
            function = Parser.parse(functionExpression);
            System.out.println("DEBUG: Parsing successful");
            //System.out.println("function parsed: " +function);
        } catch (ParseException e) {
            System.out.println("Error parsing function expression: " + e.getMessage());
            return turtle; // Return the turtle without drawing if parsing fails
        } catch (Exception e) {
            System.out.println("Unexpected error while parsing function expression: " + e.getMessage());
            return turtle; // Return the turtle without drawing if an unexpected error occurs
        }
        System.out.println("Expression parsed successfully: " + function);

        //assert  function != null : "Function expression could not be null";
        //String variableName = extractVariableName(functionExpression);
        //System.out.println("Variable name " + variableName);

        System.out.println("Entering loop to plot function from " + xFromMath + " to " + xToMath);
        for (double xMath = xFromMath; xMath <= xToMath; xMath += stepSize) {
            try {

                // Berechne den Y-Wert der Funktion für den aktuellen X-Wert
                //Map<String, Double> variable = Map.of(variableName, xMath);
                Map<String, Double> variable = Map.of("x", xMath);
                double yMath = function.evaluate(function, variable);

                // Prüfen ob Y-Wert im sichtbaren Bereich liegt (coordonnées mathématiques)
                if (yMath >= yFromMath && yMath <= yToMath) {
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
     *
     * @param step Schrittweite für die Rasterlinien (z.B. 1.0 für jede Einheit)
     * @param showGrid wenn true, zeigt Rasterlinien an
     * @return turtle (um Methodenverkettung zu ermöglichen)
     */
    public static Turtle drawCartesianSystem(Turtle turtle, double xFromPixel, double xToPixel, double yFromPixel, double yToPixel, double viewWidth, double viewHeight, double step, boolean showGrid) {
        // Aktuellen Zustand speichern
        turtle.push();

        // Achsenfarbe (schwarz)
        turtle.color(0, 0, 0);

        // X-Achse zeichnen
        turtle.moveTo(xFromPixel, 0.0, xToPixel, 0.0, true);

        // Y-Achse zeichnen
        // Umrechnung der y-Werte in Pixel
        turtle.moveTo(0.0, yFromPixel, 0.0, yToPixel, true);

        if (showGrid) {
            // Rasterlinien zeichnen
            turtle.color(200, 200, 200).width(0.5); // Hellgrau, dünn

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
            turtle.color(200, 200, 200).width(0.5); // Zurück zu Hellgrau für Raster
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
        return turtle;
    }

    public static double[] parseInterval(String interval) {
        // Entferne die eckigen Klammern und teile den String in zwei Teile
        String cleaned = interval.replace("[", "").replace("]", "");
        String[] parts = cleaned.split(";");

        double min = Double.parseDouble(parts[0]);
        double max = Double.parseDouble(parts[1]);

        return new double[]{min, max};
    }

    public static String extractVariableName(String expression) {
        String variableName = "x";    // Standardvariable
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
                    variableName = tokenValue;
                }
            }
        }
        System.out.println("Extracted variable name: " + variableName);
        return variableName; // Fallback auf 'x', falls keine Variable gefunden wurde
    }

    public static double calculateStep(double xFrom, double xTo, double viewWidth) {
        // Berechne die Schrittweite basierend auf der Breite der Ansicht und dem Bereich der x-Werte
        double maxAbsX = Math.max(Math.abs(xFrom), Math.abs(xTo));

        double range = -xFrom == xTo ? xTo - xFrom : 2 * maxAbsX;   // Bereich der x-Werte
        double idealStep = viewWidth / range;

        if (viewWidth % idealStep == 0) {
            System.out.println("Step " + idealStep);
            return idealStep; // Wenn die Schrittweite genau passt, verwenden
        }

        // Ansonsten den Divisor finden, der am nächsten an der idealen Schrittweite liegt
        double closestDivisor = findClosestDivisor(viewWidth, idealStep);
        System.out.println("Step " + closestDivisor);
        return  closestDivisor;
    }

    /*
     * Findet den Divisor von 'number', der am nächsten an 'target' liegt.
     */
    private static double findClosestDivisor(double number, double target) {
        double closestDivisor = 1.0;
        double minDifference = Math.abs(target - 1.0);

        // alle Divisoren von 'number' durchgehen
        for (double divisor = 1.0; divisor <= number; divisor += 1.0) {
            if (number % divisor == 0) { // divisor est un diviseur de number
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
     * Zeichnet den abstrakten Syntaxbaum (AST) der Funktion.
     * 
     * @param expr Die zu visualisierende Expression
     */
    private static void zeichneSyntaxbaum(Expr expr) {
        Clerk.markdown("## Abstrakter Syntaxbaum (AST)");

        Dot dot = new Dot(600, 400);
        String dotCode = erstelleASTalsDot(expr);
        dot.draw(dotCode);
    }

    /**
     * Erstellt eine DOT-Sprache Repräsentation des AST.
     * 
     * @param expr Die Expression, die als DOT-Graph dargestellt werden soll
     * @return DOT-Sprache als String
     */
    private static String erstelleASTalsDot(Expr expr) {
        StringBuilder sb = new StringBuilder("digraph AST {\n");
        sb.append("  node [shape=box, style=filled, fillcolor=lightblue];\n");

        // Knoten-ID-Zähler
        int[] idCounter = {0};

        // Rekursive Funktion zum Aufbau des DOT-Codes
        erstelleASTKnoten(expr, idCounter, sb);

        sb.append("}");
        return sb.toString();
    }

    /**
     * Rekursive Hilfsmethode zum Erstellen der AST-Knoten im DOT-Format.
     * 
     * @param expr Die aktuelle Expression
     * @param idCounter Zähler für eindeutige IDs
     * @param sb StringBuilder für den DOT-Code
     * @return ID des erstellten Knotens
     */
    private static int erstelleASTKnoten(Expr expr, int[] idCounter, StringBuilder sb) {
        int currentId = idCounter[0]++;

        // Je nach Typ der Expression unterschiedliche Darstellung
        switch (expr) {
            case BinaryOp binaryOp -> {
                sb.append("  node").append(currentId).append(" [label=\"Operator: ").append(binaryOp.operator()).append("\"];\n");

                int leftId = erstelleASTKnoten(binaryOp.left(), idCounter, sb);
                int rightId = erstelleASTKnoten(binaryOp.right(), idCounter, sb);

                sb.append("  node").append(currentId).append(" -> node").append(leftId).append(" [label=\"links\"];\n");
                sb.append("  node").append(currentId).append(" -> node").append(rightId).append(" [label=\"rechts\"];\n");
            }
            case UnaryOp unaryOp -> {
                sb.append("  node").append(currentId).append(" [label=\"Unär: ").append(unaryOp.operator()).append("\"];\n");

                int operandId = erstelleASTKnoten(unaryOp.operand(), idCounter, sb);

                sb.append("  node").append(currentId).append(" -> node").append(operandId).append(" [label=\"operand\"];\n");
            }
            case Constant constant -> {
                sb.append("  node").append(currentId).append(" [label=\"Konstante: ").append(constant.value()).append("\", fillcolor=lightgreen];\n");
            }
            case Variable variable -> {
                sb.append("  node").append(currentId).append(" [label=\"Variable: ").append(variable.name()).append("\", fillcolor=lightyellow];\n");
            }
            case FunctionCall functionCall -> {
                sb.append("  node").append(currentId).append(" [label=\"Funktion: ").append(functionCall.functionName()).append("\", fillcolor=lightpink];\n");

                for (int i = 0; i < functionCall.arguments().size(); i++) {
                    int argId = erstelleASTKnoten(functionCall.arguments().get(i), idCounter, sb);
                    sb.append("  node").append(currentId).append(" -> node").append(argId).append(" [label=\"arg").append(i).append("\"];\n");
                }
            }
        }

        return currentId;
    }

    /**
     * Zeichnet die Funktion als Kurve.
     * 
     * @param expr Die auszuwertende Expression
     * @param xMin Minimaler x-Wert für den Darstellungsbereich
     * @param xMax Maximaler x-Wert für den Darstellungsbereich
     * @param farbe RGB-Farbwerte für die Funktionskurve
     */
    private static void zeichneFunktion(Expr expr, double xMin, double xMax, int[] farbe) {
        Clerk.markdown("## Funktionsgraph");

        // Turtle zum Zeichnen verwenden
        Turtle turtle = new Turtle(0, 0, 0, 600, 300, 0, 0);

        // Achsen zeichnen
        zeichneAchsen(turtle, xMin, xMax);

        // Funktion auswerten und zeichnen
        Map<String, Double> variables = new HashMap<>();
        double step = (xMax - xMin) / 400; // 400 Schritte für eine glatte Kurve

        // Ersten Punkt berechnen
        variables.put("x", xMin);
        double prevY = berechneY(expr, variables);

        // Variablen zum Speichern der vorherigen Bildschirmkoordinaten
        double prevScreenX = 0;
        double prevScreenY = 0;

        // Funktionsfarbe setzen
        turtle.color(farbe[0], farbe[1], farbe[2], 1);
        turtle.width(2.0); // Dickere Linie für die Funktion

        // Position für den ersten Punkt berechnen
        double screenX = mapToScreen(xMin, xMin, xMax, -300, 300);
        double screenY = mapToScreen(prevY, -10, 10, -150, 150);
        prevScreenX = screenX;
        prevScreenY = screenY;

        // Turtle zum Startpunkt bewegen ohne zu zeichnen
        turtle.penUp();
        turtle.push(); // Aktuelle Position speichern
        turtle.left(90); // Nach oben drehen
        turtle.forward(screenY); // Y-Position anpassen
        turtle.right(90); // Nach rechts drehen
        turtle.forward(screenX); // X-Position anpassen
        turtle.penDown();

        // Funktion zeichnen
        for (double x = xMin + step; x <= xMax; x += step) {
            variables.put("x", x);

            try {
                double y = berechneY(expr, variables);

                // Auf Bildschirmkoordinaten umrechnen
                screenX = mapToScreen(x, xMin, xMax, -300, 300);
                screenY = mapToScreen(y, -10, 10, -150, 150);

                // Von der aktuellen Position aus neu zeichnen
                // Stift anheben, damit wir uns bewegen können ohne zu zeichnen
                turtle.penUp();

                // Zur neuen Position bewegen
                turtle.push(); // Aktuelle Position speichern
                turtle.left(90); // Auf 0 Grad setzen
                turtle.right(90); // Nach rechts drehen
                turtle.forward(screenX); // X-Position anpassen
                turtle.left(90); // Nach oben drehen
                turtle.forward(screenY); // Y-Position anpassen

                // Stift wieder absetzen und zeichnen
                turtle.penDown();
                turtle.forward(0.1); // Minimale Bewegung zum Zeichnen eines Punktes
                turtle.pop(); // Zurück zur vorherigen Rotation

                // Aktuelle Position für nächsten Schritt speichern
                prevScreenX = screenX;
                prevScreenY = screenY;

                prevY = y;
            } catch (IllegalArgumentException e) {
                // Bei Berechnungsfehlern (z.B. Division durch Null) Stift anheben
                turtle.penUp();

                // Zum nächsten gültigen Punkt gehen
                x += step;
                if (x <= xMax) {
                    variables.put("x", x);
                    try {
                        prevY = berechneY(expr, variables);
                        screenX = mapToScreen(x, xMin, xMax, -300, 300);
                        screenY = mapToScreen(prevY, -10, 10, -150, 150);

                        // Zur neuen Position bewegen
                        turtle.push(); // Aktuelle Position speichern
                        turtle.left(90); // Auf 0 Grad setzen
                        turtle.right(90); // Nach rechts drehen
                        turtle.forward(screenX); // X-Position anpassen
                        turtle.left(90); // Nach oben drehen
                        turtle.forward(screenY); // Y-Position anpassen
                        turtle.pop(); // Zurück zur vorherigen Rotation

                        // Aktuelle Position für nächsten Schritt speichern
                        prevScreenX = screenX;
                        prevScreenY = screenY;

                        turtle.penDown();
                    } catch (IllegalArgumentException ex) {
                        // Weiter versuchen
                    }
                }
            }
        }

        // Zeichnung anzeigen mit Timeline-Slider
        turtle.write().timelineSlider();
    }

    /**
     * Zeichnet die Koordinatenachsen.
     * 
     * @param turtle Die Turtle zum Zeichnen
     * @param xMin Minimaler x-Wert
     * @param xMax Maximaler x-Wert
     */
    private static void zeichneAchsen(Turtle turtle, double xMin, double xMax) {
        // Achsenfarbe und -dicke
        turtle.color(150, 150, 150, 1);
        turtle.width(1.0);

        // X-Achse zeichnen
        turtle.penUp();
        turtle.push(); // Position speichern
        turtle.left(90); // Auf 0 Grad setzen
        turtle.right(90); // Nach rechts drehen
        turtle.forward(-300); // Zum Anfang der X-Achse
        turtle.penDown();
        turtle.forward(600); // X-Achse zeichnen (von -300 bis 300)
        turtle.pop(); // Zurück zur gespeicherten Position

        // Y-Achse zeichnen
        turtle.penUp();
        turtle.push(); // Position speichern
        turtle.left(90); // Auf 0 Grad setzen
        turtle.forward(-150); // Zum Anfang der Y-Achse
        turtle.penDown();
        turtle.forward(300); // Y-Achse zeichnen (von -150 bis 150)
        turtle.pop(); // Zurück zur gespeicherten Position

        // Markierungen auf X-Achse
        for (int i = (int)xMin; i <= (int)xMax; i++) {
            if (i == 0) continue; // Ursprung überspringen

            double screenX = mapToScreen(i, xMin, xMax, -300, 300);

            turtle.penUp();
            turtle.push(); // Position speichern
            turtle.left(90); // Auf 0 Grad setzen
            turtle.right(90); // Nach rechts drehen
            turtle.forward(screenX); // X-Position
            turtle.left(90); // Nach oben drehen
            turtle.forward(-5); // 5 Einheiten unter der X-Achse
            turtle.penDown();
            turtle.forward(10); // 10 Einheiten nach oben zeichnen
            turtle.pop(); // Zurück zur gespeicherten Position
        }

        // Markierungen auf Y-Achse
        for (int i = -10; i <= 10; i++) {
            if (i == 0) continue; // Ursprung überspringen

            double screenY = mapToScreen(i, -10, 10, -150, 150);

            turtle.penUp();
            turtle.push(); // Position speichern
            turtle.left(90); // Auf 0 Grad setzen
            turtle.right(90); // Nach rechts drehen
            turtle.forward(-5); // 5 Einheiten links der Y-Achse
            turtle.left(90); // Nach oben drehen
            turtle.forward(screenY); // Y-Position
            turtle.penDown();
            turtle.right(90); // Nach rechts drehen
            turtle.forward(10); // 10 Einheiten nach rechts zeichnen
            turtle.pop(); // Zurück zur gespeicherten Position
        }
    }

    /**
     * Berechnet den y-Wert der Funktion für einen gegebenen x-Wert.
     * 
     * @param expr Die auszuwertende Expression
     * @param variables Map mit den Variablenwerten
     * @return Berechneter y-Wert
     */
    private static double berechneY(Expr expr, Map<String, Double> variables) {
        return expr.evaluate(expr, variables);
    }

    /**
     * Transformiert einen Wert aus einem Bereich in einen anderen Bereich.
     * 
     * @param value Der zu transformierende Wert
     * @param fromMin Minimaler Eingabewert
     * @param fromMax Maximaler Eingabewert
     * @param toMin Minimaler Ausgabewert
     * @param toMax Maximaler Ausgabewert
     * @return Transformierter Wert
     */
    private static double mapToScreen(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return toMin + (value - fromMin) * (toMax - toMin) / (fromMax - fromMin);
    }

    /**
     * Zeigt interaktive Steuerelemente für den Funktionsplotter an.
     * 
     * @param funktionsAusdruck Der aktuelle Funktionsausdruck
     * @param xMin Minimaler x-Wert
     * @param xMax Maximaler x-Wert
     * @param farbe RGB-Farbwerte
     */
    private static void zeigeInteraktionsElemente(String funktionsAusdruck, double xMin, double xMax, int[] farbe) {
        Clerk.markdown("## Interaktive Steuerelemente");

        // Farbauswahl-Buttons
        Clerk.markdown("### Funktionsfarbe ändern");
        Clerk.write(Interaction.button("Rot", Interaction.eventFunction("src/main/java/lvp/functionPlotter/plotter/FunctionPlotter.java", 
                "// Standardfarbe für die Funktion", 
                "        // Standardfarbe für die Funktion\n        int[] farbe = {255, 0, 0}; // Rot (RGB)")));

        Clerk.write(Interaction.button("Grün", Interaction.eventFunction("src/main/java/lvp/functionPlotter/plotter/FunctionPlotter.java", 
                "// Standardfarbe für die Funktion", 
                "        // Standardfarbe für die Funktion\n        int[] farbe = {0, 255, 0}; // Grün (RGB)")));

        Clerk.write(Interaction.button("Blau", Interaction.eventFunction("src/main/java/lvp/functionPlotter/plotter/FunctionPlotter.java", 
                "// Standardfarbe für die Funktion", 
                "        // Standardfarbe für die Funktion\n        int[] farbe = {0, 0, 255}; // Blau (RGB)")));

        // Hinweise zur Bedienung
        Clerk.markdown("""
        ### Hinweise

        - **Funktion ändern**: Ändern Sie den Funktionsausdruck im Code.
        - **Zoom**: Sie können den Darstellungsbereich ändern, indem Sie die Werte für `xMin` und `xMax` im Code anpassen.
        - **Farbe**: Wählen Sie eine Farbe über die Buttons oder definieren Sie eigene RGB-Werte im Code.

        ### Unterstützte Funktionen

        - Grundrechenarten: `+`, `-`, `*`, `/`, `^` (Potenz)
        - Mathematische Funktionen: `sin`, `cos`, `tan`, `log`, `sqrt`, `abs`
        - Konstanten: `pi`, `e`
        """);
    }
}

