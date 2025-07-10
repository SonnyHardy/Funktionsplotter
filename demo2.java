import lvp.Clerk;
import lvp.skills.Text;
import lvp.skills.Interaction;
import lvp.functionPlotter.plotter.FunctionPlotter;


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
    String interval = "[-10;10]";    // Beispielintervall

    // FunctionPlotter mit dem Interval initialisieren
    FunctionPlotter plotter = new FunctionPlotter(interval);

    // Funktionsausdrücke definieren
    String function1 = "exp(x) + e"; // Beispiel: f(x) = e^x + e
    String function2 = "x * x * x - 2 * x"; // Beispiel: f(x) = x³ - 2x
    String function3 = "cos(x)"; // Beispiel: f(x) = cos(x)

    // AST für jede Funktion anzeigen
    plotter.drawExpressionAST(function1);
    plotter.drawExpressionAST(function2);
    plotter.drawExpressionAST(function3);

    // Funktionen mit unterschiedlichen Farben zeichnen
    plotter.plotFunction(function1, 255, 0, 0);  // Rot für die Funktion 1
    plotter.plotFunction(function2, 0, 0, 255);  // Blau für die Funktion 2
    plotter.plotFunction(function3, 0, 255, 0);  // Grün für die Funktion 3

    // Turtle-Ansicht ausgeben
    plotter.writeTurtle();
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
    plotter.drawExpressionAST(expression);
    // AST Drawing
}
