package ui;

import common.Vehicle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Icons {

    private static final Pattern TOKEN = Pattern.compile("[MmLlHhVvCcSsQqAaZz]|[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?");

    public static final Shape HOME = parse("M3 10.5L12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z");
    public static final Shape PLUS = parse("M12 5v14M5 12h14");
    public static final Shape MINUS = parse("M5 12h14");
    public static final Shape SEARCH = parse("M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16zM21 21l-4.3-4.3");
    public static final Shape TICKET = parse("M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v3a2 2 0 0 0 0 4v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 0 0-4zM14 5v14");
    public static final Shape BELL = parse("M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0");
    public static final Shape CHEVRON_DOWN = parse("M6 9l6 6 6-6");
    public static final Shape CHEVRON_RIGHT = parse("M9 6l6 6-6 6");
    public static final Shape ARROW_RIGHT = parse("M5 12h14M13 6l6 6-6 6");
    public static final Shape ARROW_UP_RIGHT = parse("M7 17L17 7M9 7h8v8");
    public static final Shape USER = parse(circle(12, 8, 4) + "M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8");
    public static final Shape SERVER = parse("M5 4h14a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2zM5 13h14a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2zM7 7.5h.01M7 16.5h.01");
    public static final Shape SWAP = parse("M4 8h16M16 4l4 4-4 4M20 16H4M8 12l-4 4 4 4");
    public static final Shape CLOCK = parse(circle(12, 12, 9) + "M12 7v5l3 2");
    public static final Shape LOCK = parse("M6 11h12a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1zM8 11V7a4 4 0 0 1 8 0v4");
    public static final Shape CHECK = parse("M5 12.5l4.5 4.5L19 7.5");
    public static final Shape CLOSE = parse("M6 6l12 12M18 6L6 18");
    public static final Shape PIN = parse("M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z" + circle(12, 9.5, 2.5));
    public static final Shape TARGET = parse(circle(12, 12, 3) + "M12 2v4M12 18v4M2 12h4M18 12h4");
    public static final Shape EXPAND = parse("M4 9V4h5M20 9V4h-5M4 15v5h5M20 15v5h-5");
    public static final Shape LOGOUT = parse("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9");
    public static final Shape SEAT = parse("M7 13V6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v7M5 13h14v4H5zM7 17v3M17 17v3");
    public static final Shape USERS = parse(circle(9, 8, 3.5) + "M2.5 20c0-3.6 2.9-6.5 6.5-6.5s6.5 2.9 6.5 6.5M16 4.6a3.5 3.5 0 0 1 0 6.8M18 13.8c2.2.8 3.5 3 3.5 6.2");
    public static final Shape ROUTE = parse(circle(6, 19, 2.5) + circle(18, 5, 2.5) + "M8.5 19H16a3.5 3.5 0 0 0 0-7H8a3.5 3.5 0 0 1 0-7h7.5");
    public static final Shape CAR = parse("M3 15v-3l2.2-5.2A2 2 0 0 1 7 5.5h10a2 2 0 0 1 1.8 1.3L21 12v3a1 1 0 0 1-1 1h-1M5 16H4a1 1 0 0 1-1-1M3 12h18" + circle(7, 16.5, 2) + circle(17, 16.5, 2) + "M9 16.5h6");
    public static final Shape TUK = parse("M3 17V10a5 5 0 0 1 5-5h6l4 6h2a1 1 0 0 1 1 1v5M3 11h11V5" + circle(7, 17.5, 2) + circle(18, 17.5, 2) + "M9 17.5h7");
    public static final Shape BIKE = parse(circle(5.5, 16, 3.5) + circle(18.5, 16, 3.5) + "M5.5 16l4-6h5l4 6M14.5 10l-1.5-4h-3");
    public static final Shape VAN = parse("M2 16V7a2 2 0 0 1 2-2h11l5 5v6a1 1 0 0 1-1 1h-1M4 17H3a1 1 0 0 1-1-1M2 10h18M9 5v5" + circle(6.5, 17, 2) + circle(16.5, 17, 2) + "M8.5 17h6");

    private Icons() {
    }

    public static Shape vehicle(Vehicle v) {
        switch (v) {
            case TUK:
                return TUK;
            case BIKE:
                return BIKE;
            case VAN:
                return VAN;
            default:
                return CAR;
        }
    }

    public static String circle(double cx, double cy, double r) {
        return "M" + (cx - r) + " " + cy + "a" + r + " " + r + " 0 1 0 " + (2 * r) + " 0a" + r + " " + r + " 0 1 0 " + (-2 * r) + " 0";
    }

    public static void draw(Graphics2D g, Shape icon, double x, double y, double size, Color color, double stroke) {
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.scale(size / 24.0, size / 24.0);
        Shape s = at.createTransformedShape(icon);
        g.setColor(color);
        g.setStroke(new BasicStroke((float) (stroke * size / 24.0), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    public static Path2D parse(String d) {
        Path2D.Double path = new Path2D.Double();
        List<String> tokens = new ArrayList<>();
        Matcher m = TOKEN.matcher(d);
        while (m.find()) {
            tokens.add(m.group());
        }
        double x = 0, y = 0, sx = 0, sy = 0, lcx = 0, lcy = 0;
        char cmd = 'M';
        char prev = ' ';
        int i = 0;
        while (i < tokens.size()) {
            String t = tokens.get(i);
            if (Character.isLetter(t.charAt(0))) {
                cmd = t.charAt(0);
                i++;
                if (cmd == 'Z' || cmd == 'z') {
                    path.closePath();
                    x = sx;
                    y = sy;
                    prev = cmd;
                    continue;
                }
            }
            boolean rel = Character.isLowerCase(cmd);
            double ox = rel ? x : 0;
            double oy = rel ? y : 0;
            switch (Character.toUpperCase(cmd)) {
                case 'M': {
                    x = ox + num(tokens, i++);
                    y = oy + num(tokens, i++);
                    path.moveTo(x, y);
                    sx = x;
                    sy = y;
                    cmd = rel ? 'l' : 'L';
                    break;
                }
                case 'L': {
                    x = ox + num(tokens, i++);
                    y = oy + num(tokens, i++);
                    path.lineTo(x, y);
                    break;
                }
                case 'H': {
                    x = ox + num(tokens, i++);
                    path.lineTo(x, y);
                    break;
                }
                case 'V': {
                    y = oy + num(tokens, i++);
                    path.lineTo(x, y);
                    break;
                }
                case 'C': {
                    double x1 = ox + num(tokens, i++), y1 = oy + num(tokens, i++);
                    double x2 = ox + num(tokens, i++), y2 = oy + num(tokens, i++);
                    x = ox + num(tokens, i++);
                    y = oy + num(tokens, i++);
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lcx = x2;
                    lcy = y2;
                    break;
                }
                case 'S': {
                    char p = Character.toUpperCase(prev);
                    double x1 = (p == 'C' || p == 'S') ? 2 * x - lcx : x;
                    double y1 = (p == 'C' || p == 'S') ? 2 * y - lcy : y;
                    double x2 = ox + num(tokens, i++), y2 = oy + num(tokens, i++);
                    x = ox + num(tokens, i++);
                    y = oy + num(tokens, i++);
                    path.curveTo(x1, y1, x2, y2, x, y);
                    lcx = x2;
                    lcy = y2;
                    break;
                }
                case 'Q': {
                    double x1 = ox + num(tokens, i++), y1 = oy + num(tokens, i++);
                    x = ox + num(tokens, i++);
                    y = oy + num(tokens, i++);
                    path.quadTo(x1, y1, x, y);
                    break;
                }
                case 'A': {
                    double rx = Math.abs(num(tokens, i++)), ry = Math.abs(num(tokens, i++));
                    i++;
                    boolean large = num(tokens, i++) != 0;
                    boolean sweep = num(tokens, i++) != 0;
                    double ex = ox + num(tokens, i++), ey = oy + num(tokens, i++);
                    arc(path, x, y, rx, ry, large, sweep, ex, ey);
                    x = ex;
                    y = ey;
                    break;
                }
                default:
                    i++;
            }
            prev = cmd;
        }
        return path;
    }

    private static double num(List<String> tokens, int i) {
        return Double.parseDouble(tokens.get(i));
    }

    private static void arc(Path2D path, double x1, double y1, double rx, double ry, boolean large, boolean sweep, double x2, double y2) {
        if (rx == 0 || ry == 0 || (x1 == x2 && y1 == y2)) {
            path.lineTo(x2, y2);
            return;
        }
        double dx = (x1 - x2) / 2, dy = (y1 - y2) / 2;
        double lambda = dx * dx / (rx * rx) + dy * dy / (ry * ry);
        if (lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }
        double num = rx * rx * ry * ry - rx * rx * dy * dy - ry * ry * dx * dx;
        double den = rx * rx * dy * dy + ry * ry * dx * dx;
        double coef = (large == sweep ? -1 : 1) * Math.sqrt(Math.max(0, num / den));
        double cxp = coef * (rx * dy / ry);
        double cyp = coef * (-(ry * dx / rx));
        double cx = cxp + (x1 + x2) / 2, cy = cyp + (y1 + y2) / 2;
        double t1 = Math.atan2((dy - cyp) / ry, (dx - cxp) / rx);
        double t2 = Math.atan2((-dy - cyp) / ry, (-dx - cxp) / rx);
        double dt = t2 - t1;
        if (sweep && dt < 0) {
            dt += 2 * Math.PI;
        } else if (!sweep && dt > 0) {
            dt -= 2 * Math.PI;
        }
        Arc2D.Double a = new Arc2D.Double(cx - rx, cy - ry, 2 * rx, 2 * ry, -Math.toDegrees(t1), -Math.toDegrees(dt), Arc2D.OPEN);
        path.append(a, true);
    }

    public static Point2D center(Shape s) {
        return new Point2D.Double(s.getBounds2D().getCenterX(), s.getBounds2D().getCenterY());
    }
}
