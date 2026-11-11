package ui;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public class PillButton extends JButton {

    public enum Style { PRIMARY, ACCENT, GHOST, DANGER, SOFT, SAGE }

    private Style style;
    private Shape icon;
    private boolean iconRight = true;
    private int height = 44;
    private int radius = 999;
    private float fontSize = 15;

    public PillButton(String text, Style style) {
        super(text);
        this.style = style;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public PillButton icon(Shape icon, boolean right) {
        this.icon = icon;
        this.iconRight = right;
        return this;
    }

    public PillButton size(int height, float fontSize) {
        this.height = height;
        this.fontSize = fontSize;
        return this;
    }

    public PillButton radius(int radius) {
        this.radius = radius;
        return this;
    }

    public PillButton style(Style style) {
        this.style = style;
        repaint();
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.font(700, fontSize));
        int w = fm.stringWidth(getText()) + (int) (height * 0.9);
        if (icon != null) {
            w += (int) (fontSize * 1.2) + (getText().isEmpty() ? 0 : 8);
        }
        return new Dimension(Math.max(w, height), height);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        boolean hover = getModel().isRollover() && isEnabled();
        boolean down = getModel().isPressed() && isEnabled();
        Color bg;
        Color fg;
        Color border = null;
        switch (style) {
            case PRIMARY:
                bg = hover ? Color.WHITE : Theme.TEXT;
                fg = Theme.BG;
                break;
            case ACCENT:
                bg = hover ? Theme.ACCENT_HOVER : Theme.ACCENT;
                fg = Theme.INK;
                break;
            case DANGER:
                bg = Theme.alpha(Theme.DANGER, hover ? 0.14 : 0.06);
                fg = Theme.DANGER;
                border = Theme.alpha(Theme.DANGER, 0.35);
                break;
            case SOFT:
                bg = Theme.alpha(Theme.ACCENT, hover ? 0.2 : 0.12);
                fg = Theme.ACCENT;
                border = Theme.alpha(Theme.ACCENT, 0.3);
                break;
            case SAGE:
                bg = Theme.alpha(Theme.SAGE, 0.12);
                fg = Theme.SAGE_TEXT;
                border = Theme.alpha(Theme.SAGE, 0.3);
                break;
            default:
                bg = new Color(255, 255, 255, hover ? 22 : 10);
                fg = Theme.TEXT;
                border = new Color(255, 255, 255, 26);
        }
        if (!isEnabled()) {
            bg = Theme.LINE_SOFT;
            fg = Theme.FAINT;
            border = null;
        }
        double w = getWidth(), h = getHeight();
        double r = Math.min(radius * 2.0, h);
        double dy = down ? 1 : 0;
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5 + dy, w - 1, h - 1 - dy, r, r);
        g.setColor(bg);
        g.fill(shape);
        if (border != null) {
            g.setColor(border);
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
        }
        g.setFont(Theme.font(700, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(getText());
        double iconSize = fontSize * 1.2;
        double gap = getText().isEmpty() ? 0 : 8;
        double total = tw + (icon != null ? iconSize + gap : 0);
        double x = (w - total) / 2;
        float baseline = (float) ((h - fm.getAscent() - fm.getDescent()) / 2.0 + fm.getAscent() + dy);
        g.setColor(fg);
        if (icon != null && !iconRight) {
            Icons.draw(g, icon, x, (h - iconSize) / 2 + dy, iconSize, fg, 2.3);
            x += iconSize + gap;
        }
        g.setColor(fg);
        g.drawString(getText(), (float) x, baseline);
        if (icon != null && iconRight) {
            Icons.draw(g, icon, x + tw + 8, (h - iconSize) / 2 + dy, iconSize, fg, 2.3);
        }
        g.dispose();
    }
}
