package client;

import common.Places;
import common.Protocol;
import ui.Anim;
import ui.Icons;
import ui.Logo;
import ui.PillButton;
import ui.PulseDot;
import ui.RoundPanel;
import ui.TextBox;
import ui.Theme;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginScreen extends Screen {

    public enum Status { IDLE, BUSY, ERROR, OK }

    private final MapPanel map = new MapPanel(false, false);
    private final TextBox username = new TextBox(Icons.USER, "Username", false);
    private final TextBox host = new TextBox(Icons.SERVER, "Server IP", false);
    private final TextBox.Input port = new TextBox.Input("Port");
    private final PillButton connect = new PillButton("Connect", PillButton.Style.PRIMARY);
    private final PulseDot dot = new PulseDot(Theme.SAGE);
    private final JLabel status = Ui.label("Not connected yet", 500, 13, Theme.DIM);

    public LoginScreen(ClientApp app) {
        super(app);
        setLayout(null);
        map.labels(false).pins(true).kenburns(true).shift(0.22).corner(Theme.BG, 0);
        map.setRoute(Places.points("col", "ell"), "", "");
        List<List<double[]>> others = new ArrayList<>();
        String[][] pairs = {{"col", "gal"}, {"gal", "mat"}, {"kan", "jaf"}, {"dam", "tri"}, {"dam", "bat"}, {"ell", "aru"}, {"col", "put"}};
        for (String[] pair : pairs) {
            others.add(Places.points(pair[0], pair[1]));
        }
        map.setOthers(others);
        map.showIsland();
        Front front = new Front();
        add(front);
        add(map);
        front.add(Ui.pad(Ui.row(0, new Logo(44, true)), 0, 0, 0, 0), BorderLayout.NORTH);
        JPanel holder = Ui.clear(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 30, 0);
        holder.add(content(), c);
        front.add(holder, BorderLayout.CENTER);
        host.setText("localhost");
        port.setText(String.valueOf(Protocol.DEFAULT_PORT));
        port.setPreferredSize(new Dimension(56, 30));
        connect.addActionListener(e -> submit());
        ((javax.swing.JTextField) username.field()).addActionListener(e -> submit());
        ((javax.swing.JTextField) host.field()).addActionListener(e -> submit());
        port.addActionListener(e -> submit());
    }

    private JComponent content() {
        JLabel text = Ui.wrap("Offer your empty seats or join someone already heading your way.", 500, 16, Theme.MUTED, 400);
        JPanel column = Ui.column(28, new Heading(), text, form());
        column.setPreferredSize(new Dimension(620, column.getPreferredSize().height));
        return column;
    }

    private JComponent form() {
        Card card = new Card();
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 22));
        JPanel titles = Ui.column(2, Ui.label("Sign in", 700, 18, Theme.TEXT), Ui.label("Connect to a RouteMatch server", 500, 13, Theme.DIM));
        JPanel head = Ui.row(12, Ui.fill(titles), new MiniRoute());
        Color field = Theme.alpha(Theme.FIELD, 0.65);
        username.fill(field);
        host.fill(field);
        JPanel divider = Ui.clear(null);
        divider.setPreferredSize(new Dimension(1, 22));
        divider.setOpaque(true);
        divider.setBackground(new Color(0x2E2A26));
        host.addTrailing(divider);
        host.addTrailing(port);
        connect.icon(Icons.ARROW_RIGHT, true).size(52, 16).radius(16);
        JPanel statusRow = Ui.row(8, dot, Ui.fill(status), Ui.label("TCP", 600, 13, Theme.FAINT));
        statusRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 14)),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)));
        JPanel col = Ui.column(10, head, Ui.space(1, 4), username, host, Ui.space(1, 2), connect, Ui.space(1, 2), statusRow);
        card.add(col, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(420, card.getPreferredSize().height));
        JPanel wrap = Ui.clear(new BorderLayout());
        wrap.add(card, BorderLayout.WEST);
        return wrap;
    }

    private void submit() {
        if (!connect.isEnabled()) {
            return;
        }
        String name = Protocol.clean(username.text(), 20);
        if (name.isEmpty()) {
            status(Status.ERROR, "Enter a username to continue");
            username.field().requestFocusInWindow();
            return;
        }
        String h = host.text().isEmpty() ? "localhost" : host.text();
        int p;
        try {
            p = Integer.parseInt(port.getText().trim());
        } catch (NumberFormatException e) {
            status(Status.ERROR, "Port must be a number like 5000");
            return;
        }
        app.connect(name, h, p);
    }

    public void status(Status s, String text) {
        status.setText(text);
        connect.setEnabled(s != Status.BUSY);
        switch (s) {
            case BUSY:
                dot.set(Theme.ACCENT, true);
                status.setForeground(Theme.TEXT_2);
                break;
            case ERROR:
                dot.set(Theme.DANGER, false);
                status.setForeground(Theme.DANGER);
                break;
            case OK:
                dot.set(Theme.SAGE, true);
                status.setForeground(Theme.SAGE_TEXT);
                break;
            default:
                dot.set(Theme.SAGE, false);
                status.setForeground(Theme.DIM);
        }
    }

    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void doLayout() {
        for (Component c : getComponents()) {
            c.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void shown() {
        username.field().requestFocusInWindow();
    }

    @Override
    public void refresh() {
    }

    private static final class Front extends JPanel {
        Front() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            int w = getWidth(), h = getHeight();
            g.setPaint(new LinearGradientPaint(new Point2D.Double(0, 0), new Point2D.Double(w, 0), new float[]{0f, 0.34f, 0.66f, 1f},
                    new Color[]{new Color(13, 12, 11, 247), new Color(13, 12, 11, 219), new Color(13, 12, 11, 64), new Color(13, 12, 11, 26)}));
            g.fillRect(0, 0, w, h);
            g.setPaint(new RadialGradientPaint(new Point2D.Double(w * 0.85, h * 0.08), (float) (w * 0.4), new float[]{0f, 1f},
                    new Color[]{new Color(237, 176, 124, 56), new Color(237, 176, 124, 0)}));
            g.fillRect(0, 0, w, h);
            g.setPaint(new GradientPaint(0, (float) (h * 0.7), new Color(13, 12, 11, 0), 0, h, new Color(13, 12, 11, 230)));
            g.fillRect(0, (int) (h * 0.7), w, h - (int) (h * 0.7));
            g.dispose();
        }
    }

    private static final class Card extends RoundPanel {
        Card() {
            super(new BorderLayout(), new Color(22, 20, 18, 200), new Color(255, 255, 255, 26), 24);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            float w = getWidth();
            g.setPaint(new LinearGradientPaint(new Point2D.Float(28, 0), new Point2D.Float(w - 28, 0), new float[]{0f, 0.5f, 1f},
                    new Color[]{Theme.alpha(Theme.ACCENT, 0), Theme.ACCENT, Theme.alpha(Theme.ACCENT, 0)}));
            g.fill(new java.awt.geom.Rectangle2D.Float(28, 0, w - 56, 1.2f));
            g.dispose();
        }
    }

    private static final class MiniRoute extends JComponent {
        MiniRoute() {
            Anim.register(this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(70, 22);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            g.translate(0, (getHeight() - 22) / 2.0);
            g.setColor(Theme.TEXT);
            g.fill(new Ellipse2D.Double(2, 7, 8, 8));
            float phase = (float) (14 - ((Anim.now() / 1.2) % 1.0) * 14);
            g.setColor(Theme.ACCENT);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{2f, 5f}, phase));
            g.draw(new Line2D.Double(14, 11, 52, 11));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Double(57, 6, 10, 10));
            g.fill(new Ellipse2D.Double(60.2, 9.2, 3.6, 3.6));
            g.dispose();
        }
    }

    private static final class Heading extends JComponent {
        private static final String[] WORDS = {"SHARED RIDE.", "SPLIT THE FUEL.", "FEWER CARS.", "NEW FRIENDS.", "ONE JOURNEY."};
        private final Font font = Theme.font(800, 66).deriveFont(Map.of(TextAttribute.TRACKING, -0.03, TextAttribute.KERNING, TextAttribute.KERNING_ON));

        Heading() {
            Anim.register(this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(620, (int) (66 * 1.05 * 2) + 6);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics();
            double line = 66 * 1.05;
            float base1 = fm.getAscent() - 4;
            g.setColor(Theme.TEXT);
            g.drawString("SAME ROAD.", 0, base1);
            double t = Anim.now();
            double slot = 2.4;
            int index = (int) Math.floor(t / slot) % WORDS.length;
            double local = t % slot;
            double move = Anim.easeInOut((local - (slot - 0.6)) / 0.6);
            Shape clip = new RoundRectangle2D.Double(0, line, getWidth(), line + 6, 0, 0);
            g.clip(clip);
            g.setColor(Theme.ACCENT);
            float base2 = (float) (line + base1);
            g.drawString(WORDS[index], 0, (float) (base2 - move * line));
            g.drawString(WORDS[(index + 1) % WORDS.length], 0, (float) (base2 + line - move * line));
            g.dispose();
        }
    }
}
