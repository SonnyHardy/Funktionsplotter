# Funktionsplotter - Interaktive Funktionsvisualisierung

## Projektbeschreibung
Der **Funktionsplotter** ist ein leistungsstarkes Werkzeug zur Analyse und Visualisierung mathematischer Funktionen. Es ermöglicht die Eingabe, Auswertung und grafische Darstellung von mathematischen Ausdrücken, sowie die Visualisierung ihrer abstrakten Syntaxbäume (AST).

## Implementierte Funktionen

- **Ausdruckseingabe**: Funktionen können in Infix-Notation eingegeben werden (z.B. `x^2 + 3*x`) und RPN-Notation (z.B. `x 2 ^ 3 * +`)
- **Taschenrechnermodus**: Sofortige Auswertung von Ausdrücken ohne Variablen
- **AST-Generierung**: Erzeugung eines abstrakten Syntaxbaums für jeden Ausdruck
- **AST-Visualisierung**: Darstellung des AST mittels DOT/Graphviz
- **Funktionsdarstellung**: Zeichnung von Funktionen in einem kartesischen Koordinatensystem
- **Umfangreiche Unterstützung mathematischer Operationen**: `+`, `-`, `*`, `/`, `^`, `sqrt`, `log`, `ln`, `sin`, `cos`, `tan`, `pi`, `π`, `e` usw.


- **Bedingte Ausdrücke (`Erweiterung`)**: Unterstützung von bedingten Ausdrücken (z.B. `x <= 0 ? 0 : x * x`)
- **Mehrfarbige Darstellung (`Erweiterung`)**: Verschiedene Funktionen können in unterschiedlichen Farben dargestellt werden

## Geplante Erweiterungen (noch nicht implementiert)

- **Interaktives Zoomen/Verschieben**: dynamische Anpassung des sichtbaren Bereichs im Koordinatensystem
- **Parametrische Funktionen**: Unterstützung von Funktionen mit anpassbaren Parametern über Schieberegler
- **Logarithmische/Lineare Achsen**: Auswählbare Achsenskalierung

## Projektstruktur

- `demo.java` - Allgemeine Demo der LVP-Funktionalitäten
- `Ausdruck.pdf` - ein Ausdruck der durch das Programm erzeugten LVP-Ansicht im Browser
- `src/main/java/lvp/functionPlotter/` - Hauptimplementierung des Funktionsplotters
  - `plotter/` - Zeichnungsfunktionalität
  - `ast/` - Abstrakte Syntaxbaum-Implementierung
  - `parser/` - Parser für mathematische Ausdrücke

## Installation und Ausführung

Dieses Projekt verwendet LVP (Live View Programming) als interaktive Laufzeitumgebung.

### Voraussetzungen

- Java SDK 24 oder höher
- LVP-Bibliothek (`lvp-0.5.4.jar` oder neuer)

### Ausführung

Um das Projekt zu starten, nutzen Sie den folgenden Befehl im Hauptverzeichnis des Projekts:

```bash
java -jar lvp-0.5.4.jar --log --watch=demo.java
```

Die `--log`-Option aktiviert die Protokollierung für Debug-Zwecke, während `--watch` das automatische Neuladen bei Dateiänderungen ermöglicht.

## Nutzung

Nach dem Start öffnet sich ein Browserfenster mit der interaktiven Anwendung. In der Funktionsplotter-Demo können Sie:

1. Das Darstellungsintervall ändern
2. Eigene mathematische Funktionen eingeben
3. Die generierten ASTs betrachten
4. Die resultierenden Funktionsgraphen in verschiedenen Farben ansehen

## Projektbeispiel

```java
// Funktionsplotter mit dem Intervall [-10;10] initialisieren
FunctionPlotter plotter = new FunctionPlotter("[-10;10]");

// Funktionen definieren
String f = "exp(x) + e";
String g = "x * x * x - 2 * x";
String h = "cos(x)";
String j = "x <= 0 ? 0 : x*x"; // Bedingte Funktion

// AST visualisieren
plotter.drawExpressionAST(f);
plotter.drawExpressionAST(j); // AST der bedingten Funktion

// Funktionen zeichnen (rot, blau, grün, lila)
plotter.plotFunction(f, 255, 0, 0);
plotter.plotFunction(g, 0, 0, 255);
plotter.plotFunction(h, 0, 255, 0);
plotter.plotFunction(j, 128, 0, 128); // Lila für bedingte Funktion

// Grafik ausgeben
plotter.writeTurtle();
```