package ui;

import common.Places;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

public class PlaceSelect extends JComponent {

    private final String caption;
    private final Color dot;
    private final List<Places.Place> places = Places.sorted();
    private String code;
    private boolean hover;
    private Consumer<String> listener;

    public PlaceSelect(String caption, Color dot, String code) {
        this.caption = caption;
        this.dot = dot;
        this.code = code;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                open();
            }
        });
    }

    public void onChange(Consumer<String> listener) {
        this.listener = listener;
    }

    public String code() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        repaint();
    }

    private void open() {
        String[] names = new String[places.size()];
        int current = 0;
        for (int i = 0; i < places.size(); i++) {
            names[i] = places.get(i).name;
            if (places.get(i).code.equals(code)) {
                current = i;
            }
        }
        JList<String> list = new JList<>(names);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(Theme.PANEL);
        list.setForeground(Theme.TEXT);
        list.setSelectionBackground(Theme.PANEL_2);
        list.setSelectionForeground(Theme.ACCENT);
        list.setFixedCellHeight(38);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, value, index, selected, false);
                c.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                c.setFont(Theme.font(selected ? 700 : 500, 14.5f));
                c.setBackground(selected ? Theme.PANEL_2 : Theme.PANEL);
                c.setForeground(selected ? Theme.ACCENT : Theme.TEXT);
                return c;
            }
        });
        list.setSelectedIndex(current);
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        popup.setBackground(Theme.PANEL);
        JScrollPane sp = Ui.scroll(list);
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(Theme.PANEL);
        sp.setPreferredSize(new Dimension(Math.max(getWidth(), 220), Math.min(320, names.length * 38)));
        popup.add(sp);
        list.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) {
                    list.setSelectedIndex(i);
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) {
                    code = places.get(i).code;
                    popup.setVisible(false);
                    repaint();
                    if (listener != null) {
                        listener.accept(code);
                    }
                }
            }
        });
        popup.show(this, 0, getHeight() + 6);
        list.ensureIndexIsVisible(current);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 64);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double w = getWidth(), h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 32, 32);
        g.setColor(Theme.alpha(Theme.FIELD, 0.85));
        g.fill(shape);
        g.setStroke(new BasicStroke(1f));
        g.setColor(hover ? Theme.alpha(Theme.ACCENT, 0.6) : new Color(255, 255, 255, 22));
        g.draw(shape);
        g.setColor(dot);
        g.fill(new Ellipse2D.Double(16, 17, 7, 7));
        g.setFont(Theme.font(600, 12));
        g.setColor(Theme.DIM);
        g.drawString(caption, 30, 25);
        g.setFont(Theme.font(700, 16));
        FontMetrics fm = g.getFontMetrics();
        String name = Places.name(code);
        int max = (int) w - 50;
        while (fm.stringWidth(name) > max && name.length() > 3) {
            name = name.substring(0, name.length() - 2);
            if (fm.stringWidth(name + "\u2026") <= max) {
                name = name + "\u2026";
                break;
            }
        }
        g.setColor(Theme.TEXT);
        g.drawString(name, 16, 48);
        Icons.draw(g, Icons.CHEVRON_DOWN, w - 32, (h - 16) / 2, 16, Theme.MUTED, 2.4);
        g.dispose();
    }
}
