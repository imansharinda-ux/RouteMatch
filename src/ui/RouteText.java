package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class RouteText extends JComponent {

    private String from = "";
    private String to = "";
    private final int weight;
    private final float size;
    private final Color color;

    public RouteText(int weight, float size, Color color) {
        this.weight = weight;
        this.size = size;
        this.color = color;
    }

    public RouteText set(String from, String to) {
        this.from = from;
        this.to = to;
        revalidate();
        repaint();
        return this;
    }

    private double arrow() {
        return size * 0.95;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.font(weight, size));
        int w = fm.stringWidth(from) + fm.stringWidth(to) + (int) (arrow() + size * 0.9);
        return new Dimension(w, (int) (size * 1.35));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        g.setFont(Theme.font(weight, size));
        FontMetrics fm = g.getFontMetrics();
        float base = (getHeight() - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent();
        g.setColor(color);
        g.drawString(from, 0, base);
        double x = fm.stringWidth(from) + size * 0.45;
        Icons.draw(g, Icons.ARROW_RIGHT, x, (getHeight() - arrow()) / 2.0, arrow(), Theme.ACCENT, 2.4);
        g.setColor(color);
        g.drawString(to, (float) (x + arrow() + size * 0.45), base);
        g.dispose();
    }
}
