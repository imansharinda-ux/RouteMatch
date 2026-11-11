package ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.function.IntConsumer;

public class Chips extends JPanel {

    private final String[] labels;
    private final Shape[] icons;
    private final boolean tiles;
    private int selected;
    private IntConsumer listener;

    public Chips(String[] labels, Shape[] icons, boolean tiles, int selected) {
        this.labels = labels;
        this.icons = icons;
        this.tiles = tiles;
        this.selected = selected;
        setOpaque(false);
        setLayout(tiles ? new GridLayout(1, labels.length, 10, 0) : new Ui.Row(8));
        for (int i = 0; i < labels.length; i++) {
            add(new Chip(i));
        }
    }

    public void onChange(IntConsumer listener) {
        this.listener = listener;
    }

    public int selected() {
        return selected;
    }

    public void select(int index) {
        selected = index;
        repaint();
    }

    private final class Chip extends JComponent {
        private final int index;
        private boolean hover;

        Chip(int index) {
            this.index = index;
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
                    selected = Chip.this.index;
                    Chips.this.repaint();
                    if (listener != null) {
                        listener.accept(selected);
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            if (tiles) {
                return new Dimension(90, 78);
            }
            FontMetrics fm = getFontMetrics(Theme.font(600, 13.5f));
            int w = fm.stringWidth(labels[index]) + 30 + (icons != null && icons[index] != null ? 24 : 0);
            return new Dimension(w, 36);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            boolean on = selected == index;
            double w = getWidth(), h = getHeight();
            Shape icon = icons == null ? null : icons[index];
            if (tiles) {
                RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 32, 32);
                g.setColor(on ? Theme.alpha(Theme.ACCENT, 0.12) : hover ? new Color(255, 255, 255, 14) : Theme.alpha(Theme.FIELD, 0.85));
                g.fill(shape);
                g.setStroke(new BasicStroke(on ? 1.5f : 1f));
                g.setColor(on ? Theme.ACCENT : new Color(255, 255, 255, 22));
                g.draw(shape);
                Color fg = on ? Theme.ACCENT : Theme.TEXT_2;
                if (icon != null) {
                    Icons.draw(g, icon, (w - 26) / 2, 12, 26, fg, 1.8);
                }
                g.setFont(Theme.font(on ? 700 : 600, 13));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(on ? Theme.TEXT : Theme.MUTED);
                g.drawString(labels[index], (float) ((w - fm.stringWidth(labels[index])) / 2), (float) (h - 14));
            } else {
                RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, h, h);
                if (on) {
                    g.setColor(Theme.TEXT);
                    g.fill(shape);
                } else {
                    g.setColor(hover ? new Color(255, 255, 255, 18) : new Color(255, 255, 255, 8));
                    g.fill(shape);
                    g.setColor(new Color(255, 255, 255, 22));
                    g.draw(shape);
                }
                Color fg = on ? Theme.BG : hover ? Theme.TEXT : Theme.MUTED;
                g.setFont(Theme.font(on ? 700 : 600, 13.5f));
                FontMetrics fm = g.getFontMetrics();
                double x = 15;
                if (icon != null) {
                    Icons.draw(g, icon, x - 2, (h - 18) / 2, 18, fg, 2);
                    x += 22;
                }
                g.setColor(fg);
                g.drawString(labels[index], (float) x, (float) ((h - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent()));
            }
            g.dispose();
        }
    }
}
