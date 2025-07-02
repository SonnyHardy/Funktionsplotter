//package lvp.functionPlotter.plotter;

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
    public static void main(String[] args) {
        Clerk.clear();

        // Funktionsausdruck, der vom Benutzer bearbeitet werden kann
        String funktionsAusdruck = "sin(x) * (x^2 / 10)";

        // Standardwerte für den Darstellungsbereich
        double xMin = -10.0;
        double xMax = 10.0;

        // Standardfarbe für die Funktion
        int[] farbe = {0, 0, 255}; // Blau (RGB)

        // Markdown-Titel und Beschreibung
        Clerk.markdown(Text.fillOut("""            
        # Funktionsplotter

        Mit diesem interaktiven Tool können Sie mathematische Funktionen visualisieren 
        und deren abstrakten Syntaxbaum (AST) anzeigen lassen.

        ## Aktuelle Funktion: `${0}`

        Sie können den Funktionsausdruck, Farbe und Darstellungsbereich im Code anpassen.
        """, funktionsAusdruck));

        // Versuche die Funktion zu parsen und zu visualisieren
        try {
            // Parse den Funktionsausdruck
            Expr expr = Parser.parse(funktionsAusdruck);

            // Zeichne den Syntaxbaum
            zeichneSyntaxbaum(expr);

            // Zeichne die Funktion
            zeichneFunktion(expr, xMin, xMax, farbe);

            // Steuerelemente für den Plotter anzeigen
            zeigeInteraktionsElemente(funktionsAusdruck, xMin, xMax, farbe);

        } catch (ParseException e) {
            Clerk.markdown("### Fehler beim Parsen des Ausdrucks: " + e.getMessage());
        }
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

        // Turtle mit angepasstem Koordinatensystem
        // xFrom=-300, xTo=300, yFrom=-200, yTo=200 für bessere Darstellung
        Turtle turtle = new Turtle(-300, 300, -200, 200, 0, 0, 0);

        // Achsen zeichnen
        zeichneAchsen(turtle, xMin, xMax);

        // Funktion auswerten und zeichnen
        zeichneFunktionskurve(turtle, expr, xMin, xMax, farbe);

        // Zeichnung anzeigen mit Timeline-Slider
        turtle.write().timelineSlider();
    }

    /**
     * Zeichnet die Funktionskurve mit der Turtle.
     */
    private static void zeichneFunktionskurve(Turtle turtle, Expr expr, double xMin, double xMax, int[] farbe) {
        Map<String, Double> variables = new HashMap<>();
        double step = (xMax - xMin) / 500; // 500 Schritte für eine glatte Kurve

        // Funktionsfarbe setzen
        turtle.color(farbe[0], farbe[1], farbe[2], 1.0);
        turtle.width(2.0);

        boolean penIsDown = false;
        double currentX = 0;
        double currentY = 0;

        // Funktion zeichnen
        for (double x = xMin; x <= xMax; x += step) {
            variables.put("x", x);

            try {
                double y = berechneY(expr, variables);

                // Begrenze Y-Werte für bessere Darstellung
                if (Double.isNaN(y) || Double.isInfinite(y) || Math.abs(y) > 15) {
                    if (penIsDown) {
                        turtle.penUp();
                        penIsDown = false;
                    }
                    continue;
                }

                // Auf Turtle-Koordinaten umrechnen
                double turtleX = mapToScreen(x, xMin, xMax, -280, 280);
                double turtleY = mapToScreen(y, -10, 10, -180, 180);

                // Zur Position bewegen
                moveToPosition(turtle, currentX, currentY, turtleX, turtleY);
                currentX = turtleX;
                currentY = turtleY;

                // Stift absetzen wenn noch nicht geschehen
                if (!penIsDown) {
                    turtle.penDown();
                    penIsDown = true;
                }

            } catch (IllegalArgumentException e) {
                // Bei Berechnungsfehlern Stift anheben
                if (penIsDown) {
                    turtle.penUp();
                    penIsDown = false;
                }
            }
        }
    }

    /**
     * Bewegt die Turtle von der aktuellen Position zu einer neuen Position.
     * Diese Methode berechnet den benötigten Winkel und die Distanz.
     */
    private static void moveToPosition(Turtle turtle, double fromX, double fromY, double toX, double toY) {
        // Distanz berechnen
        double dx = toX - fromX;
        double dy = toY - fromY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.1) {
            return; // Zu kleine Bewegung, überspringen
        }

        // Zielwinkel berechnen (in Grad)
        double targetAngle = Math.toDegrees(Math.atan2(dy, dx));

        // Turtle in Richtung des Ziels drehen
        // Annahme: Turtle startet bei 0° (nach rechts)
        double currentAngle = 0; // Vereinfachung - sollte eigentlich getrackt werden
        double angleDiff = targetAngle - currentAngle;

        // Winkel normalisieren
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        if (angleDiff > 0) {
            turtle.left(angleDiff);
        } else {
            turtle.right(-angleDiff);
        }

        // Zur neuen Position bewegen
        turtle.forward(distance);
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
        turtle.color(128, 128, 128, 0.8);
        turtle.width(1.0);

        // X-Achse zeichnen
        turtle.penUp();
        // Zur linken Seite der X-Achse bewegen
        moveToPosition(turtle, 0, 0, -280, 0);
        turtle.penDown();
        turtle.forward(560); // X-Achse zeichnen (von -280 bis 280)

        // Y-Achse zeichnen
        turtle.penUp();
        // Zur unteren Seite der Y-Achse bewegen
        moveToPosition(turtle, turtle.toString().contains("x1=\"280\"") ? 280 : 0, 0, 0, -180);
        turtle.penDown();
        // Turtle nach oben ausrichten und Y-Achse zeichnen
        turtle.left(90);
        turtle.forward(360); // Y-Achse zeichnen (von -180 bis 180)

        // Markierungen auf den Achsen
        zeichneAchsenMarkierungen(turtle, xMin, xMax);
    }

    /**
     * Zeichnet die Markierungen auf den Achsen.
     */
    private static void zeichneAchsenMarkierungen(Turtle turtle, double xMin, double xMax) {
        // Markierungen auf X-Achse
        for (int i = (int)Math.ceil(xMin); i <= (int)Math.floor(xMax); i++) {
            if (i == 0) continue; // Ursprung überspringen

            double screenX = mapToScreen(i, xMin, xMax, -280, 280);

            turtle.penUp();
            moveToPosition(turtle, 0, 180, screenX, 0); // Zur X-Achse
            turtle.left(90); // Nach oben ausrichten
            turtle.penDown();
            turtle.backward(5);
            turtle.forward(10); // Markierung zeichnen
        }

        // Markierungen auf Y-Achse
        for (int i = -10; i <= 10; i++) {
            if (i == 0) continue; // Ursprung überspringen

            double screenY = mapToScreen(i, -10, 10, -180, 180);

            turtle.penUp();
            moveToPosition(turtle, 0, 5, 0, screenY); // Zur Y-Achse
            turtle.left(0); // Nach rechts ausrichten
            turtle.penDown();
            turtle.backward(5);
            turtle.forward(10); // Markierung zeichnen
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
        Clerk.write(Interaction.button("Rot", Interaction.eventFunction("FunctionPlotter.java",
                "// Standardfarbe für die Funktion",
                "        // Standardfarbe für die Funktion\n        int[] farbe = {255, 0, 0}; // Rot (RGB)")));

        Clerk.write(Interaction.button("Grün", Interaction.eventFunction("FunctionPlotter.java",
                "// Standardfarbe für die Funktion",
                "        // Standardfarbe für die Funktion\n        int[] farbe = {0, 255, 0}; // Grün (RGB)")));

        Clerk.write(Interaction.button("Blau", Interaction.eventFunction("FunctionPlotter.java",
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
        
        ### Beispiele für Funktionen
        
        - `sin(x)` - Sinus-Funktion
        - `x^2` - Parabel
        - `sin(x) * cos(x)` - Produkt von Sinus und Cosinus
        - `log(abs(x))` - Logarithmus des Absolutwerts
        - `sqrt(abs(x))` - Quadratwurzel des Absolutwerts
        """);
    }
}