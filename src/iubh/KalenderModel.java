package iubh;

import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.WEEK_OF_YEAR;

@SuppressWarnings({"deprecation", "MagicConstant"})
public class KalenderModel extends AbstractTableModel {

    public static String[] spaltennamen = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    public static int[] feiertageVonOstersonntag = {-48, -47, -46, -7, -3, -2, 0, 1, 39, 49, 50, 60};
    public Date[] feiertagsDaten;
    private int zeilen = 0;
    private Date startDerWoche;
    private Date startDatum;
    private Date endDatum;

    public KalenderModel(Date startDatum, Date endDatum) {
        this.startDatum = startDatum;
        this.endDatum = endDatum;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDatum);
        startDerWoche = cal.getTime();
        Date ostersonntag = getOstersonntag(cal.getTime().getYear());

        //Bestimmen der Feiertage des Jahres
        int i = 0;
        feiertagsDaten = new Date[feiertageVonOstersonntag.length * 2 + 16];//Zahl der variablen und festen Feiertage für dieses und das nächste Jahr
        for (int feiertag : feiertageVonOstersonntag) {
            cal.setTime(ostersonntag);
            cal.add(Calendar.DATE, feiertag);
            feiertagsDaten[i] = cal.getTime();
            i++;
        }
        //Bestimmen der Feiertage des Folgejahres
        ostersonntag = getOstersonntag(cal.getTime().getYear() + 1);
        for (int feiertag : feiertageVonOstersonntag) {
            cal.setTime(ostersonntag);
            cal.add(Calendar.DATE, feiertag);
            feiertagsDaten[i] = cal.getTime();
            i++;
        }

        //Feste Feiertage (nicht vom Ostersonntag abhängig)
        int[] festeFeiertage = {1, 1, 6, 1, 1, 5, 3, 10, 1, 11, 25, 12, 26, 12, 15, 8};//Zahlenpaare stehen jeweils für tag1,monat1,tag2,monat2...
        for (int j = 0; j < festeFeiertage.length; j += 2) {
            cal.set(startDatum.getYear() + 1900, festeFeiertage[j + 1] - 1, festeFeiertage[j]);
            feiertagsDaten[i] = cal.getTime();
            i++;
            //Datum wird um 1 erhöht für feste Feiertage im Folgejahr
            cal.set(startDatum.getYear() + 1901, festeFeiertage[j + 1] - 1, festeFeiertage[j]);
            feiertagsDaten[i] = cal.getTime();
            i++;
        }

        cal.setTime(startDatum);//Reset des Kalenders

        //Zählen der benötigten Zeilen der Tabelle abhängig von der gewählten Zeitspanne
        while (cal.getTime().before(endDatum)) {
            cal.add(Calendar.DATE, 7);
            zeilen++;
        }
        zeilen++;
    }

    public static String EinfachesDatum(Date datum) {
        DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        if (datum.getMinutes() == 13) return format.format(datum) + " (Feiertag)";
        if (datum.getMinutes() == 14) return format.format(datum) + " (Praxistag)";
        else return format.format(datum);
    }

    public static String Ausfuellfelder(Date datum) {
        if (datum.getDay() == 6 || datum.getDay() == 0 || datum.getMinutes() == 13 || datum.getMinutes() == 14)
            return null;
        else return "nicht möglich";
    }

    @Override
    public int getRowCount() {
        return (zeilen) * 4;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Date.class;
    }

    @Override
    public String getColumnName(int column) {
        return spaltennamen[column];
    }

    @Override
    public Date getValueAt(int rowIndex, int columnIndex) {
        //Diese Methode gibt an welche Information in jeder Zelle der Tabelle stehen soll.

        Date zellenDatum = null;//Initialisieren mit Leerzelle
        Calendar cal = Calendar.getInstance();//es gibt nur einen Kalender, daher kann auch nur diese Instanz gefunden werden
        cal.setTime(startDerWoche);
        if (startDerWoche != null) {

            //Setzt Leere Tage an den Anfang des Kalenders, bis der erste Tag in der richten Spalte steht
            while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                cal.add(Calendar.DATE, -1);
            }

            //Um für jeden Tag 3 Felder (Morgen, Nachmittag, Abend) zu bekommen muss gelten: Rows 0 bis 2 = 1; 3 bis 5 = 2 ...
            int tag = (((rowIndex) / 4)) * 7 + columnIndex;
            cal.add(Calendar.DATE, tag);
            //Daten vor dem Startdatum oder nach dem Enddatum werden als Leerzellen dargestellt
            if (cal.getTime().before(startDatum) || cal.getTime().after(endDatum)) {
                zellenDatum = null;
            } else {
                //Feiertage bekommen die Minute 13, welche von der Methode "einfachesDatum" erkannt wird.
                if (isFeiertag(cal)) cal.set(Calendar.MINUTE, 13);
                //Praxistage bekommen die Minute 14, welche von der Methode "einfachesDatum" erkannt wird.
                if (cal.get(WEEK_OF_YEAR) % 2 != 0) cal.set(Calendar.MINUTE, 14);//TODO:Renderer
                zellenDatum = cal.getTime();
            }
        }
        return zellenDatum;
    }

    private boolean isFeiertag(Calendar cal) {
        boolean isFeiertag = false;
        for (Date feiertag : feiertagsDaten) {
            if (cal.getTime().toString().equalsIgnoreCase(feiertag.toString())) {
                isFeiertag = true;
                break;
            }
        }
        return isFeiertag;
    }

    //TODO:Unifreie Zeit
    //jede zweite Woche

    private Date getOstersonntag(int jahr) {
        {
            //Javas Jahreszählung fängt erst nach 1900 an. In der Gaußschen Osterformel wird des normale Jahr benötigt.
            jahr = jahr + 1900;
            int a, b, c, k, p, q, M, N, d, e, ostern;
            int Ostermonat = 2;
            a = jahr % 19;
            b = jahr % 4;
            c = jahr % 7;
            k = jahr / 100;
            p = (8 * k + 13) / 25;
            q = k / 4;
            M = (15 + k - p - q) % 30;
            N = (4 + k - q) % 7;
            d = (19 * a + M) % 30;
            e = (2 * b + 4 * c + 6 * d + N) % 7;
            if (d + e == 35) ostern = 50;
            else if (d == 28 && e == 6 && (11 * M + 11) % 30 < 19) ostern = 49;
            else ostern = 22 + d + e;
            //Der Integer, der zurückgegeben wird entspricht den Tagen seit Anfang März.
            //ein später Ostersonntag ist dabei erst z.B. am 40sten März, was dem 9.April entspricht. (40-31=9)
            if (ostern > 31) {
                ostern -= 31;
                Ostermonat = 3;
            }
            Calendar cal = Calendar.getInstance();
            cal.set(jahr, Ostermonat, ostern, 0, 0, 0);
            return cal.getTime();
        }
    }
}
