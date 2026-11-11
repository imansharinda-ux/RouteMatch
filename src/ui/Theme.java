package ui;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class Theme {

    public static final Color BG = new Color(0x0D0C0B);
    public static final Color PANEL = new Color(0x161412);
    public static final Color PANEL_2 = new Color(0x1B1815);
    public static final Color FIELD = new Color(0x110F0E);
    public static final Color LINE = new Color(0x2E2925);
    public static final Color LINE_SOFT = new Color(0x241F1B);
    public static final Color TEXT = new Color(0xF3F0EB);
    public static final Color TEXT_2 = new Color(0xC9BBAE);
    public static final Color MUTED = new Color(0xA69B91);
    public static final Color DIM = new Color(0x8F8984);
    public static final Color FAINT = new Color(0x6E655D);
    public static final Color ACCENT = new Color(0xEDB07C);
    public static final Color ACCENT_HOVER = new Color(0xF6CBA1);
    public static final Color ACCENT_DEEP = new Color(0xD9844A);
    public static final Color ACCENT_LIGHT = new Color(0xF8E2C8);
    public static final Color ROUTE = new Color(0xF2C08F);
    public static final Color ROUTE_GLOW = new Color(0xE39A5E);
    public static final Color PIN = new Color(0xE8A06A);
    public static final Color SAGE = new Color(0xA8C5A0);
    public static final Color SAGE_TEXT = new Color(0xCFE0C9);
    public static final Color DANGER = new Color(0xF0A193);
    public static final Color SKY = new Color(0x9FB3C8);
    public static final Color INK = new Color(0x1A1411);

    private static final int[] WEIGHTS = {400, 500, 600, 700, 800};
    private static final Map<Integer, Font> BASE = new HashMap<>();
    private static final Map<String, Font> CACHE = new HashMap<>();

    private Theme() {
    }

    public static Font font(int weight, float size) {
        int w = WEIGHTS[0];
        for (int candidate : WEIGHTS) {
            if (Math.abs(candidate - weight) < Math.abs(w - weight)) {
                w = candidate;
            }
        }
        String key = w + "/" + size;
        Font cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Font base = BASE.get(w);
        if (base == null) {
            try (InputStream in = Theme.class.getResourceAsStream("/ui/fonts/InterTight-" + w + ".ttf")) {
                base = in == null ? null : Font.createFont(Font.TRUETYPE_FONT, in);
            } catch (Exception e) {
                base = null;
            }
            if (base == null) {
                base = new Font("SansSerif", w >= 600 ? Font.BOLD : Font.PLAIN, 12);
            }
            BASE.put(w, base);
        }
        Font f = base.deriveFont(size);
        CACHE.put(key, f);
        return f;
    }

    public static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.round(Math.max(0, Math.min(1, a)) * 255));
    }

    public static Color mix(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k),
                (int) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * k));
    }

    public static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public static java.awt.Dimension windowSize(int width, int height) {
        java.awt.Rectangle screen = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        return new java.awt.Dimension(Math.min(width, screen.width - 20), Math.min(height, screen.height - 20));
    }

    public static void install() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.opengl", "false");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Panel.background", new ColorUIResource(BG));
        UIManager.put("Viewport.background", new ColorUIResource(BG));
        UIManager.put("ScrollPane.background", new ColorUIResource(BG));
        UIManager.put("PopupMenu.background", new ColorUIResource(PANEL));
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(LINE));
        UIManager.put("MenuItem.background", new ColorUIResource(PANEL));
        UIManager.put("MenuItem.foreground", new ColorUIResource(TEXT));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(PANEL_2));
        UIManager.put("MenuItem.selectionForeground", new ColorUIResource(ACCENT));
        UIManager.put("MenuItem.font", font(600, 14));
        UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(8, 10, 8, 10));
        UIManager.put("ToolTip.background", new ColorUIResource(PANEL_2));
        UIManager.put("ToolTip.foreground", new ColorUIResource(TEXT));
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(LINE));
        UIManager.put("ToolTip.font", font(500, 13));
        UIManager.put("Table.background", new ColorUIResource(PANEL));
        UIManager.put("Table.foreground", new ColorUIResource(TEXT));
        UIManager.put("Label.foreground", new ColorUIResource(TEXT));
    }
}
