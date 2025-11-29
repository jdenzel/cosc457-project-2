package infinitebasketball;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Theme {

    // --- DARK MODE BRAND COLORS ---
    public static final Color BACKGROUND = new Color(18, 18, 18);
    public static final Color CONTENT_BG = new Color(45, 45, 45);
    public static final Color PRIMARY = new Color(20, 50, 140); // Royal Blue
    public static final Color ACCENT = new Color(255, 195, 0);  // Sports Gold
    public static final Color TEXT_MAIN = new Color(240, 240, 240);

    // --- FONTS ---
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);

    public static void apply(Container container) {
        container.setBackground(BACKGROUND);

        for (Component c : container.getComponents()) {
            styleComponent(c);
            if (c instanceof Container) {
                apply((Container) c);
            }
        }
    }

    private static void styleComponent(Component c) {
        // --- BUTTONS ---
        if (c instanceof JButton) {
            JButton btn = (JButton) c;
            btn.setBackground(PRIMARY);  // Always Blue
            btn.setForeground(ACCENT);   // Always Gold Text
            btn.setFont(FONT_BOLD);
            btn.setFocusPainted(false);
            
            // Gold Border
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
            ));
            
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // REMOVED: The MouseListener that was swapping colors.
            // Now the button will stay Blue when you hover over it.
        } 
        // --- LABELS ---
        else if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            if (lbl.getFont().getSize() > 20) {
                lbl.setFont(FONT_HEADER);
                lbl.setForeground(ACCENT);
            } else {
                lbl.setFont(FONT_NORMAL);
                lbl.setForeground(TEXT_MAIN);
            }
        } 
        // --- INPUT FIELDS ---
        else if (c instanceof JTextField || c instanceof JComboBox) {
            JComponent comp = (JComponent) c;
            comp.setBackground(CONTENT_BG);
            comp.setForeground(Color.WHITE);
            comp.setFont(FONT_NORMAL);
            comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80)), 
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        } 
        // --- PANELS ---
        else if (c instanceof JPanel) {
            c.setBackground(BACKGROUND);
        }
        // --- SCROLL PANES ---
        else if (c instanceof JScrollPane) {
            c.setBackground(BACKGROUND);
            ((JScrollPane) c).getViewport().setBackground(BACKGROUND);
            ((JScrollPane) c).setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        }
        // --- TABLES ---
        else if (c instanceof JTable) {
            JTable table = (JTable) c;
            table.setRowHeight(30);
            table.setFont(FONT_NORMAL);
            table.setBackground(CONTENT_BG);
            table.setForeground(TEXT_MAIN);
            
            table.setSelectionBackground(ACCENT);
            table.setSelectionForeground(Color.BLACK);
            table.setGridColor(new Color(60, 60, 60));
            
            JTableHeader header = table.getTableHeader();
            header.setBackground(PRIMARY);
            header.setForeground(ACCENT);
            header.setFont(FONT_BOLD);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT));
        }
    }
}