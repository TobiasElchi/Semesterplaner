package iubh;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

class KalenderRenderer implements TableCellRenderer {
    public static final DefaultTableCellRenderer DEFAULT_RENDERER =
            new DefaultTableCellRenderer();
    private String chosenString;
    private boolean isAuswahlmodus;

    public void setAuswahlmodus(boolean auswahlmodus) {
        isAuswahlmodus = auswahlmodus;
    }

    public void setChosenString(String chosenString) {
        this.chosenString = chosenString;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Color background;
        Color foreground = Color.BLACK;

        //Jede vierte Zeile ist eine Datumszeile. Datumszellen sind immer grau wenn sie Inhalt haben
        if (row % 4 == 0) {
            if (value != null) background = Color.LIGHT_GRAY;
            else background = Color.WHITE;
        }
        //Die normalen Zellen werden je nach Inhalt eingefärbt
        else {
            if (("" + value).contains("bevorzugt")) background = Color.GREEN;
            else if (("" + value).contains("nicht möglich")) background = Color.RED;
            else if (("" + value).contains("möglich")) background = Color.YELLOW;
            else background = Color.WHITE;
            //Im Asuwahlmodus wird zuerst eine Auswahl getroffen und dann der String gewählt. Die Auswahlschrift ist blau gefärbt
            if (isAuswahlmodus) {
                if (isSelected && value != null) {
                    foreground = Color.BLUE;//TODO: Ein Rahmen wäre schöner
                }
            } else {
                //In diesem Fall wird direkt in die angeklickten Zellen geschrieben
                if (hasFocus && value != null) {
                    table.setValueAt(chosenString, row, column);
                }
            }
        }

        renderer.setBackground(background);
        renderer.setForeground((foreground));
        return renderer;
    }
}