package iubh;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ExportManager {

    public String[][] saveProfessorenDaten;
    public String[][] professorenDaten;

    //Randomisiert die erste Dimension eines String[][]Arrays
    public static void StringsMischen(String[][] stringArray) {
        Random rgen = new Random();
        String[] savestring = new String[stringArray[0].length];
        for (int i = 0; i < stringArray.length; i++) {
            //Zufallszahl entsprechend der ersten Dimension des 2d-Arrays wird gezogen
            int zufallsZahl = rgen.nextInt(stringArray.length);
            for (int j = 0; j < stringArray[0].length; j++) {
                //Der gezogene Array wird in savestring gespeichert
                savestring[j] = stringArray[i][j];
                //der gezogene Array wird mit einem zufälligen überschrieben
                stringArray[i][j] = stringArray[zufallsZahl][j];
                //der zufällige Array wird mit dem gezogenen überschrieben
                stringArray[zufallsZahl][j] = savestring[j];
            }
        }
    }

    //Randomisiert alle Elemente eines Int[]Arrays
    public static void IntsMischen(int[] intArray) {
        Random rgen = new Random();
        for (int i = 0; i < intArray.length; i++) {
            //Zufallszahl wird gezogen
            int zufallsZahl = rgen.nextInt(intArray.length);
            //Der momentane Int wird in saveint gespeichert
            int saveint = intArray[i];
            //Der momentane int wird mit der Zufallszahl überschrieben
            intArray[i] = intArray[zufallsZahl];
            //der zufällige Int wird mit dem gespeicherten überschrieben
            intArray[zufallsZahl] = saveint;
        }
    }

    public static Vorlesung[] getVorlesungsliste(String[][] vorlesungenDerProfessoren, String[][] SemesterStudentenzahlFaecher) {
        //Aus den Angaben am Anfang der Main-Methode werden Vorlesungen erstellt und in einem Array zusammengefasst.
        //Die Vorlesungsliste wird drei mal soviele Vorlesungen enthalten, wie Semester geplant sind (da immer 3 Vorlesungen pro Semester)
        Vorlesung[] vorlesungsliste = new Vorlesung[SemesterStudentenzahlFaecher.length * 3];
        int vorlesungszahl = 0;
        for (String[] s : vorlesungenDerProfessoren) {
            String professorName = s[0];
            for (int i = 1; i < s.length; i++) {
                String vorlesungsName = s[i];
                //Die Default-Studentenzahl ist willkürlich auf 20 gesetzt. Hohe Zahlen laufen Gefahr die Optimierung zu gefährden, bei niedrigen reicht der Platz im Raum eventuell nicht.
                int studentenzahl = 20;
                for (String[] s2 : SemesterStudentenzahlFaecher) {
                    for (int j = 1; j < s2.length; j++) {
                        if (s2[j].equals(vorlesungsName)) studentenzahl = Integer.parseInt(s2[1]);
                    }
                }
                Vorlesung vorlesung = new Vorlesung(vorlesungsName, professorName, studentenzahl);
                vorlesungsliste[vorlesungszahl] = vorlesung;
                vorlesungszahl++;
            }
        }
        return vorlesungsliste;
    }

    public void writeFile(String filepath, JTable table) throws IOException {
        try {
            //Printwriter geht alle Spalten und Zeilen der Tabelle durch und schreibt Sie in neue Zeilen der Textdatei im übergebenen Pfad.
            //Hat der User eine neue Tabelle erstellt, dann muss diese noch eine Endung bekommen.
            if (!filepath.endsWith(".txt")) filepath = filepath + ".txt";
            PrintWriter writer = new PrintWriter(new File(filepath));

            //in der ersten Zeile der Speicherdateien steht der Professorenname (Dateiname ohne Endung)
            String Professor = filepath.substring(filepath.lastIndexOf("\\") + 1, filepath.length() - 4);
            writer.println(Professor);

            //in der zweiten Zeile der Speicherdateien steht die Anzahl der Unterrichtseinheiten
            writer.println(getUnterrichtseinheiten(Professor, 0, ""));

            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    writer.println(table.getValueAt(row, col));
                }
            }
            writer.close();
            System.out.println("Speichern erfolgreich!(" + filepath + ")");

        } catch (FileNotFoundException e) {
            //Dieser Fehler würde entstehen, wenn die Datei im angegeben Pfad nicht existiert
            //TODO: Fehlermeldung für User
            e.printStackTrace();
        }
    }

    public void loadFile(String filepath, JTable table) {
        try {
            File file = new File(filepath);
            LineNumberReader reader = new LineNumberReader(new FileReader(file));
            String readLine;
            //Der Reader geht zeilenweise die Textdatei im angegebenen Pfad durch und schriebt die gefundenen Strings in die Zellen der Tabelle.
            //Die ersten zwei Zeilen enthalten Infos und werden daher übersprungen
            reader.readLine();
            reader.readLine();
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int col = 0; col < table.getColumnCount(); col++) {
                    readLine = reader.readLine();
                    //Damit der String "null" nicht so übernommen wird, sondern eine Leerzelle geschrieben wird muss ersetzt werden:
                    if (readLine.equalsIgnoreCase("null")) readLine = null;
                    table.setValueAt(readLine, row, col);
                }
            }
            System.out.println("Laden erfolgreich!(" + filepath + ")");
        } catch (IOException e) {
            //Dieser Fehler würde entstehen, wenn die Datei im angegeben Pfad nicht existiert, oder wenn z.B. das Auslesen (i/o = import/export) der Datei scheitert (Verbindung getrennt etc)
            e.printStackTrace();
        }
    }

    public void kalenderKombinieren(String Ordnerpfad, JTable Grundtabelle, int versuchszahl, int raumzahl) throws IOException {

        //Fortschritt-Fenster wird eingeblendet. Hier kein Balken etc um Multithread zu umgehen
        Dialog frame = new JDialog();
        frame.setSize(500, 20);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //Pfade aller .txt-Dateien im Programmverzeichnis werden gesammelt //TODO: Extra save-Ordner
        List<File> txtDateien = new ArrayList<>();
        File dir = new File(Ordnerpfad);
        int txtDateienAnzahl = 0;
        for (File datei : Objects.requireNonNull(dir.listFiles())) {
            if (datei.getName().endsWith((".txt"))) {
                txtDateien.add(datei);
                txtDateienAnzahl++;
            }
        }

        //Fasst alle gefundenen Daten der txt-Dateien in einem String[][] zusammen
        int felderzahl = Grundtabelle.getColumnCount() * Grundtabelle.getRowCount();
        professorenDaten = new String[txtDateienAnzahl][felderzahl];
        for (int i = 0; i < txtDateienAnzahl; i++) {
            File file = new File(txtDateien.get(i).getAbsolutePath());
            LineNumberReader reader = null;
            try {//TODO: Fehlerbeschreibungen
                reader = new LineNumberReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            for (int j = 0; j < felderzahl; j++) {
                if (reader != null) {
                    reader.setLineNumber(j);
                }
                try {
                    if (reader != null) {
                        professorenDaten[i][j] = reader.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//----------------------------------------Raumwechsel----------------------------------------------------------------------------------------------------------
        for (int raum = 0; raum < raumzahl; raum++) {

            //resettet Score
            int kalenderScore = 0;

//----------------------------------------Versuchsbeginn----------------------------------------------------------------------------------------------------------

            for (int versuch = 0; versuch < versuchszahl; versuch++) {
                //Fortschrittsanzeige wird aktualisiert
                frame.setTitle("Versuchszahl: " + versuchszahl + " Raum: " + raum + " Fortschritt: " + versuch * 100 / versuchszahl + "%");

                //Noch zu vergebende Unterrichtseinheiten und Trefferzahlen werden resettet
                for (String[] s : professorenDaten) {
                    s[1] = String.valueOf(getUnterrichtseinheiten(s[0], raum, Ordnerpfad));
                }

                int anzahlBevorzugt = 0;
                int anzahlMoeglich = 0;
/*
                //Alternative: Werden die Spalten und Zeilen im Vorfeld gemischt, dann kann danach mit einer Innen- und Außen-
                //schleife gearbeitet werden. Bei Außenschleife Spalten würden dann bevorzugt Räume den ganzen Tag frei gelassen.
                //Diese Steuerung geht allerdings auf Kosten der Optimierung, weil dann nach TagX immer TagY angeschaut werden müsste
                //Erzeuge gemischte Integerarrays, um die Tage und Tageszeiten randomisieren zu können
                int[] zeilen = new int[Grundtabelle.getRowCount()];
                for (int i = 0; i < Grundtabelle.getRowCount(); i++) {
                    zeilen[i] = i;
                }
                IntsMischen(zeilen);
                int[] spalten = new int[Grundtabelle.getColumnCount()];
                for (int i = 0; i < Grundtabelle.getColumnCount(); i++) {
                    spalten[i] = i;
                }
                IntsMischen(spalten);

                //Die Zellen der Grundtabelle werden in zufälliger Reihenfolge durchiteriert
                for (int irow = 0; irow < Grundtabelle.getRowCount(); irow++) {
                    int row = zeilen[irow];
                    for (int icol = 0; icol < Grundtabelle.getColumnCount(); icol++) {
                        int col = spalten[icol];*/

//Ein Array mit allen Zellenadressen(Laufnummer) wird erstellt und gemischt
                int Felderzahl = Grundtabelle.getColumnCount() * Grundtabelle.getRowCount();
                int[] FeldnummerArray = new int[Felderzahl];
                for (int i = 0; i < Felderzahl; i++) {
                    FeldnummerArray[i] = i;
                }
                IntsMischen(FeldnummerArray);
                for (int kalenderFeld = 0; kalenderFeld < Felderzahl; kalenderFeld++) {
                    int row = (FeldnummerArray[kalenderFeld] / 7);
                    int col = (FeldnummerArray[kalenderFeld] % 7);

                    if (row % 4 != 0 && Grundtabelle.getValueAt(row, col) != null) {
                        //Reihenfolge der gespeicherten Txt-Daten wird randomisiert
                        StringsMischen(professorenDaten);
                        String readline = null;
                        SucheBeginn:
                        //Drei Iterationen für "bevorzugt", "möglich" und "nicht möglich"
                        for (int i = 0; i < 3; i++) {
                            for (int j = 0; j < professorenDaten.length; j++) {
                                //Prüft, ob noch Unterrichtseinheiten zu vergeben sind.
                                int uebrigeVersuche = Integer.parseInt(professorenDaten[j][1]);
                                //if(professorenDaten[j][0].equalsIgnoreCase("Geiger"))System.out.println(uebrigeVersuche);
                                if (uebrigeVersuche > 0) {
                                    readline = professorenDaten[j][row * 7 + col + 2];
                                    //Wenn ein "bevorzugt" gefunden wurde, dann wird dieses direkt in die Zieltabelle übernommen.
                                    if (i < 1 && readline.equalsIgnoreCase("bevorzugt")) {
                                        //Schreibt den Professornamen und "bevorzugt" in die Zieltabelle.
                                        Grundtabelle.setValueAt(professorenDaten[j][0] + ":" + readline, row, col);
                                        anzahlBevorzugt++;
                                        professorenDaten[j][1] = String.valueOf(Integer.parseInt(professorenDaten[j][1]) - 1);
                                        break SucheBeginn;
                                    }
                                    //wurde in dem ersten Druchgang kein "bevorzugt" gefunden, dann wird das erste "möglich" übernommen
                                    if (i == 1 && readline.equalsIgnoreCase("möglich")) {
                                        //Schreibt den Professornamen und "möglich" in die Zieltabelle
                                        Grundtabelle.setValueAt(professorenDaten[j][0] + ":" + readline, row, col);
                                        anzahlMoeglich++;
                                        professorenDaten[j][1] = String.valueOf(Integer.parseInt(professorenDaten[j][1]) - 1);
                                        break SucheBeginn;
                                    }
                                    //wurden alle Dateien zweimal durchsucht und weder "möglich" noch "bevorzugt" gefunden, so wird "nicht möglich" geschrieben
                                    if (i > 1) {
                                        Grundtabelle.setValueAt("nicht möglich", row, col);
                                        break SucheBeginn;
                                    }
                                }
                            }
                        }
                        if (readline == null) Grundtabelle.setValueAt("nicht möglich", row, col);
                    }
                }
                //Wurde der Tabellenscore verbessert, so wird die Tabelle erneuert und als txt-Datei abgespeichert.
                //Hier könnten noch viele andere Bewertungsmöglichkeiten angewendet werden (z.B. Freitag frei=+10)
                //10 und 7 gewählt, damit einmal bevorzugt nicht besser ist als zwei mal möglich
                int newScore = anzahlBevorzugt * 10 + anzahlMoeglich * 7;
                if (newScore > kalenderScore) {
                    kalenderScore = newScore;
                    writeFile(Ordnerpfad + "/save/besttableRaum" + raum + ".txt", Grundtabelle);
                }
                //DEBUG zeigt letzten und neuen Score an
                //System.out.println("Neuer Score: " + (newScore) + " bester Score bisher: " + kalenderScore);
            }
            //Wurde keine save-Datei für den Raum erstellt, dann wird ein leerer Kalender gespeichert
            if (kalenderScore == 0) writeFile(Ordnerpfad + "/save/besttableRaum" + raum + ".txt", Grundtabelle);

            //Die beste Kombination wird geladen
            System.out.println("Raum" + raum + " hat einen score von " + kalenderScore + " erreicht");
            loadFile(Ordnerpfad + "/save/besttableRaum" + raum + ".txt", Grundtabelle);

            //Die Anzahl der verteilten Stunden wird vermerkt.
            // Zudem werden die vergebenen Termine der Professoren jetzt als "nicht möglich" überschrieben
            // damit sie in weiteren Räumen nicht mehr vergeben werden
            for (String[] txtString : professorenDaten) {
                txtString[1] = String.valueOf(getUnterrichtseinheiten(txtString[0], raum, Ordnerpfad));
                for (int col = 0; col < Grundtabelle.getColumnCount(); col++) {
                    for (int row = 0; row < Grundtabelle.getRowCount(); row++) {
                        if ((Grundtabelle.getValueAt(row, col) + "").contains(txtString[0])) {
                            txtString[row * 7 + col + 2] = "nicht möglich";
                            txtString[1] = String.valueOf((Integer.parseInt(txtString[1]) - 1));
                        }
                    }
                }

            }
            saveProfessorenDaten = professorenDaten;
        }
        frame.dispose();
        System.out.println("Kombinieren abgeschlossen!");
    }

    //Findet durch den Namen des Professors die Zahl der Unterrichtseinheiten, die dieser pro Semester abhalten soll
    public int getUnterrichtseinheiten(String NameProfessor, int raum, String Ordnerpfad) throws IOException {
        if (raum == 0) {
            switch (NameProfessor) {
                case "Geiger":
                    return 60;

                case "Pfeil":
                    return 30;

                case "Rempel":
                    return 30;

                case "Gambarte":
                    return 30;

                case "Wittmann":
                    return 30;

                case "Ayoub":
                    return 30;

                case "Pöschl":
                    return 30;

                default:
                    return 30;//50 Unterrichtseinheiten a 45min. Ein Block (Vormittag, Nachmittag, Abend) hat 5 Unterrichtseinheiten. Der ganze Semesterkalender SS2020 hat 141 mögliche Einheiten
            }
        } else {
            int fundzahl = 0;
            //Alle vorher erstellten Raumkalender werden durchsucht
            for (int i = 0; i < raum; i++) {
                File file = new File(Ordnerpfad + "/save/besttableRaum" + (i) + ".txt");
                LineNumberReader reader = new LineNumberReader(new FileReader(file));
                //Die ersten zwei Zeilen enthalten Infos und werden daher übersprungen
                reader.readLine();
                reader.readLine();
                //Die Anzahl der Vorkommnisse des Professorennamen im txt-File wird gezählt
                for (int j = 0; j < 560; j++) {
                    if (reader.readLine().contains(NameProfessor)) fundzahl++;
                }
            }
            return getUnterrichtseinheiten(NameProfessor, 0, Ordnerpfad) - fundzahl;
        }
    }

    public void vorlesungenVerteilen(Vorlesung[] vorlesungsliste, String[][] raumliste, JTable kalenderTabelle, String Ordnerpfad, int versuche, int bestesErgebnis) throws IOException {

        int momentanerRaum = 0;
        int verteilteVorlesungen = 0;

        //Geht alle Raumkalender durch und verteilt die Vorlesungen von großer zu kleiner Kursgröße auf die Dozenten
        for (String[] raumbeschreibung : raumliste) {
            //Lädt den ersten Raum (mit dem höchsten Score) in die Kalendertabelle. Dieser wird ab jetzt dem größten Raum zugeordnet.
            loadFile(Ordnerpfad + "/save/besttableRaum" + momentanerRaum + ".txt", kalenderTabelle);

            //Entnimmt die Raumgrösse dem String aus der Initialisierung der Main-Methode
            int raumgroesse = Integer.parseInt(raumbeschreibung[1]);

            //Sucht nach Kalenderfeldern mit dem passenden Professornamen und schreibt die Vorlesung in das Feld, wenn diese noch nicht 10 mal vergeben wurde. So werden die selben Vorlesungen gehäuft im selben Raum stattfinden
            for (Vorlesung v : vorlesungsliste) {
                for (int row = 0; row < kalenderTabelle.getRowCount(); row++) {
                    for (int col = 0; col < kalenderTabelle.getColumnCount(); col++) {
                        if (v.verteileEinheiten < 10 && v.getStudentenzahl() <= raumgroesse && row % 4 != 0 && kalenderTabelle.getValueAt(row, col) != null && kalenderTabelle.getValueAt(row, col).toString().contains(v.getProfessor())) {
                            kalenderTabelle.setValueAt(v.getName(), row, col);
                            v.verteileEinheiten++;
                        }
                    }
                }
            }
            //der Aktuelle Raum wird abgespeichert.
            writeFile(Ordnerpfad + "/Raumkalender/" + raumbeschreibung[0] + ".txt", kalenderTabelle);
            momentanerRaum++;
        }
        //Verteilte Vorlesungen werden gezählt und resettet
        verteilteVorlesungen = 0;

        for (Vorlesung v : vorlesungsliste) {
            //DEBUG: Anzeigen wie viele Vorlesungen nicht verteilt werden konnten
            //if(v.verteileEinheiten!=10) System.out.println(v.getName()+" sind " + (10-v.verteileEinheiten)+ " übrig geblieben");
            verteilteVorlesungen += v.verteileEinheiten;
            v.verteileEinheiten = 0;
        }
        if ((vorlesungsliste.length * 10 - verteilteVorlesungen) < bestesErgebnis)
            bestesErgebnis = (vorlesungsliste.length * 10 - verteilteVorlesungen);
        System.out.println("Es wurden " + verteilteVorlesungen + " Vorlesungen verteilt. Es bleiben noch " + (vorlesungsliste.length * 10 - verteilteVorlesungen) + " Vorlesungen übrig.");

        //Gehen die Räume aus und es bleiben noch Vorlesungen übrig, dann wird nochmal neu kombiniert und die Versuchszahl verringert. Es wird folglich Optimierung zu Gunsten der Studentenzahl/Raum aufgegeben.
        if (versuche > 0 && vorlesungsliste.length * 10 - verteilteVorlesungen != 0) {
            System.out.println("Tabellen werden neu kombiniert...-----------------------------------------------------------------------------------------------");
            versuche = (int) (versuche * 0.9);
            kalenderKombinieren(Ordnerpfad, kalenderTabelle, versuche, raumliste.length);
            vorlesungenVerteilen(vorlesungsliste, raumliste, kalenderTabelle, Ordnerpfad, versuche, bestesErgebnis);
        } else if (versuche < 1)
            System.out.println("Die Vorlesungen konnten nicht miteinander vereinbart werden. Es bleiben mindestens " + bestesErgebnis + " Vorlesungen übrig. Bitte Professoren um mehr Terminangebote bitten oder Versuchszahl erhöhen.");
    }
}
