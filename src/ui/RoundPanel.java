package ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.geom.RoundRectangle2D;

public class RoundPanel extends JPanel {

    private Color fill;
    private Color border;
    private int radius;
    private Color glow;

    public RoundPanel(LayoutManager layout, Color fill, Color border, int radius) {
        super(layout);
        this.fill = fill;
        this.border = border;
        this.radius = radius;
        setOpaque(false);
    }

    public RoundPanel setFill(Color fill) {
        this.fill = fill;
        repaint();
        return this;
    }

    public RoundPanel setBorderColor(Color border) {
        this.border = border;
        repaint();
        return this;
    }

    public RoundPanel setGlow(Color glow) {
        this.glow = glow;
        repaint();
        return this;
    }

    public int radius() {
        return radius;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double w = getWidth(), h = getHeight();
        double r = Math.min(radius * 2.0, Math.min(w, h));
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, r, r);
        if (glow != null) {
            g.setColor(glow);
            g.setStroke(new BasicStroke(6f));
            g.draw(shape);
        }
        if (fill != null) {
            g.setColor(fill);
            g.fill(shape);
        }
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
        }
        g.dispose();
    }
}
