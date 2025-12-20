# Christmas-Indy-Trail
Weihnachts Indy Trail – Aufgabenstellung & Pflichtenheft (Android)
1. Ziel des Projekts

Entwicklung einer Android-App für einen Indoor-Weihnachts-Trail, bei dem Nutzer per QR-Code-Scan nacheinander Posten freischalten.
Jeder Posten besteht primär aus Bilderrätseln, optional ergänzt durch Titel und kurzen Text.
Das Look & Feel soll an das bestehende IndyTrail-Projekt angelehnt sein („Ancient Tech“, dunkel, mystisch), während die Funktionalität neu und bewusst schlank aufgebaut wird.

2. Grundlogik / Flow (Strict Mode)

Hauptloop (nicht unterbrechbar):

QR scannen → Posten anzeigen → (optional Hilfe) → QR scannen → nächster Posten

Regeln

Strict Reihenfolge:
Posten müssen in der definierten Reihenfolge (01 → 02 → 03 → …) gescannt werden.

Wird ein falscher QR-Code gescannt:

Anzeige einer kurzen Meldung:
„Das ist noch nicht der richtige Posten.“

Keine Fortschrittsänderung.

Fortschritt wird lokal gespeichert (App-Neustart soll keinen Reset verursachen).

3. QR-Code Format

Schema + ID (Pflicht):

indypath://posten/01
indypath://posten/02


Die App parst:

schema = indypath

type = posten

id = 01

IDs müssen mit der Ordnerstruktur übereinstimmen.

4. Posten-Inhalt & Datenstruktur
Anzahl

5–6 Posten

ca. 3 Bilder pro Posten

Inhalte

Titel → optional

Text → optional

Bilder → Pflicht (mindestens 1)

5. Ordnerstruktur (App-gesteuert)

Die Ordnerstruktur bestimmt den Trail.

Empfohlene Struktur (intern im App-Speicher):

trail/
  weihnachten_2025/
    manifest.json
    01/
      meta.json
      hint_1.jpg
      hint_2.jpg
      hint_3.jpg
    02/
      meta.json
      hint_1.jpg
      hint_2.jpg

meta.json (optional Felder)
{
  "title": "Posten 1",
  "text": "Optionaler Hinweistext",
  "hints": [
    "hint_1.jpg",
    "hint_2.jpg",
    "hint_3.jpg"
  ]
}


Wenn title oder text fehlt → UI blendet das Feld nicht ein.

Reihenfolge der Bilder ergibt sich aus hints.

6. Bildanzeige & Hilfe-Logik
Standard

Beim Öffnen eines Postens:

Nur erstes Bild sichtbar

Button:

„Mehr Hilfe“

blendet jeweils ein weiteres Bild ein

bis alle Bilder sichtbar sind

Anzeige

Bilder:

zentriert

mit leichtem Rahmen (Cyan, transparent)

skalieren proportional (kein Verzerren)

7. Look & Feel Anforderungen (verbindlich)
Farbwelt

Dunkler Hintergrund (Obsidian / fast schwarz)

Akzentfarben:

Cyan / Blau → Primäraktionen, Rahmen, Scanner

Gold → Scan-Linie, aktive Akzente

Stil

„Ancient Tech / Archäologie + Technologie“

Ruhig, nicht verspielt

Keine hellen Hintergründe

Typografie

Sans-Serif

Überschriften mit leichtem Letter-Spacing

Text gut lesbar, kein Runenfont für normalen Text

8. QR-Scanner Screen (Key Design)

Pflichtmerkmale:

Vollbild-Kamera

Dunkles Overlay außerhalb des Scan-Fensters

Zentraler Scan-Rahmen:

Cyan

leicht pulsierend

Scan-Linie:

Gold

vertikale Bewegung

Oben:

dezente Top-Bar mit Titel

Immersiv (Statusleiste möglichst ausgeblendet)

9. Versteckter Reset / Debug

Reset-Funktion vorhanden, aber:

nicht prominent sichtbar

z.B.:

langer Druck auf Titel

5× Tippen auf Logo

oder nur über verstecktes Menü

Reset bewirkt:

Fortschritt zurück auf Posten 01

alle Hilfen zurückgesetzt

10. Sprache

Deutsch

Keine Mehrsprachigkeit nötig

11. Technische Leitplanken

Zielplattform: Android Smartphone

Offlinefähig

Keine Accounts

Kein Internet notwendig

Moderne Architektur (z.B. Jetpack Compose empfohlen, aber nicht zwingend vorgeschrieben)

12. Abnahmekriterien (Acceptance Criteria)

✔ QR-Code Scan funktioniert zuverlässig
✔ Falsche QR-Codes werden korrekt abgefangen
✔ Posten erscheinen nur in richtiger Reihenfolge
✔ Titel/Text werden nur angezeigt, wenn vorhanden
✔ „Mehr Hilfe“ blendet Bilder stufenweise ein
✔ Fortschritt bleibt nach App-Neustart erhalten
✔ Reset funktioniert, ist aber versteckt
✔ Look & Feel entspricht dunklem IndyTrail-Stil

13. Milestones (empfohlen)

M1 – Core

QR-Scanner

Strict Flow

1 Posten laden & anzeigen

M2 – Content

Ordnerstruktur lesen

Bilder + optionale Texte

Hilfe-Logik

M3 – Polish

Animationen

Look & Feel

Reset
