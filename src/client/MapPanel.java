package client;

import common.Places;
import ui.Anim;
import ui.Icons;
import ui.PillButton;
import ui.RoundPanel;
import ui.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapPanel extends JPanel {

    private static final class Layer {
        final Shape path;
        final Color fill;
        final Color stroke;
        final float width;

        Layer(Shape path, Color fill, Color stroke, float width) {
            this.path = path;
            this.fill = fill;
            this.stroke = stroke;
            this.width = width;
        }
    }

    private static final Shape PIN = Icons.parse("M0,0 C-6,-9 -12,-15 -12,-24 A12,12 0 1 1 12,-24 C12,-15 6,-9 0,0 Z");
    private static final String[] PIN_PLACES = {"col", "kan", "gal", "jaf", "tri", "ell", "anu", "bat", "aru"};
    private static List<Layer> layers;
    private static Rectangle2D island;

    private List<Point2D> route = new ArrayList<>();
    private double routeLength;
    private double routeStart;
    private List<List<Point2D>> others = new ArrayList<>();
    private String fromLabel = "";
    private String toLabel = "";
    private boolean labels = true;
    private boolean pins;
    private boolean kenburns;
    private double shift;
    private Insets fitPad = new Insets(60, 60, 60, 90);
    private String mode = "route";
    private double cx;
    private double cy;
    private double s;
    private double tcx;
    private double tcy;
    private double ts;
    private boolean ready;
    private BufferedImage cache;
    private String cacheKey = "";
    private int radius = 24;
    private Color corner = Theme.BG;
    private Point drag;
    private final double pinStart = Anim.now();
    private final boolean interactive;

    public MapPanel(boolean interactive, boolean controls) {
        super(null);
        this.interactive = interactive;
        setOpaque(true);
        setBackground(new Color(0x0F1215));
        load();
        Anim.register(this);
        if (interactive) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    drag = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    drag = null;
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (drag == null) {
                        return;
                    }
                    double dx = (e.getX() - drag.x) / s;
                    double dy = (e.getY() - drag.y) / s;
                    cx -= dx;
                    cy -= dy;
                    tcx = cx;
                    tcy = cy;
                    ts = s;
                    mode = "free";
                    drag = e.getPoint();
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    zoomAt(e.getWheelRotation() < 0 ? 1.2 : 1 / 1.2, e.getX(), e.getY());
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
            addMouseWheelListener(m);
        }
        if (controls) {
            RoundPanel box = new RoundPanel(new GridLayout(4, 1, 0, 2), new Color(22, 20, 18, 235), Theme.LINE, 14);
            box.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            box.add(control(Icons.PLUS, "Zoom in", () -> zoomAt(1.4, getWidth() / 2, getHeight() / 2)));
            box.add(control(Icons.MINUS, "Zoom out", () -> zoomAt(1 / 1.4, getWidth() / 2, getHeight() / 2)));
            box.add(control(Icons.TARGET, "Fit route", this::fitRoute));
            box.add(control(Icons.EXPAND, "Show all of Sri Lanka", this::showIsland));
            overlay(box, "e");
        }
    }

    private JComponent control(Shape icon, String tip, Runnable action) {
        PillButton b = new PillButton("", PillButton.Style.GHOST).icon(icon, false).size(38, 14).radius(10);
        b.setToolTipText(tip);
        b.addActionListener(e -> action.run());
        return b;
    }

    private static synchronized void load() {
        if (layers != null) {
            return;
        }
        layers = new ArrayList<>();
        try (InputStream in = MapPanel.class.getResourceAsStream("/client/srilanka.txt")) {
            if (in != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = r.readLine()) != null) {
                    int bar = line.indexOf('|');
                    if (bar < 0) {
                        continue;
                    }
                    String[] style = line.substring(0, bar).trim().split("\\s+");
                    Shape path = Icons.parse(line.substring(bar + 1));
                    layers.add(new Layer(path, color(style[0]), color(style[1]), Float.parseFloat(style[2])));
                }
            }
        } catch (Exception ignored) {
        }
        island = layers.isEmpty() ? new Rectangle2D.Double(80, 60, 900, 1560) : layers.get(0).path.getBounds2D();
    }

    private static Color color(String text) {
        return text.equals("none") ? null : Color.decode(text);
    }

    public MapPanel labels(boolean labels) {
        this.labels = labels;
        return this;
    }

    public MapPanel pins(boolean pins) {
        this.pins = pins;
        return this;
    }

    public MapPanel kenburns(boolean kenburns) {
        this.kenburns = kenburns;
        return this;
    }

    public MapPanel shift(double shift) {
        this.shift = shift;
        return this;
    }

    public MapPanel corner(Color corner, int radius) {
        this.corner = corner;
        this.radius = radius;
        return this;
    }

    public MapPanel fitPadding(int top, int left, int bottom, int right) {
        this.fitPad = new Insets(top, left, bottom, right);
        if (mode.equals("route")) {
            target(false);
        }
        return this;
    }

    public void overlay(JComponent c, String position) {
        c.putClientProperty("map.pos", position);
        c.addMouseListener(new MouseAdapter() {
        });
        c.addMouseWheelListener(e -> {
        });
        add(c, 0);
        revalidate();
    }

    public void setRoute(List<double[]> points, String from, String to) {
        List<Point2D> pts = new ArrayList<>();
        for (double[] p : points) {
            pts.add(new Point2D.Double(p[0], p[1]));
        }
        boolean same = pts.equals(route) && from.equals(fromLabel) && to.equals(toLabel);
        fromLabel = from;
        toLabel = to;
        if (same) {
            return;
        }
        route = pts;
        routeLength = 0;
        for (int i = 1; i < route.size(); i++) {
            routeLength += route.get(i).distance(route.get(i - 1));
        }
        routeStart = Anim.now();
        mode = "route";
        target(!ready);
    }

    public void clearRoute() {
        if (route.isEmpty()) {
            return;
        }
        route = new ArrayList<>();
        routeLength = 0;
        fromLabel = "";
        toLabel = "";
        mode = "island";
        target(!ready);
    }

    public void setOthers(List<List<double[]>> lines) {
        List<List<Point2D>> list = new ArrayList<>();
        for (List<double[]> line : lines) {
            List<Point2D> pts = new ArrayList<>();
            for (double[] p : line) {
                pts.add(new Point2D.Double(p[0], p[1]));
            }
            list.add(pts);
        }
        others = list;
    }

    public void fitRoute() {
        mode = route.isEmpty() ? "island" : "route";
        target(false);
    }

    public void showIsland() {
        mode = "island";
        target(false);
    }

    private double islandScale(int w, int h) {
        return Math.min((w - 40.0) / island.getWidth(), (h - 40.0) / island.getHeight());
    }

    private void target(boolean snap) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (mode.equals("route") && route.size() > 0) {
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (Point2D p : route) {
                minX = Math.min(minX, p.getX());
                minY = Math.min(minY, p.getY());
                maxX = Math.max(maxX, p.getX());
                maxY = Math.max(maxY, p.getY());
            }
            double bw = Math.max(140, maxX - minX), bh = Math.max(140, maxY - minY);
            double mx = (minX + maxX) / 2, my = (minY + maxY) / 2;
            double aw = Math.max(80, w - fitPad.left - fitPad.right);
            double ah = Math.max(80, h - fitPad.top - fitPad.bottom);
            ts = Math.min(aw / bw, ah / bh);
            ts = Math.min(ts, islandScale(w, h) * 9);
            tcx = mx - (fitPad.left - fitPad.right) / 2.0 / ts;
            tcy = my - (fitPad.top - fitPad.bottom) / 2.0 / ts;
        } else {
            ts = islandScale(w, h);
            tcx = island.getCenterX() - shift * w / ts;
            tcy = island.getCenterY();
        }
        if (snap || !ready) {
            cx = tcx;
            cy = tcy;
            s = ts;
            ready = true;
        }
    }

    private void zoomAt(double factor, int mx, int my) {
        int w = getWidth(), h = getHeight();
        double base = islandScale(w, h);
        double ns = Math.max(base * 0.7, Math.min(base * 14, ts * factor));
        double wx = tcx + (mx - w / 2.0) / ts;
        double wy = tcy + (my - h / 2.0) / ts;
        tcx = wx - (mx - w / 2.0) / ns;
        tcy = wy - (my - h / 2.0) / ns;
        ts = ns;
        mode = "free";
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        boolean changed = width != getWidth() || height != getHeight();
        super.setBounds(x, y, width, height);
        if (changed && width > 0 && height > 0) {
            boolean first = !ready;
            if (mode.equals("free")) {
                if (first) {
                    mode = "island";
                }
            }
            if (!mode.equals("free")) {
                target(true);
            }
        }
    }

    @Override
    public void doLayout() {
        int w = getWidth(), h = getHeight(), m = 16;
        for (Component c : getComponents()) {
            if (!(c instanceof JComponent) || !c.isVisible()) {
                continue;
            }
            String pos = (String) ((JComponent) c).getClientProperty("map.pos");
            Dimension d = c.getPreferredSize();
            int x = m, y = m, cw = d.width, ch = d.height;
            if (pos == null) {
                pos = "nw";
            }
            switch (pos) {
                case "ne":
                    x = w - m - cw;
                    break;
                case "sw":
                    y = h - m - ch;
                    break;
                case "se":
                    x = w - m - cw;
                    y = h - m - ch;
                    break;
                case "s":
                    cw = w - 2 * m;
                    y = h - m - ch;
                    break;
                case "e":
                    x = w - m - cw;
                    y = (h - ch) / 2;
                    break;
                case "n":
                    x = (w - cw) / 2;
                    break;
                default:
                    break;
            }
            c.setBounds(x, y, Math.min(cw, w - 2 * m), ch);
        }
    }

    private void step() {
        double k = 0.16;
        if (Math.abs(ts - s) / Math.max(s, 1e-9) < 0.0005 && Math.abs(tcx - cx) * s < 0.3 && Math.abs(tcy - cy) * s < 0.3) {
            cx = tcx;
            cy = tcy;
            s = ts;
            return;
        }
        s += (ts - s) * k;
        cx += (tcx - cx) * k;
        cy += (tcy - cy) * k;
    }

    private Point2D toScreen(double wx, double wy, int w, int h) {
        return new Point2D.Double((wx - cx) * s + w / 2.0, (wy - cy) * s + h / 2.0);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (!ready) {
            target(true);
        }
        step();
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double t = Anim.now();
        if (kenburns) {
            double k = 1.02 + 0.06 * (0.5 - 0.5 * Math.cos(2 * Math.PI * t / 40.0));
            g.translate(w * 0.62, h * 0.55);
            g.scale(k, k);
            g.translate(-w * 0.62, -h * 0.55);
        }
        double dpr = g0 instanceof Graphics2D ? ((Graphics2D) g0).getTransform().getScaleX() : 1;
        String key = w + "x" + h + "@" + cx + "," + cy + "," + s + "," + dpr + "," + labels + "," + fromLabel + "," + toLabel;
        if (cache == null || !key.equals(cacheKey)) {
            int iw = (int) Math.ceil(w * dpr), ih = (int) Math.ceil(h * dpr);
            if (cache == null || cache.getWidth() != iw || cache.getHeight() != ih) {
                cache = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D cg = cache.createGraphics();
            Theme.hints(cg);
            cg.scale(dpr, dpr);
            renderBase(cg, w, h);
            cg.dispose();
            cacheKey = key;
        }
        g.drawImage(cache, 0, 0, w, h, null);
        renderOthers(g, w, h);
        renderRoute(g, w, h, t);
        if (pins) {
            renderPins(g, w, h, t);
        }
        g.dispose();
        Graphics2D f = (Graphics2D) g0.create();
        Theme.hints(f);
        if (radius > 0) {
            RoundRectangle2D rr = new RoundRectangle2D.Double(0, 0, w, h, radius * 2.0, radius * 2.0);
            Area outside = new Area(new Rectangle2D.Double(-1, -1, w + 2, h + 2));
            outside.subtract(new Area(rr));
            f.setColor(corner);
            f.fill(outside);
            f.setColor(new Color(255, 255, 255, 18));
            f.setStroke(new BasicStroke(1f));
            f.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, radius * 2.0, radius * 2.0));
        }
        f.dispose();
    }

    private void renderBase(Graphics2D g, int w, int h) {
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h / 2.0), (float) (Math.max(w, h) * 0.75),
                new float[]{0f, 1f}, new Color[]{new Color(0x161A1E), new Color(0x0B0D0F)}));
        g.fillRect(0, 0, w, h);
        AffineTransform at = new AffineTransform();
        at.translate(w / 2.0, h / 2.0);
        at.scale(s, s);
        at.translate(-cx, -cy);
        for (Layer layer : layers) {
            Shape shape = at.createTransformedShape(layer.path);
            if (layer.fill != null) {
                g.setColor(layer.fill);
                g.fill(shape);
            }
            if (layer.stroke != null && layer.width > 0) {
                g.setColor(layer.stroke);
                g.setStroke(new BasicStroke(layer.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(shape);
            }
        }
        if (!labels) {
            return;
        }
        double zoom = s / islandScale(w, h);
        Font sea = Theme.font(500, (float) Math.max(11, Math.min(15, 11 * Math.sqrt(zoom))))
                .deriveFont(Map.of(TextAttribute.TRACKING, 0.35, TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE));
        g.setFont(sea);
        g.setColor(new Color(0x3C434A));
        seaLabel(g, "INDIAN OCEAN", 520, 1760, w, h);
        seaLabel(g, "BAY OF BENGAL", 1080, 560, w, h);
        seaLabel(g, "GULF OF MANNAR", -60, 560, w, h);
        boolean minor = zoom >= 1.7;
        for (Places.Place p : Places.all()) {
            if ((!p.major && !minor) || p.name.equals(fromLabel) || p.name.equals(toLabel)) {
                continue;
            }
            Point2D sp = toScreen(p.x, p.y, w, h);
            if (sp.getX() < -80 || sp.getY() < -20 || sp.getX() > w + 80 || sp.getY() > h + 20) {
                continue;
            }
            double r = p.major ? 3.2 : 2.4;
            g.setColor(p.major ? Theme.TEXT_2 : Theme.DIM);
            g.fill(new Ellipse2D.Double(sp.getX() - r, sp.getY() - r, 2 * r, 2 * r));
            Font font = Theme.font(p.major ? 600 : 500, p.major ? 12.5f : 11.5f);
            halo(g, p.name, font, sp.getX(), sp.getY(), p.labelLeft, p.major ? new Color(0xD6C2AE) : Theme.MUTED, 3.5f);
        }
    }

    private void seaLabel(Graphics2D g, String text, double wx, double wy, int w, int h) {
        Point2D p = toScreen(wx, wy, w, h);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (p.getX() - fm.stringWidth(text) / 2.0), (float) p.getY());
    }

    private void halo(Graphics2D g, String text, Font font, double x, double y, boolean left, Color color, float haloWidth) {
        TextLayout tl = new TextLayout(text, font, g.getFontRenderContext());
        double em = font.getSize2D();
        double tx = left ? x - em * 0.75 - tl.getAdvance() : x + em * 0.75;
        double ty = y + em * 0.35;
        Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(tx, ty));
        g.setColor(Theme.alpha(Theme.BG, 0.9));
        g.setStroke(new BasicStroke(haloWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);
        g.setColor(color);
        g.fill(outline);
    }

    private Path2D screenPath(List<Point2D> pts, int w, int h, double upTo) {
        Path2D.Double path = new Path2D.Double();
        if (pts.isEmpty()) {
            return path;
        }
        Point2D first = toScreen(pts.get(0).getX(), pts.get(0).getY(), w, h);
        path.moveTo(first.getX(), first.getY());
        double walked = 0;
        for (int i = 1; i < pts.size(); i++) {
            Point2D a = pts.get(i - 1), b = pts.get(i);
            double seg = a.distance(b);
            if (walked + seg >= upTo) {
                double f = seg == 0 ? 0 : (upTo - walked) / seg;
                Point2D end = toScreen(a.getX() + (b.getX() - a.getX()) * f, a.getY() + (b.getY() - a.getY()) * f, w, h);
                path.lineTo(end.getX(), end.getY());
                return path;
            }
            walked += seg;
            Point2D sp = toScreen(b.getX(), b.getY(), w, h);
            path.lineTo(sp.getX(), sp.getY());
        }
        return path;
    }

    private Point2D along(List<Point2D> pts, double dist) {
        double walked = 0;
        for (int i = 1; i < pts.size(); i++) {
            Point2D a = pts.get(i - 1), b = pts.get(i);
            double seg = a.distance(b);
            if (walked + seg >= dist) {
                double f = seg == 0 ? 0 : (dist - walked) / seg;
                return new Point2D.Double(a.getX() + (b.getX() - a.getX()) * f, a.getY() + (b.getY() - a.getY()) * f);
            }
            walked += seg;
        }
        return pts.get(pts.size() - 1);
    }

    private void renderOthers(Graphics2D g, int w, int h) {
        if (others.isEmpty()) {
            return;
        }
        g.setColor(new Color(179, 164, 147, 110));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (List<Point2D> line : others) {
            g.draw(screenPath(line, w, h, Double.MAX_VALUE));
            if (!line.isEmpty()) {
                Point2D a = toScreen(line.get(0).getX(), line.get(0).getY(), w, h);
                g.fill(new Ellipse2D.Double(a.getX() - 3, a.getY() - 3, 6, 6));
            }
        }
    }

    private void renderRoute(Graphics2D g, int w, int h, double t) {
        if (route.isEmpty()) {
            return;
        }
        double p = Anim.easeInOut((t - routeStart) / 0.9);
        Point2D start = toScreen(route.get(0).getX(), route.get(0).getY(), w, h);
        Point2D endW = route.get(route.size() - 1);
        Point2D end = toScreen(endW.getX(), endW.getY(), w, h);
        if (routeLength > 0) {
            Path2D path = screenPath(route, w, h, routeLength * p);
            g.setColor(new Color(227, 154, 94, 64));
            g.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(path);
            g.setColor(new Color(13, 12, 11, 190));
            g.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(path);
            float phase = (float) (19 - ((t * 32) % 19));
            g.setColor(Theme.ROUTE);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{10f, 9f}, phase));
            g.draw(path);
            if (p >= 1) {
                double dur = Math.max(3, Math.min(9, routeLength / 90));
                double f = ((t - routeStart - 1.3) / dur) % 1.0;
                if (f >= 0) {
                    Point2D cw = along(route, routeLength * f);
                    Point2D car = toScreen(cw.getX(), cw.getY(), w, h);
                    g.setColor(Color.WHITE);
                    g.fill(new Ellipse2D.Double(car.getX() - 6, car.getY() - 6, 12, 12));
                    g.setColor(Theme.ROUTE_GLOW);
                    g.setStroke(new BasicStroke(2.5f));
                    g.draw(new Ellipse2D.Double(car.getX() - 6, car.getY() - 6, 12, 12));
                }
            }
        }
        double pulse = (t % 1.8) / 1.8;
        double pr = 7 + 9 * pulse;
        g.setColor(Theme.alpha(Theme.TEXT, 0.35 * (1 - pulse)));
        g.fill(new Ellipse2D.Double(start.getX() - pr, start.getY() - pr, 2 * pr, 2 * pr));
        g.setColor(Theme.TEXT);
        g.fill(new Ellipse2D.Double(start.getX() - 7, start.getY() - 7, 14, 14));
        g.setColor(Theme.BG);
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new Ellipse2D.Double(start.getX() - 7, start.getY() - 7, 14, 14));
        if (route.size() > 1 && p > 0.85) {
            double drop = Anim.easeOut((p - 0.85) / 0.15);
            AffineTransform at = new AffineTransform();
            at.translate(end.getX(), end.getY() - 14 * (1 - drop));
            at.scale(0.9, 0.9);
            Shape pin = at.createTransformedShape(PIN);
            g.setColor(Theme.PIN);
            g.fill(pin);
            g.setColor(Theme.BG);
            g.setStroke(new BasicStroke(2f));
            g.draw(pin);
            Point2D hole = at.transform(new Point2D.Double(0, -24), null);
            g.fill(new Ellipse2D.Double(hole.getX() - 4, hole.getY() - 4, 8, 8));
        }
        Font font = Theme.font(700, 13.5f);
        if (!fromLabel.isEmpty()) {
            pinLabel(g, fromLabel, font, start.getX() + 14, start.getY() - 10);
        }
        if (!toLabel.isEmpty() && route.size() > 1 && p > 0.85) {
            pinLabel(g, toLabel, font, end.getX() + 16, end.getY() - 26);
        }
    }

    private void pinLabel(Graphics2D g, String text, Font font, double x, double y) {
        TextLayout tl = new TextLayout(text, font, g.getFontRenderContext());
        Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
        g.setColor(Theme.BG);
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(outline);
        g.setColor(Color.WHITE);
        g.fill(outline);
    }

    private void renderPins(Graphics2D g, int w, int h, double t) {
        for (int i = 0; i < PIN_PLACES.length; i++) {
            Places.Place p = Places.get(PIN_PLACES[i]);
            double local = t - pinStart - 0.4 - 0.3 * i;
            if (local <= 0) {
                continue;
            }
            double drop = Anim.easeOut(local / 0.6);
            Point2D sp = toScreen(p.x, p.y, w, h);
            double ring = (local % 2.2) / 2.2;
            double rr = 4 + 14 * ring;
            g.setColor(Theme.alpha(Theme.ROUTE, 0.7 * (1 - ring) * drop));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new Ellipse2D.Double(sp.getX() - rr, sp.getY() - rr, 2 * rr, 2 * rr));
            AffineTransform at = new AffineTransform();
            at.translate(sp.getX(), sp.getY() - 30 * (1 - drop));
            at.scale(0.55, 0.55);
            Shape pin = at.createTransformedShape(PIN);
            g.setColor(Theme.alpha(Theme.PIN, drop));
            g.fill(pin);
            g.setColor(Theme.alpha(Theme.BG, drop));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(pin);
            Point2D hole = at.transform(new Point2D.Double(0, -24), null);
            g.fill(new Ellipse2D.Double(hole.getX() - 2.5, hole.getY() - 2.5, 5, 5));
        }
    }

    public static List<List<double[]>> routesOf(List<String[]> pairs) {
        List<List<double[]>> list = new ArrayList<>();
        for (String[] pair : pairs) {
            list.add(Places.points(pair[0], pair[1]));
        }
        return list;
    }

    public static List<double[]> none() {
        return Collections.emptyList();
    }
}
