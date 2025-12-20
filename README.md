# Christmas-Indy-Trail

## Kurzbeschreibung
Indoor-Weihnachts-Trail als Android-App: Nutzer schalten Posten nur in der vorgegebenen Reihenfolge frei, indem sie QR-Codes scannen. Jeder Posten zeigt Bildraetsel (Pflicht) sowie optional Titel und kurzen Text. Look & Feel: dunkel, ruhig, "Ancient Tech" mit Cyan- und Gold-Akzenten.

## Kern-Flow (Strict Mode)
- Ablauf: QR scannen -> Posten anzeigen -> (optional) Mehr Hilfe -> naechsten QR scannen.
- Reihenfolge ist strikt (01 -> 02 -> 03 ...). Falscher QR-Code: kurze Meldung, kein Fortschritt.
- Fortschritt wird lokal gespeichert (App-Neustart behaelt den Stand).
- Versteckter Reset: langer Druck auf den App-Titel setzt Fortschritt auf Posten 01 zurueck.

## QR-Code Format
- Schema: `indypath://posten/{id}` (z. B. `indypath://posten/01`).
- Parser: `schema = indypath`, `type = posten`, `id = zweistellig`.
- IDs muessen mit den Ordnernamen uebereinstimmen.

## Datenstruktur der Trail-Inhalte
Im App-Speicher (wird beim ersten Start aus Assets kopiert):
```
trail/
  weihnachten_2025/
    manifest.json
    01/
      meta.json
      01_Cabinet.png
      02_Birds.png
      03_Bathroom.png
    02/
      meta.json
      ...
    ...
```
`manifest.json` (vereinfacht):
```
{
  "id": "weihnachten_2025",
  "title": "Weihnachts Indy Trail",
  "description": "Indoor-Weihnachts-Trail mit sechs Posten in fester Reihenfolge.",
  "posts": [
    { "id": "01", "folder": "01" },
    ...
  ]
}
```
`meta.json` pro Posten:
```
{
  "title": "Posten 1",
  "text": "Optionaler Hinweistext",
  "hints": [
    "01_Cabinet.png",
    "02_Birds.png",
    "03_Bathroom.png"
  ]
}
```
- Titel/Text optional; wenn leer, blendet die UI das Feld aus.
- Reihenfolge der Bilder ergibt sich aus `hints`.

## Hilfe-Logik & Anzeige
- Beim Oeffnen eines Postens: nur das erste Bild sichtbar.
- Button "Mehr Hilfe" blendet je ein weiteres Bild ein, bis alle sichtbar sind.
- Bilder: zentriert, proportional skaliert, dezenter cyanfarbener Rahmen.

## Look & Feel
- Hintergrund: Obsidian/fast schwarz.
- Akzente: Cyan (Rahmen, Buttons, Scanner) und Gold (Scan-Linie, aktive Akzente).
- Stil: ruhig, archaeologisch-technisch; keine hellen Hintergruende; Sans-Serif mit leichtem Letter-Spacing.

## QR-Scanner Screen
- Vollbild-Kamera mit dunklem Overlay ausserhalb des Scan-Fensters.
- Zentraler Scan-Rahmen (Cyan, pulsierend) und vertikale, goldene Scan-Linie.
- Dezente Top-Bar mit Titel; Statusleiste moeglichst ausgeblendet.

## Reset / Debug
- Nicht offensichtlich: langer Druck auf den Titel in der Top-Bar setzt Fortschritt und Hilfe-Stati zurueck.

## Technische Umsetzung
- Android-App mit Jetpack Compose, CameraX + ML Kit Barcode Scanning fuer QR-Codes.
- Strict Flow kontrolliert ueber `indypath://posten/{id}`, Validation im ViewModel.
- Persistenz: Android DataStore (naechster erwarteter Posten) -> uebersteht Neustarts.
- Inhalte werden aus `app/src/main/assets/trail` nach `filesDir/trail` kopiert und dann von dort gelesen.
- Offline-first: keine Accounts, kein Netzwerk noetig.

## Projektstruktur (Auszug)
- `app/src/main/java/com/example/christmasindytrail/MainActivity.kt` — Compose-Oberflaeche, Scanner, Post-Anzeige.
- `app/src/main/java/com/example/christmasindytrail/MainViewModel.kt` — Flow-Logik, Validierung, Persistenz.
- `app/src/main/java/com/example/christmasindytrail/data/` — Models, TrailRepository (Assets -> Files, Manifest/Meta-Lesen).
- `app/src/main/assets/trail/weihnachten_2025/` — Beispiel-Trail mit manifest/meta + Bildern.
- `gradlew`, `gradlew.bat`, `gradle/wrapper/` — Gradle Wrapper (8.10.2).

## Abnahmekriterien
- QR-Code-Scan funktioniert zuverlaessig; falsche Codes liefern die Fehlermeldung.
- Posten erscheinen nur in der richtigen Reihenfolge.
- Titel/Text nur wenn vorhanden; Bilder werden stufenweise eingeblendet.
- Fortschritt bleibt nach App-Neustart erhalten; versteckter Reset funktioniert.
- Look & Feel entspricht dunklem IndyTrail-Stil mit Cyan/Gold-Akzenten.

## Milestones (empfohlen)
- **M1 Core**: QR-Scanner, Strict Flow, 1 Posten laden & anzeigen.
- **M2 Content**: Ordnerstruktur lesen, Bilder + optionale Texte, Hilfe-Logik.
- **M3 Polish**: Animationen, Look & Feel, Reset.

## Build & Run
- Voraussetzungen: JDK 17, Android SDK/Android Studio.
- Start (CLI):
  - `./gradlew assembleDebug` (macOS/Linux) oder `gradlew.bat assembleDebug` (Windows).
  - App auf ein Geraet mit Kamera (minSdk 24, targetSdk 34) installieren.
- Trail-Dateien sind im APK eingebettet und werden beim ersten Start in den App-Speicher kopiert.
