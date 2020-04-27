package iubh;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws ParseException {//Parse Exception, weil das SimpleDateFormat fehlschlagen könnte, wenn kein gültiges Datum angegeben wurde.

        //Verzeichnis aller Räume mit Ihren Sitzplatzzahlen.Größere Räume stehen oben
        String[][] raumVerzeichnis = {
                {"Eisbachwelle", "30"},
                {"Viktualienmarkt", "20"},
                {"Marienplatz", "15"}
        };

        //Verzeichnis aller Professoren mit Ihren Vorlesungen. Ohne diese Datei wird nur der Professorenname in den Kalender eingetragen, statt eine Vorlesung zu planen.
        String[][] vorlesungenDerProfessoren = {
                {"Geiger", "OoP1", "IndS", "OoP2", "SWE1", "SWE2", "DBMa"},
                {"Pfeil", "ReqE1", "ReqE2", "DSP1"},
                {"Gambarte", "BWL1", "BWL2", "BWL3"},
                {"Rempel", "BAdm1", "BAdm2", "BAdm3"},
                {"Pöschl", "VWL1", "VWL2", "VWL3"},
                {"Wittmann", "KuL1", "KuL2", "KuL3"},
                {"Ayoub", "Mark1", "Mark2", "Mark3"},
                {"Jenny", "Stat1", "Stat2"},
                {"Tobias", "Chem1", "Chem2"},
                {"Weronika", "JaSc1", "JaSc2"}
        };
        //Verzeichnis aller Studiengänge mit Ihren Studentenzahlen und Vorlesungen.
        // Momentan sind keine Semester vorgesehen, in welchen der selbe Professor mehr als eine Vorlesung hält
        String[][] SemesterStudentenzahlFaecher = {
                {"WinWS19", "8", "OoP1", "BWL3", "ReqE1"},
                {"WinWS18", "8", "ReqE2", "OoP2", "BAdm3"},
                {"WinSS19", "11", "BAdm1", "IndS", "DSP1"},
                {"MarWS18", "20", "VWL2", "Mark3", "KuL3"},
                {"MarWS19", "27", "Mark1", "VWL1", "BWL1"},
                {"TouWS18", "19", "SWE1", "Mark2", "VWL3"},
                {"GeMWS18", "19", "DBMa", "BAdm2", "Chem2"},
                {"CheWS19", "6", "Chem1", "SWE2", "Stat2"},
                {"ReWWS19", "6", "JaSc1", "KuL2", "BWL2"},
                {"JaSWS19", "27", "KuL1", "JaSc2", "Stat1"}
        };

        //Initialisierungsvariablen
        int versuchszahl = 300;
        int raumzahl = raumVerzeichnis.length;

        //Vorlesungsklassen werden anhand der Angaben erstellt und in ein Array zusammengefasst.
        Vorlesung[] Vorlesungsliste = ExportManager.getVorlesungsliste(vorlesungenDerProfessoren, SemesterStudentenzahlFaecher);

        //Die Vorlesungen werden nach Studentenzahl sortiert. Dafür wir ein neuer Comparator erstellt.
        Arrays.sort(Vorlesungsliste, new Comparator<Vorlesung>() {
            public int compare(Vorlesung b1, Vorlesung b2) {
                if (b1.getStudentenzahl() > b2.getStudentenzahl()) return -1;
                else if (b1.getStudentenzahl() < b2.getStudentenzahl()) return 1;
                else return 0;
            }
        });

        //      //DEBUG gibt Vorlesungsverzeichnis aus
        //      for (Vorlesung v :
        //              Vorlesungsliste) {
        //          System.out.println(v.getName()+" "+v.getProfessor() + " " + v.getStudentenzahl());
        //      }


        //Eigene Klasse KalenderRenderer ist für Abbildung der Tabelle zuständig
        KalenderRenderer kalenderRenderer = new KalenderRenderer();

        //Erzeugen eines Datums für das Kalendermodell
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date startDate = sdf.parse("30.03.2020");
        Date endDate = sdf.parse("09.08.2020");

        //Erstellen des Kalendermodells
        KalenderModel kalenderModel = new KalenderModel(startDate, endDate);

        //Erstellen der Kalendertabelle nach dem Kalendermodell
        String[][] hintergrundDaten = new String[kalenderModel.getRowCount()][kalenderModel.getColumnCount()];
        for (int i = 0; i < kalenderModel.getRowCount(); i++) {
            for (int j = 0; j < kalenderModel.getColumnCount(); j++) {
                try {
                    if (i % 4 == 0) {//In jedes erste von 4 Feldern wird das Datum aus dem Kalendermodell übernommen
                        hintergrundDaten[i][j] = KalenderModel.EinfachesDatum(kalenderModel.getValueAt(i, j));
                    }
                    //In alle weiteren Felder (mit Datum darüber) wird "möglich" geschrieben. Wochenenden, Feiertage und Praxistage sind ausgeschlossen
                    else if (hintergrundDaten[i - (i % 4)][j] != null)
                        hintergrundDaten[i][j] = KalenderModel.Ausfuellfelder(kalenderModel.getValueAt(i, j));

                } catch (NullPointerException e) {
                    //wenn keine Daten im Kalendermodell gefunden werden, dann muss auch nichts übernommen werden
                    hintergrundDaten[i][j] = null;
                }
            }
        }

        //Zusammensetzen der Daten zur Kalendertabelle
        JTable kalenderTabelle = new JTable(hintergrundDaten, KalenderModel.spaltennamen);
        kalenderTabelle.setDefaultRenderer(Object.class, kalenderRenderer);
        kalenderRenderer.setChosenString(("bevorzugt"));
        kalenderTabelle.setRowHeight(30);

        //Combobox zur Auswahl der Zellwerte wird erstellt
        JComboBox<String> stringWaehler = new JComboBox<>();
        stringWaehler.addItem("bevorzugt");
        stringWaehler.addItem("möglich");
        stringWaehler.addItem("nicht möglich");
        stringWaehler.setBackground(Color.GREEN);
        stringWaehler.setOpaque(true);
        stringWaehler.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                kalenderRenderer.setChosenString((String) stringWaehler.getSelectedItem());
                switch (stringWaehler.getSelectedIndex()) {
                    case 0:
                        stringWaehler.setBackground(Color.GREEN);
                        break;
                    case 1:
                        stringWaehler.setBackground(Color.YELLOW);
                        break;
                    default:
                        stringWaehler.setBackground(Color.RED);
                        break;
                }

            }
        });

        //Combobox zur Auswahl des Ausfüllmodus wird erstellt//TODO: Zuerst Auswählen funktioniert noch nicht
        JComboBox<String> modusWaehler = new JComboBox<>();
        modusWaehler.addItem("Sofort Überschreiben");
        modusWaehler.addItem("Zuerst Auswählen");
        modusWaehler.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                kalenderRenderer.setAuswahlmodus(modusWaehler.getSelectedItem() == "Zuerst Auswählen");
                kalenderTabelle.setCellSelectionEnabled(modusWaehler.getSelectedItem() == "Zuerst Auswählen");//Ermöglicht das Auswählen eines Rechtecks
            }
        });

        ExportManager exportManager = new ExportManager();

        //Filechooser mit Filter zum Finden der Speicherdateien
        JFileChooser chooser = new JFileChooser();
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                //Es werden nur txt Dateien und Ordner angezeigt
                return f.getAbsolutePath().endsWith(".txt") || !f.getAbsolutePath().contains(".");
            }

            @Override
            public String getDescription() {
                return "Textdateien (.txt)";
            }
        };

        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Bitte .txt-Datei auswählen.");
        chooser.setFileFilter(fileFilter);
        chooser.setAcceptAllFileFilterUsed(false);

        //Speichern-Knopf
        JButton saveButton = new JButton("Tabelle speichern");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e2) {
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try {
                        exportManager.writeFile("" + chooser.getSelectedFile(), kalenderTabelle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("keine Auswahl getroffen");
                }
            }
        });

        //Laden-Knopf
        JButton loadButton = new JButton("Tabelle laden");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    exportManager.loadFile("" + chooser.getSelectedFile(), kalenderTabelle);
                } else {
                    System.out.println("keine Auswahl getroffen");
                }
            }
        });

        //Combine-Knopf
        JButton combineButton = new JButton("Tabellen kombinieren");
        combineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportManager.kalenderKombinieren(chooser.getCurrentDirectory().getPath(), kalenderTabelle, versuchszahl, raumzahl);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        //Raumkalender-Knopf
        JButton raumkalenderButton = new JButton("Raumkalender erstellen");
        raumkalenderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    exportManager.vorlesungenVerteilen(Vorlesungsliste, raumVerzeichnis, kalenderTabelle, chooser.getCurrentDirectory().getPath(), versuchszahl, 100000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        //Random-Knopf
        JButton randomButton = new JButton("Zufallskalender");
        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Random rnd = new Random();
                String bewertung;
                for (int row = 0; row < kalenderTabelle.getRowCount(); row++) {
                    for (int col = 0; col < kalenderTabelle.getColumnCount(); col++) {
                        if (row % 4 != 0 && kalenderTabelle.getValueAt(row, col) != null) {
                            switch (rnd.nextInt(3)) {
                                case 0: {
                                    bewertung = "bevorzugt";
                                    break;
                                }
                                case 1: {
                                    bewertung = "möglich";
                                    break;
                                }
                                default: {
                                    bewertung = "nicht möglich";
                                    break;
                                }
                            }
                            kalenderTabelle.setValueAt(bewertung, row, col);
                        }
                    }

                }
            }
        });

        //Einstellungen für das Programmfenster
        JFrame frame = new JFrame("iubh Semesterplaner");

        //Extra Panel für Dropdowns
        JPanel comboboxpanel = new JPanel(new BorderLayout());
        comboboxpanel.add(new JLabel("Gewählte Ausfülloption: "), BorderLayout.NORTH);
        comboboxpanel.add(stringWaehler, BorderLayout.CENTER);
        comboboxpanel.add(randomButton, BorderLayout.EAST);


        //Extra Panel für Speichern/Laden
        JPanel saveLoadPanel = new JPanel(new BorderLayout());
        saveLoadPanel.add(saveButton, BorderLayout.EAST);
        saveLoadPanel.add(loadButton, BorderLayout.WEST);
        saveLoadPanel.add(combineButton, BorderLayout.CENTER);
        saveLoadPanel.add(raumkalenderButton, BorderLayout.SOUTH);

        //Zusammensetzen des Fensters
        frame.add(new JScrollPane(kalenderTabelle), BorderLayout.CENTER);
        frame.add(comboboxpanel, BorderLayout.NORTH);
        frame.add(saveLoadPanel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(1, 1, 1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}