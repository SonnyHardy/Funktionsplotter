import lvp.Clerk;
import lvp.skills.Text;
import lvp.skills.Interaction;
import lvp.functionPlotter.plotter.FunctionPlotter;


void main() {
    Clerk.clear();
    // Markdown 1
    Clerk.markdown(Text.fillOut("""
    # Funktionsplotter - Interaktive Funktionsvisualisierung

    ## Projektübersicht
    Der **Funktionsplotter** ist ein leistungsstarkes Werkzeug zur Analyse und Visualisierung mathematischer Funktionen. Er ermöglicht es, mathematische Ausdrücke einzugeben, sie zu analysieren und grafisch darzustellen. Das Projekt bietet folgende Hauptfunktionen:

    - Auswertung mathematischer Ausdrücke
    - Generierung und Visualisierung abstrakter Syntaxbäume (AST)
    - Grafische Darstellung von Funktionen in einem kartesischen Koordinatensystem
    - Unterstützung verschiedener mathematischer Operationen und Funktionen

    Diese interaktive Dokumentation demonstriert die Funktionalitäten des Projekts und gibt Einblicke in die Implementation.

    ## Funktionalitäten

    ### Implementierte Funktionen

    - **Ausdruckseingabe**: Funktionen können in Infix-Notation eingegeben werden (z.B. `x^2 + 3*x`) und RPN-Notation (z.B. `x 2 ^ 3 * +`)
    - **Taschenrechnermodus**: Sofortige Auswertung von Ausdrücken ohne Variablen
    - **AST-Generierung**: Erzeugung eines abstrakten Syntaxbaums für jeden Ausdruck
    - **AST-Visualisierung**: Darstellung des AST mittels DOT/Graphviz
    - **Funktionsdarstellung**: Zeichnung von Funktionen in einem kartesischen Koordinatensystem
    - **Mehrfarbige Darstellung**: Verschiedene Funktionen können in unterschiedlichen Farben dargestellt werden
    - **Umfangreiche Unterstützung mathematischer Operationen**: `+`, `-`, `*`, `/`, `^`, `sqrt`, `log`, `ln`, `sin`, `cos`, `tan`, `pi`, `π`, `e` usw.

    ### Geplante Funktionalitäten

    - **Interaktives Zoomen/Verschieben**: Dynamische Anpassung des sichtbaren Bereichs im Koordinatensystem
    - **Parametrische Funktionen**: Unterstützung von Funktionen mit anpassbaren Parametern über Schieberegler
    - **Mehrfache Funktionseingabe**: Gleichzeitige Eingabe mehrerer Funktionen über die Benutzeroberfläche
    - **Logarithmische/Lineare Achsen**: Auswählbare Achsenskalierung
    - **Bedingte Funktionen**: Unterstützung von Ausdrücken wie `x <= 0 ? 0 : 1`

    ## Beispiel: Funktionsvisualisierung

    Der folgende Code demonstriert die grundlegende Verwendung des Funktionsplotters:

    ```java
    ${0}
    ```

    Im obigen Beispiel wird zunächst ein Funktionsplotter mit dem Intervall `[-10;10]` initialisiert. Anschließend werden drei mathematische Funktionen definiert:
    1. `exp(x) + e` - Die Exponentialfunktion plus der Eulerschen Zahl
    2. `x * x * x - 2 * x` - Eine kubische Funktion
    3. `cos(x)` - Die Kosinus-Funktion

    Für jede dieser Funktionen wird zuerst der abstrakte Syntaxbaum (AST) visualisiert, der die interne Struktur des mathematischen Ausdrucks zeigt. Danach werden die Funktionen im kartesischen Koordinatensystem gezeichnet, wobei jede Funktion eine eigene Farbe erhält (Rot, Blau und Grün).

    Am Ende wird die fertige Visualisierung ausgegeben.
    """, Text.codeBlock("./demo.java", "// Function Plotter Example")));
    // Markdown 1

    // Function Plotter Example
    // Input-Felder für Funktionen und Intervall
    Clerk.markdown("### Eingabeparameter anpassen");

    // Variablen für die Eingabeparameter deklarieren
    String interval = "[-10;10]";
    String fx = "exp(x) + e";
    String gx = "x * x * x - 2 * x";
    String hx = "cos(x)";

    // Eingabefelder für Parameter ohne Aktualisierung
    Clerk.markdown("**Intervall:** (Format: [min;max])");
    Clerk.write(Interaction.input("./demo.java", "// Interval Update", "interval = \"$\";", interval));
    interval = "[-10;10]"; // Interval Update

    Clerk.markdown("**Funktionen eingeben:**");

    Clerk.markdown("Funktion f(x) - <span style='color:red'>rot</span>:");
    Clerk.write(Interaction.input("./demo.java", "// fx Update", "fx = \"$\";", fx));
    fx = "exp(x) - x^2"; // fx Update

    Clerk.markdown("Funktion g(x) - <span style='color:blue'>blau</span>:");
    Clerk.write(Interaction.input("./demo.java", "// gx Update", "gx = \"$\";", gx));
    // gx Update

    Clerk.markdown("Funktion h(x) - <span style='color:green'>grün</span>:");
    Clerk.write(Interaction.input("./demo.java", "// hx Update", "hx = \"$\";", hx));
    // hx Update

    // FunctionPlotter mit dem Intervall initialisieren
    FunctionPlotter plotter = new FunctionPlotter(interval);

    // AST (Abstrakter Syntaxbaum) für jede Funktion visualisieren
    Clerk.markdown("### Abstrakte Syntaxbäume (AST)");

    Clerk.markdown("**f(x) = " + fx + "**");
    plotter.drawExpressionAST(fx);    // Zeigt die interne Struktur des Ausdrucks

    Clerk.markdown("**g(x) = " + gx + "**");
    plotter.drawExpressionAST(gx);

    Clerk.markdown("**h(x) = " + hx + "**");
    plotter.drawExpressionAST(hx);

    // Funktionen mit unterschiedlichen Farben im Koordinatensystem zeichnen
    Clerk.markdown("### Funktionsgraphen");
    plotter.plotFunction(fx, 255, 0, 0);  // Rot für die Funktion f(x)
    plotter.plotFunction(gx, 0, 0, 255);  // Blau für die Funktion g(x)
    plotter.plotFunction(hx, 0, 255, 0);  // Grün für die Funktion h(x)

    // Ergebnis als SVG-Grafik ausgeben
    plotter.writeTurtle();
    // Function Plotter Example

}
