package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Table extends JComponent {

    private static final int HEAD = 40;
    private static final int ROW = 42;

    private final String[] columns;
    private final double[] weights;
    private final List<String[]> rows = new ArrayList<>();
    private final List<Color> badges = new ArrayList<>();
    private int badgeColumn = -1;
    private int dotColumn = -1;
    private String empty = "Nothing yet";

    public Table(String[] columns, double[] weights) {
        this.columns = columns;
        this.weights = weights;
    }

    public Table badgeColumn(int index) {
        this.badgeColumn = index;
        return this;
    }

    public Table dotColumn(int index) {
        this.dotColumn = index;
        return this;
    }

    public Table empty(String text) {
        this.empty = text;
        return this;
    }

    public void setRows(List<String[]> data, List<Color> badgeColors) {
        rows.clear();
        rows.addAll(data);
        badges.clear();
        if (badgeColors != null) {
            badges.addAll(badgeColors);
        }
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, HEAD + Math.max(1, rows.size()) * ROW + 6);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        int w = getWidth();
        double total = 0;
        for (double v : weights) {
            total += v;
        }
        double[] xs = new double[columns.length + 1];
        double usable = w - 32;
        xs[0] = 16;
        for (int i = 0; i < columns.length; i++) {
            xs[i + 1] = xs[i] + usable * weights[i] / total;
        }
        g.setFont(Theme.font(700, 11.5f));
        g.setColor(Theme.DIM);
        FontMetrics hm = g.getFontMetrics();
        for (int i = 0; i < columns.length; i++) {
            g.drawString(columns[i].toUpperCase(), (float) xs[i], (HEAD + hm.getAscent()) / 2f - 1);
        }
        g.setColor(Theme.LINE_SOFT);
        g.fillRect(0, HEAD - 1, w, 1);
        if (rows.isEmpty()) {
            g.setFont(Theme.font(500, 13.5f));
            g.setColor(Theme.FAINT);
            g.drawString(empty, 16, HEAD + 26);
        }
        g.setFont(Theme.font(500, 13.5f));
        FontMetrics fm = g.getFontMetrics();
        for (int r = 0; r < rows.size(); r++) {
            String[] row = rows.get(r);
            int y = HEAD + r * ROW;
            float base = y + (ROW + fm.getAscent() - fm.getDescent()) / 2f;
            for (int c = 0; c < columns.length && c < row.length; c++) {
                String text = fit(fm, row[c], xs[c + 1] - xs[c] - 10);
                double x = xs[c];
                if (c == dotColumn) {
                    g.setColor(Theme.SAGE);
                    g.fill(new Ellipse2D.Double(x, y + ROW / 2.0 - 3.5, 7, 7));
                    x += 15;
                }
                if (c == badgeColumn) {
                    Color tint = r < badges.size() && badges.get(r) != null ? badges.get(r) : Theme.SAGE;
                    double bw = fm.stringWidth(text) + 18;
                    g.setColor(Theme.alpha(tint, 0.14));
                    g.fill(new RoundRectangle2D.Double(x - 2, y + ROW / 2.0 - 12, bw, 24, 24, 24));
                    g.setColor(tint);
                    g.drawString(text, (float) (x + 7), base);
                    continue;
                }
                g.setFont(Theme.font(c == 0 || c == dotColumn ? 700 : 500, 13.5f));
                g.setColor(c == 0 || c == dotColumn ? Theme.TEXT : Theme.TEXT_2);
                g.drawString(text, (float) x, base);
                g.setFont(Theme.font(500, 13.5f));
            }
            if (r < rows.size() - 1) {
                g.setColor(new Color(255, 255, 255, 8));
                g.fillRect(16, y + ROW - 1, w - 32, 1);
            }
        }
        g.dispose();
    }

    private static String fit(FontMetrics fm, String text, double max) {
        if (fm.stringWidth(text) <= max) {
            return text;
        }
        String s = text;
        while (s.length() > 1 && fm.stringWidth(s + "\u2026") > max) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "\u2026";
    }
}
