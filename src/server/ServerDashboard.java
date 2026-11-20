package server;

import common.Offer;
import common.Places;
import ui.IconView;
import ui.Icons;
import ui.Logo;
import ui.PillButton;
import ui.PulseDot;
import ui.RoundPanel;
import ui.Table;
import ui.TextBox;
import ui.Theme;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ServerDashboard extends JFrame {

    private final Server server;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final TextBox password = new TextBox(Icons.LOCK, "Enter password", true);
    private final JLabel error = Ui.label(" ", 600, 13, Theme.DANGER);
    private final JLabel online = Ui.label("0", 800, 34, Theme.TEXT);
    private final JLabel offers = Ui.label("0", 800, 34, Theme.TEXT);
    private final JLabel requests = Ui.label("0", 800, 34, Theme.TEXT);
    private final JLabel matches = Ui.label("0", 800, 34, Theme.TEXT);
    private final JLabel busy = Ui.label("", 600, 13, Theme.MUTED);
    private final Segments segments = new Segments();
    private final Table clients = new Table(new String[]{"User", "IP address", "Thread", "Since"}, new double[]{1.2, 1.2, 1.3, 0.8})
            .dotColumn(0).empty("No clients connected yet");
    private final Table offerTable = new Table(new String[]{"#", "Driver", "Route", "Leaves", "Seats"}, new double[]{0.35, 1, 2, 0.9, 0.8})
            .badgeColumn(4).empty("No ride offers yet");
    private final LogView logView = new LogView();
    private JScrollPane logScroll;
    private long lastSeq;

    public ServerDashboard(Server server) {
        super("RouteMatch Server Dashboard");
        this.server = server;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Theme.windowSize(1280, 800));
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);
        deck.setBackground(Theme.BG);
        deck.add(lockScreen(), "lock");
        deck.add(mainScreen(), "main");
        setContentPane(deck);
        cards.show(deck, "lock");
        new Timer(700, e -> refresh()).start();
        SwingUtilities.invokeLater(() -> password.field().requestFocusInWindow());
    }

    private JComponent lockScreen() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BG);
        RoundPanel card = new RoundPanel(new BorderLayout(), Theme.PANEL, Theme.LINE, 26);
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        PillButton unlock = new PillButton("Unlock dashboard", PillButton.Style.ACCENT).icon(Icons.ARROW_RIGHT, true).size(50, 15.5f);
        JLabel title = Ui.label("Server dashboard", 800, 28, Theme.TEXT);
        JLabel text = Ui.wrap("Admin access only. The password is read from config.properties on this computer.", 500, 14.5f, Theme.MUTED, 340);
        JPanel content = Ui.column(16,
                new Logo(40, true),
                Ui.space(1, 4),
                Ui.column(8, title, text),
                Ui.column(8, Ui.label("Admin password", 600, 13, Theme.TEXT_2), password),
                error,
                unlock);
        card.add(content);
        card.setPreferredSize(new Dimension(420, card.getPreferredSize().height));
        outer.add(card);
        Runnable attempt = () -> {
            String pw = new String(((javax.swing.JPasswordField) password.field()).getPassword());
            if (server.password() == null) {
                error.setText("config.properties with admin.password was not found");
            } else if (pw.equals(server.password())) {
                error.setText(" ");
                password.setText("");
                server.log().log("SERVER", "Dashboard unlocked by admin");
                cards.show(deck, "main");
            } else {
                error.setText("Wrong password, try again");
                server.log().log("SERVER", "Wrong dashboard password attempt");
            }
        };
        unlock.addActionListener(e -> attempt.run());
        ((javax.swing.JPasswordField) password.field()).addActionListener(e -> attempt.run());
        return outer;
    }

    private JComponent mainScreen() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 20, 20));
        root.add(header(), BorderLayout.NORTH);
        JPanel body = Ui.clear(new BorderLayout(0, 18));
        JPanel stats = Ui.clear(new GridLayout(1, 5, 14, 0));
        stats.add(stat("Online users", online, Icons.USERS, Theme.SAGE));
        stats.add(stat("Active offers", offers, Icons.ROUTE, Theme.ACCENT));
        stats.add(stat("Open requests", requests, Icons.SEARCH, Theme.SKY));
        stats.add(stat("Matches today", matches, Icons.TICKET, Theme.ACCENT_HOVER));
        RoundPanel threads = new RoundPanel(new BorderLayout(0, 12), Theme.PANEL, Theme.LINE_SOFT, 20);
        threads.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        threads.add(Ui.row(8, Ui.fill(Ui.label("Worker threads", 600, 13, Theme.MUTED)), busy), BorderLayout.NORTH);
        threads.add(segments, BorderLayout.CENTER);
        stats.add(threads);
        body.add(stats, BorderLayout.NORTH);
        JPanel tables = Ui.clear(new GridLayout(2, 1, 0, 18));
        tables.add(section("Connected clients", Ui.scroll(clients), null));
        tables.add(section("Active offers", Ui.scroll(offerTable), null));
        JPanel split = Ui.clear(new BorderLayout(18, 0));
        split.add(tables, BorderLayout.CENTER);
        logScroll = Ui.scroll(logView);
        PulseDot dot = new PulseDot(Theme.SAGE);
        JComponent streaming = Ui.row(6, dot, Ui.label("streaming \u00B7 server.log", 600, 12.5f, Theme.SAGE_TEXT));
        JComponent log = section("Live log", logScroll, streaming);
        log.setPreferredSize(new Dimension(480, 100));
        split.add(log, BorderLayout.EAST);
        body.add(split, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JComponent header() {
        RoundPanel bar = new RoundPanel(new BorderLayout(), Theme.PANEL, new Color(255, 255, 255, 18), 22);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        RoundPanel chip = new RoundPanel(new BorderLayout(), Theme.alpha(Theme.ACCENT, 0.12), Theme.alpha(Theme.ACCENT, 0.3), 999);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        chip.add(Ui.label("Server", 700, 13, Theme.ACCENT));
        RoundPanel live = new RoundPanel(new BorderLayout(8, 0), Theme.alpha(Theme.SAGE, 0.08), Theme.alpha(Theme.SAGE, 0.25), 999);
        live.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 14));
        live.add(new PulseDot(Theme.SAGE), BorderLayout.WEST);
        live.add(Ui.label("Running \u00B7 TCP port " + server.port(), 600, 13, Theme.SAGE_TEXT), BorderLayout.CENTER);
        JLabel started = Ui.label("Started " + Offer.fmt(server.startedAt()) + " \u00B7 " + server.address(), 500, 13, Theme.DIM);
        PillButton lock = new PillButton("Lock", PillButton.Style.GHOST).icon(Icons.LOCK, false).size(42, 14);
        lock.addActionListener(e -> {
            cards.show(deck, "lock");
            server.log().log("SERVER", "Dashboard locked");
            password.field().requestFocusInWindow();
        });
        JPanel left = Ui.row(16, new Logo(38, true), chip, live, started);
        bar.add(left, BorderLayout.WEST);
        bar.add(lock, BorderLayout.EAST);
        return bar;
    }

    private JComponent stat(String label, JLabel value, java.awt.Shape icon, Color tint) {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 6), Theme.PANEL, Theme.LINE_SOFT, 20);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JPanel top = Ui.row(10, Ui.fill(Ui.label(label, 600, 13, Theme.MUTED)), new IconView(icon, 16, tint, 30, Theme.alpha(tint, 0.14)));
        card.add(top, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JComponent section(String title, JComponent content, JComponent extra) {
        RoundPanel panel = new RoundPanel(new BorderLayout(0, 8), Theme.PANEL, Theme.LINE_SOFT, 22);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 6, 10, 6));
        JPanel head = Ui.row(10, Ui.fill(Ui.label(title, 800, 17, Theme.TEXT)));
        if (extra != null) {
            head.add(extra);
        }
        Ui.pad(head, 0, 12, 0, 12);
        panel.add(head, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void refresh() {
        MatchEngine engine = server.engine();
        online.setText(String.valueOf(engine.onlineCount()));
        offers.setText(String.valueOf(engine.offerCount()));
        requests.setText(String.valueOf(engine.requestCount()));
        matches.setText(String.valueOf(engine.matchCount()));
        int active = server.pool().getActiveCount();
        segments.set(active, Server.MAX_CLIENTS);
        busy.setText(active + " / " + Server.MAX_CLIENTS + " busy");
        List<String[]> clientRows = new ArrayList<>();
        for (ClientHandler h : engine.clients()) {
            clientRows.add(new String[]{h.name(), h.ip(), h.threadName(), Offer.fmt(h.since())});
        }
        clients.setRows(clientRows, null);
        List<String[]> offerRows = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        for (Offer o : engine.sortedOffers()) {
            String seats = o.isFull() ? "Full" : o.seatsLeft() + " of " + o.seats + " left";
            offerRows.add(new String[]{String.valueOf(o.id), o.driver, Places.name(o.from) + "  \u203A  " + Places.name(o.to), Offer.fmt(o.time), seats});
            colors.add(o.isFull() ? Theme.DANGER : Theme.SAGE);
        }
        offerTable.setRows(offerRows, colors);
        List<ServerLog.Entry> fresh = server.log().since(lastSeq);
        if (!fresh.isEmpty()) {
            lastSeq = fresh.get(fresh.size() - 1).seq;
            JScrollBar bar = logScroll.getVerticalScrollBar();
            boolean atBottom = bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 30;
            logView.add(fresh);
            if (atBottom) {
                SwingUtilities.invokeLater(() -> bar.setValue(bar.getMaximum()));
            }
        }
    }

    private static final class Segments extends JComponent {
        private int on;
        private int total = Server.MAX_CLIENTS;

        void set(int on, int total) {
            this.on = on;
            this.total = total;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(160, 22);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            double gap = 4;
            double w = (getWidth() - gap * (total - 1)) / total;
            double h = 22;
            double y = (getHeight() - h) / 2;
            for (int i = 0; i < total; i++) {
                g.setColor(i < on ? Theme.SAGE : new Color(255, 255, 255, 14));
                g.fill(new RoundRectangle2D.Double(i * (w + gap), y, w, h, 6, 6));
            }
            g.dispose();
        }
    }

    private static final class LogView extends JComponent {
        private static final int ROW = 24;
        private final List<ServerLog.Entry> entries = new ArrayList<>();

        void add(List<ServerLog.Entry> list) {
            entries.addAll(list);
            while (entries.size() > 400) {
                entries.remove(0);
            }
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, entries.size() * ROW + 12);
        }

        private Color tint(String tag) {
            switch (tag) {
                case "SERVER":
                case "REQUEST":
                    return Theme.SKY;
                case "CONNECT":
                case "LOGIN":
                case "MATCH":
                    return Theme.SAGE;
                case "OFFER":
                    return Theme.ACCENT;
                case "BOOK":
                    return Theme.ACCENT_HOVER;
                case "LOCK":
                    return new Color(0xCDB6DD);
                case "CANCEL":
                case "ERROR":
                    return Theme.DANGER;
                default:
                    return Theme.DIM;
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            g.setFont(Theme.font(500, 12.5f));
            FontMetrics fm = g.getFontMetrics();
            int w = getWidth();
            for (int i = 0; i < entries.size(); i++) {
                ServerLog.Entry e = entries.get(i);
                int y = 6 + i * ROW;
                float base = y + (ROW + fm.getAscent() - fm.getDescent()) / 2f;
                float x = 16;
                g.setColor(Theme.FAINT);
                g.drawString(e.time, x, base);
                x += fm.stringWidth("00:00:00") + 10;
                String tag = "[" + e.tag + "]";
                g.setFont(Theme.font(700, 12.5f));
                g.setColor(tint(e.tag));
                g.drawString(tag, x, base);
                x += 92;
                g.setFont(Theme.font(500, 12.5f));
                g.setColor(Theme.TEXT_2);
                String msg = e.message;
                double max = w - x - 14;
                if (fm.stringWidth(msg) > max) {
                    while (msg.length() > 1 && fm.stringWidth(msg + "\u2026") > max) {
                        msg = msg.substring(0, msg.length() - 1);
                    }
                    msg = msg + "\u2026";
                }
                g.drawString(msg, x, base);
            }
            g.dispose();
        }
    }
}
