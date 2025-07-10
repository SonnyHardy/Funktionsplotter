import lvp.Clerk;
import lvp.skills.Text;
import lvp.skills.Interaction;
import lvp.views.Dot;
import lvp.views.Turtle;

import lvp.functionPlotter.ast.*;
import lvp.functionPlotter.parser.Parser;
import java.text.ParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


void main() {
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
    String interval = "[-10;10]";    // Beispielintervall

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

    String function1 = "x * x"; // Beispiel: f(x) = x^2
    String function2 = "x * x * x - 2 * x"; // Beispiel: f(x) = x³ - 2x
    String function3 = "cos(x)"; // Beispiel: f(x) = cos(x)

    try {
        turtle.color(255, 0, 0).width(1.0);  // Rot für die Funktion 1
        plotFunction(turtle, function1, xFrom, xTo, yFrom, yTo, step);

        turtle.color(0, 0, 255).width(1.0);    // Blau für die Funktion 2
        plotFunction(turtle, function2, xFrom, xTo, yFrom, yTo, step);

        turtle.color(0, 255, 0).width(1.0); // Grün für die Funktion 3
        plotFunction(turtle, function3, xFrom, xTo, yFrom, yTo, step);
    } catch (Exception e) {
        System.out.println("Error plotting function: " + e.getMessage());
        e.printStackTrace();
    }

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

    // AST Visualization Demo
    Clerk.markdown(Text.fillOut("""
    ## AST-Visualisierung
    Die folgende Visualisierung zeigt den abstrakten Syntaxbaum (AST), der aus einem mathematischen Ausdruck generiert wurde.
    Jeder Knoten im Baum ist nach seinem Typ eingefärbt:
    - **Grün**: Variablen
    - **Blau**: Konstanten
    - **Rot**: Binäre Operatoren
    - **Orange**: Unäre Operatoren
    - **Violett**: Funktionen

    ```java
    ${0}
    ```
    """, Text.codeBlock("./demo2.java", "// AST Drawing")));

    // AST Drawing
    String expression = "-sin(x) + 2 * (x^2 - 1)"; // Expression Example
    Clerk.write(Interaction.input("./demo2.java", "// Expression Example", "String expression = \"$\";", "Geben Sie einen mathematischen Ausdruck ein"));
    drawExpressionAST(expression);
    // AST Drawing
}


/**
 * Zeichnet eine mathematische Funktion mit einer Turtle.
 *
 * @param turtle Die Turtle-Instanz für das Zeichnen.
 * @param functionExpression Die mathematische Funktion zum Zeichnen, als String dargestellt.
 * @param xFromMath Die Start-X-Koordinate in mathematischen Koordinaten.
 * @param xToMath Die End-X-Koordinate in mathematischen Koordinaten.
 * @param yFromMath Die Start-Y-Koordinate in mathematischen Koordinaten.
 * @param yToMath Die End-Y-Koordinate in mathematischen Koordinaten.
 * @param step Die Schrittgröße für die Umrechnung von mathematischen Koordinaten in Pixelkoordinaten.
 * @return Die Turtle-Instanz nach dem Zeichnen der Funktion (für Methodenverkettung).
 */
public Turtle plotFunction(Turtle turtle, String functionExpression,
                           double xFromMath, double xToMath, double yFromMath, double yToMath,
                           double step) {

    turtle.push();

    boolean firstPoint = true;
    double prevXPixel = 0, prevYPixel = 0;
    final double stepSize = Math.min(0.05, (xToMath - xFromMath) / 1000.0);
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

    assert  function != null : "Funktionsausdruck darf nicht null sein";
    String variableName = extractVariableName(functionExpression);

    for (double xMath = xFromMath; xMath <= xToMath; xMath += stepSize) {
        try {

            // Berechne den Y-Wert der Funktion für den aktuellen X-Wert
            Map<String, Double> variable = Map.of(variableName, xMath);
            double yMath = function.evaluate(function, variable);

            // Prüfen ob Y-Wert im sichtbaren Bereich liegt (mathematische Koordinaten)
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
public Turtle drawCartesianSystem(Turtle turtle, double xFromPixel, double xToPixel, double yFromPixel, double yToPixel, double viewWidth, double viewHeight, double step, boolean showGrid) {
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
    return turtle;
}

public double[] parseInterval(String interval) {
    // Entferne die eckigen Klammern und teile den String in zwei Teile
    String cleaned = interval.replace("[", "").replace("]", "");
    String[] parts = cleaned.split(";");

    double min = Double.parseDouble(parts[0]);
    double max = Double.parseDouble(parts[1]);

    return new double[]{min, max};
}

public String extractVariableName(String expression) {
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

public double calculateStep(double xFrom, double xTo, double viewWidth) {
    // Berechne die Schrittweite basierend auf der Breite der Ansicht und dem Bereich der x-Werte
    double maxAbsX = Math.max(Math.abs(xFrom), Math.abs(xTo));

    double range = -xFrom == xTo ? xTo - xFrom : 2 * maxAbsX;   // Bereich der x-Werte
    double idealStep = viewWidth / range;

    if (viewWidth % idealStep == 0) {
        return idealStep; // Wenn die Schrittweite genau passt, verwenden
    }

    // Ansonsten den Divisor finden, der am nächsten an der idealen Schrittweite liegt
    double closestDivisor = findClosestDivisor(viewWidth, idealStep);
    return  closestDivisor;
}

/*
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
 * Zeichnet den abstrakten Syntaxbaum (AST) eines mathematischen Ausdrucks.
 * Verwendet die umgekehrte polnische Notation (RPN) und die Dot-Bibliothek, um einen visuellen Graphen zu erzeugen.
 *
 * @param expression Der zu analysierende und zu visualisierende mathematische Ausdruck
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
 * Erzeugt eine DOT-Darstellung (für Graphviz) des abstrakten Syntaxbaums.
 * Verwendet verschiedene Farben, um die Knotentypen zu unterscheiden:
 * - Grün: Variablen
 * - Blau: Konstanten
 * - Rot: Binäre Operatoren
 * - Orange: Unäre Operatoren
 * - Violett: Funktionen
 *
 * @param ast Der darzustellende abstrakte Syntaxbaum
 * @return Der String mit dem DOT-Graphen
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
 *
 * @param expr Der aktuelle Ausdrucksknoten
 * @param dotBuilder Der StringBuilder zum Erstellen des DOT-Strings
 * @param counter Zähler zum Generieren eindeutiger Knoten-IDs
 * @param parentId Die ID des übergeordneten Knotens (null für den Wurzelknoten)
 * @param edgeLabel Beschriftung für die Kante vom übergeordneten Knoten (null für den Wurzelknoten)
 * @return Die ID des erstellten Knotens
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
