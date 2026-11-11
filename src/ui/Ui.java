package ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

public final class Ui {

    public static final String FILL = "ui.fill";

    private Ui() {
    }

    public static JLabel label(String text, int weight, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(weight, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel wrap(String text, int weight, float size, Color color, int width) {
        JLabel l = new JLabel(html(text, width));
        l.setFont(Theme.font(weight, size));
        l.setForeground(color);
        return l;
    }

    public static String html(String text, int width) {
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width:" + Math.round(width * 0.88) + "px'>" + safe + "</div></html>";
    }

    public static JPanel clear(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    public static <T extends JComponent> T pad(T c, int top, int left, int bottom, int right) {
        c.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return c;
    }

    public static <T extends JComponent> T fill(T c) {
        c.putClientProperty(FILL, Boolean.TRUE);
        return c;
    }

    public static JPanel column(int gap, Component... items) {
        JPanel p = clear(new Column(gap));
        for (Component c : items) {
            p.add(c);
        }
        return p;
    }

    public static JPanel row(int gap, Component... items) {
        JPanel p = clear(new Row(gap));
        for (Component c : items) {
            p.add(c);
        }
        return p;
    }

    public static JComponent space(int w, int h) {
        JPanel p = clear(null);
        p.setPreferredSize(new Dimension(w, h));
        return p;
    }

    public static JScrollPane scroll(JComponent view) {
        JPanel holder = new ScrollHolder(view);
        JScrollPane sp = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setViewportBorder(null);
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setUnitIncrement(18);
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUI(new ThinBar());
        return sp;
    }

    private static final class ScrollHolder extends JPanel implements javax.swing.Scrollable {
        ScrollHolder(JComponent view) {
            super(new java.awt.BorderLayout());
            setOpaque(false);
            add(view, java.awt.BorderLayout.NORTH);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return r.height - 40;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() != null && getParent().getHeight() > getPreferredSize().height;
        }
    }

    private static final class ThinBar extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Theme.LINE;
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected javax.swing.JButton createDecreaseButton(int o) {
            return zero();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int o) {
            return zero();
        }

        private javax.swing.JButton zero() {
            javax.swing.JButton b = new javax.swing.JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }

        @Override
        protected void paintThumb(Graphics g0, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            g.setColor(isThumbRollover() ? Theme.FAINT : Theme.LINE);
            g.fill(new RoundRectangle2D.Double(r.x + 3, r.y + 2, r.width - 6, r.height - 4, 6, 6));
            g.dispose();
        }
    }

    public static final class Column implements LayoutManager {
        private final int gap;

        public Column(int gap) {
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int w = 0, h = 0, n = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = c.getPreferredSize();
                w = Math.max(w, d.width);
                h += d.height;
                n++;
            }
            h += Math.max(0, n - 1) * gap;
            return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int y = in.top;
            int w = parent.getWidth() - in.left - in.right;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                int h = c.getPreferredSize().height;
                c.setBounds(in.left, y, w, h);
                y += h + gap;
            }
        }
    }

    public static final class Row implements LayoutManager {
        private final int gap;

        public Row(int gap) {
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets in = parent.getInsets();
            int w = 0, h = 0, n = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = c.getPreferredSize();
                w += d.width;
                h = Math.max(h, d.height);
                n++;
            }
            w += Math.max(0, n - 1) * gap;
            return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return preferredLayoutSize(parent);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets in = parent.getInsets();
            int avail = parent.getWidth() - in.left - in.right;
            int h = parent.getHeight() - in.top - in.bottom;
            int fixed = 0, fills = 0, n = 0;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                n++;
                if (isFill(c)) {
                    fills++;
                } else {
                    fixed += c.getPreferredSize().width;
                }
            }
            int extra = Math.max(0, avail - fixed - Math.max(0, n - 1) * gap);
            int x = in.left;
            for (Component c : parent.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }
                Dimension d = c.getPreferredSize();
                int w = isFill(c) ? extra / Math.max(1, fills) : d.width;
                int ch = Math.min(h, d.height);
                c.setBounds(x, in.top + (h - ch) / 2, w, ch);
                x += w + gap;
            }
        }

        private boolean isFill(Component c) {
            return c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty(FILL));
        }
    }
}
