package ui;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class Logo extends JComponent {

    private static final Shape TRAIL = Icons.parse("M13 35 C 13 24, 34 27, 33 15");
    private static final Shape PIN = Icons.parse("M33 21 C 29.4 16.6 27.4 13.8 27.4 11.4 A 5.6 5.6 0 1 1 38.6 11.4 C 38.6 13.8 36.6 16.6 33 21 Z");

    private final int size;
    private final boolean showText;

    public Logo(int size, boolean showText) {
        this.size = size;
        this.showText = showText;
        setOpaque(false);
        Anim.register(this);
    }

    private float textSize() {
        return Math.round(size * 0.55f);
    }

    private int gap() {
        return Math.round(size * 0.3f);
    }

    @Override
    public Dimension getPreferredSize() {
        if (!showText) {
            return new Dimension(size, size);
        }
        FontMetrics fm = getFontMetrics(Theme.font(800, textSize()));
        return new Dimension(size + gap() + fm.stringWidth("RouteMatch") + 4, size);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        int y = (getHeight() - size) / 2;
        paintMark(g, 0, y, size, Anim.now());
        if (showText) {
            paintText(g, size + gap(), getHeight() / 2.0, textSize());
        }
        g.dispose();
    }

    public static void paintText(Graphics2D g, double x, double centerY, float textSize) {
        g.setFont(Theme.font(800, textSize));
        FontMetrics fm = g.getFontMetrics();
        float baseline = (float) (centerY + (fm.getAscent() - fm.getDescent()) / 2.0);
        g.setColor(Theme.TEXT);
        g.drawString("Route", (float) x, baseline);
        g.setColor(Theme.ACCENT);
        g.drawString("Match", (float) x + fm.stringWidth("Route"), baseline);
    }

    public static void paintMark(Graphics2D g0, double x, double y, double size, double t) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        double k = size / 48.0;
        g.scale(k, k);
        RoundRectangle2D box = new RoundRectangle2D.Double(0, 0, 48, 48, 28, 28);
        g.setPaint(new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(48, 48), new float[]{0f, 0.55f, 1f},
                new Color[]{Theme.ACCENT_LIGHT, Theme.ACCENT, Theme.ACCENT_DEEP}));
        g.fill(box);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(37.4, 10.6), 26.4f, new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 140), new Color(255, 255, 255, 0)}));
        g.fill(box);
        float phase = (float) (6.3 - ((t / 0.9) % 1.0) * 6.3);
        g.setColor(Theme.INK);
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{0.1f, 6.2f}, phase));
        g.draw(TRAIL);
        double p = (t % 1.8) / 1.8;
        double r = 4 + 5 * p;
        g.setColor(Theme.alpha(Theme.INK, 0.6 * (1 - p)));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new Ellipse2D.Double(13 - r, 35 - r, 2 * r, 2 * r));
        g.setColor(Theme.INK);
        g.fill(new Ellipse2D.Double(13 - 4.4, 35 - 4.4, 8.8, 8.8));
        g.setColor(Theme.ACCENT_LIGHT);
        g.fill(new Ellipse2D.Double(13 - 1.7, 35 - 1.7, 3.4, 3.4));
        double bob = -2.2 * Math.sin(Math.PI * ((t % 1.6) / 1.6));
        AffineTransform at = AffineTransform.getTranslateInstance(0, bob);
        g.setColor(Theme.INK);
        g.fill(at.createTransformedShape(PIN));
        g.setColor(Theme.ACCENT_LIGHT);
        g.fill(new Ellipse2D.Double(33 - 2.2, 11.4 - 2.2 + bob, 4.4, 4.4));
        g.dispose();
    }
}
