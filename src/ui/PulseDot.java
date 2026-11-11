package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class PulseDot extends JComponent {

    private Color color;
    private boolean pulse = true;

    public PulseDot(Color color) {
        this.color = color;
        Anim.register(this);
    }

    public void set(Color color, boolean pulse) {
        this.color = color;
        this.pulse = pulse;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(14, 14);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
        if (pulse) {
            double p = (Anim.now() % 1.8) / 1.8;
            double r = 3 + 4 * Math.sin(Math.PI * p);
            g.setColor(Theme.alpha(color, 0.3));
            g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        }
        g.setColor(color);
        g.fill(new Ellipse2D.Double(cx - 3.5, cy - 3.5, 7, 7));
        g.dispose();
    }
}
