package ui;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class Loader extends JComponent {

    private static final double IN = 0.45;
    private static final double BAR = 0.9;
    private static final double OUT_START = 0.95;
    private static final double OUT = 0.45;

    private final Timer timer;
    private double start;

    public Loader() {
        setOpaque(false);
        setVisible(false);
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseAdapter() {
        });
        timer = new Timer(16, e -> {
            repaint();
            if (Anim.now() - start > OUT_START + OUT) {
                ((Timer) e.getSource()).stop();
                setVisible(false);
            }
        });
    }

    public void play(Runnable behind) {
        start = Anim.now();
        setVisible(true);
        repaint();
        if (behind != null) {
            behind.run();
        }
        timer.restart();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        double t = Anim.now() - start;
        double out = Anim.clamp((t - OUT_START) / OUT);
        if (out >= 1) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1 - out)));
        int w = getWidth(), h = getHeight();
        float radius = (float) Math.max(w, h) * 0.6f;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h * 0.45), radius, new float[]{0f, 0.7f},
                new Color[]{new Color(0x1C1712), Theme.BG}));
        g.fillRect(0, 0, w, h);
        double in = Anim.easeOut(t / IN);
        double scale = 0.92 + 0.08 * in;
        Graphics2D c = (Graphics2D) g.create();
        c.setComposite(AlphaComposite.SrcOver.derive((float) ((1 - out) * Anim.clamp(t / IN))));
        c.translate(w / 2.0, h / 2.0);
        c.scale(scale, scale);
        double logo = 52;
        c.setFont(Theme.font(800, 28));
        double textW = c.getFontMetrics().stringWidth("RouteMatch");
        double total = logo + 16 + textW;
        double x = -total / 2;
        double y = -logo / 2 - 14;
        Logo.paintMark(c, x, y, logo, Anim.now());
        Logo.paintText(c, x + logo + 16, y + logo / 2, 28);
        double barW = 180, barH = 3, by = y + logo + 26;
        c.setColor(new Color(243, 240, 235, 26));
        c.fill(new RoundRectangle2D.Double(-barW / 2, by, barW, barH, 3, 3));
        double p = Anim.easeInOut(t / BAR);
        if (p > 0) {
            c.setPaint(new LinearGradientPaint(new Point2D.Double(-barW / 2, 0), new Point2D.Double(barW / 2, 0),
                    new float[]{0f, 1f}, new Color[]{Theme.TEXT, Theme.ACCENT}));
            c.fill(new RoundRectangle2D.Double(-barW / 2, by, barW * p, barH, 3, 3));
        }
        c.dispose();
        g.dispose();
    }
}
