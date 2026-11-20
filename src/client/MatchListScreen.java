package client;

import common.Offer;
import common.Places;
import common.Protocol;
import ui.IconView;
import ui.Icons;
import ui.PillButton;
import ui.RoundPanel;
import ui.RouteText;
import ui.Theme;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchListScreen extends Screen {

    private final JPanel main = Ui.column(14);
    private final JPanel notesList = Ui.column(4);
    private final Map<Integer, MapPanel> miniMaps = new HashMap<>();

    public MatchListScreen(ClientApp app) {
        super(app);
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(Ui.scroll(Ui.pad(main, 0, 0, 0, 10)), BorderLayout.CENTER);
        RoundPanel aside = new RoundPanel(new BorderLayout(0, 12), Theme.PANEL, Theme.LINE_SOFT, 24);
        aside.setBorder(BorderFactory.createEmptyBorder(18, 16, 12, 10));
        aside.setPreferredSize(new Dimension(370, 100));
        PillButton read = new PillButton("Mark all read", PillButton.Style.GHOST).size(34, 13);
        read.addActionListener(e -> app.markAllRead());
        JPanel head = Ui.row(10, Ui.fill(Ui.label("Notifications", 800, 19, Theme.TEXT)), read);
        Ui.pad(head, 0, 4, 0, 6);
        aside.add(head, BorderLayout.NORTH);
        aside.add(Ui.scroll(notesList), BorderLayout.CENTER);
        add(aside, BorderLayout.EAST);
    }

    @Override
    public void refresh() {
        String me = app.user();
        main.removeAll();
        main.add(Ui.label("My rides", 800, 30, Theme.TEXT));
        main.add(section("You're riding"));
        boolean riding = false;
        for (Offer o : app.offers()) {
            if (o.hasPassenger(me)) {
                main.add(bookingCard(o, me));
                riding = true;
            }
        }
        if (!riding) {
            main.add(empty("No bookings yet", "Find a ride going your way and book a seat.", "Find a ride", ClientApp.FIND));
        }
        main.add(Ui.space(1, 6));
        main.add(section("You're driving"));
        boolean driving = false;
        for (Offer o : app.offers()) {
            if (o.driver.equals(me)) {
                main.add(drivingCard(o));
                driving = true;
            }
        }
        if (!driving) {
            main.add(empty("You haven't offered a ride", "Making a trip anyway? Share your empty seats.", "Offer a ride", ClientApp.OFFER));
        }
        main.revalidate();
        main.repaint();
        notesList.removeAll();
        List<ClientApp.Note> notes = app.notes();
        if (notes.isEmpty()) {
            notesList.add(Ui.pad(Ui.wrap("Bookings, cancellations and new matching rides appear here live.", 500, 13.5f, Theme.FAINT, 300), 8, 6, 0, 0));
        }
        for (ClientApp.Note n : notes) {
            notesList.add(noteRow(n));
        }
        notesList.revalidate();
        notesList.repaint();
    }

    private JComponent section(String text) {
        return Ui.pad(Ui.label(text, 700, 17, Theme.TEXT_2), 6, 2, 0, 0);
    }

    private JComponent bookingCard(Offer o, String me) {
        RoundPanel card = new RoundPanel(new BorderLayout(20, 0), Theme.PANEL, Theme.LINE_SOFT, 24);
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 20));
        MapPanel mini = miniMaps.computeIfAbsent(o.id, k -> new MapPanel(false, false).corner(Theme.PANEL, 16));
        mini.fitPadding(28, 28, 28, 28);
        mini.setRoute(Places.points(o.from, o.to), "", "");
        mini.setPreferredSize(new Dimension(240, 170));
        LocalTime at = o.bookedAt(me);
        RoundPanel chip = new RoundPanel(new BorderLayout(), Theme.alpha(Theme.SAGE, 0.12), Theme.alpha(Theme.SAGE, 0.3), 999);
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        chip.add(Ui.label("Confirmed", 700, 12, Theme.SAGE_TEXT));
        JPanel status = Ui.row(10, chip, Ui.label("Today \u00B7 Booked " + (at == null ? "" : Offer.fmt(at)), 500, 12.5f, Theme.DIM));
        JPanel facts = Ui.clear(new GridLayout(1, 3, 16, 0));
        facts.add(fact("Leaves", Offer.fmt(o.time)));
        facts.add(fact("Driver", o.driver));
        facts.add(fact("Vehicle", o.vehicle.label));
        String noteText = o.note.isEmpty() ? Places.viaLabel(o.from, o.to) : "Pickup: " + o.note;
        JPanel middle = Ui.column(10, status, new RouteText(800, 21, Theme.TEXT).set(Places.name(o.from), Places.name(o.to)),
                Ui.label(noteText, 500, 13, Theme.MUTED), facts);
        PillButton cancel = new PillButton("Cancel booking", PillButton.Style.DANGER).size(38, 13);
        cancel.addActionListener(e -> app.send(Protocol.CANCEL_BOOKING, o.id));
        JPanel seat = Ui.column(2,
                Ui.label("SEAT", 700, 11, Theme.DIM),
                Ui.label(String.valueOf(o.seatNumber(me)), 800, 34, Theme.ACCENT),
                Ui.label("of " + o.seats + " in this " + o.vehicle.label.toLowerCase(), 500, 12.5f, Theme.MUTED),
                Ui.space(1, 8),
                cancel);
        card.add(mini, BorderLayout.WEST);
        card.add(Ui.pad(middle, 4, 0, 0, 0), BorderLayout.CENTER);
        card.add(Ui.pad(seat, 4, 0, 0, 0), BorderLayout.EAST);
        return card;
    }

    private JComponent drivingCard(Offer o) {
        RoundPanel card = new RoundPanel(new BorderLayout(0, 14), Theme.PANEL, Theme.LINE_SOFT, 24);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        IconView icon = new IconView(Icons.vehicle(o.vehicle), 26, Theme.ACCENT, 48, Theme.alpha(Theme.ACCENT, 0.12));
        String detail = "Today \u00B7 " + Offer.fmt(o.time) + " \u00B7 " + o.vehicle.label + " \u00B7 " + Places.viaLabel(o.from, o.to);
        JPanel title = Ui.column(4, new RouteText(800, 20, Theme.TEXT).set(Places.name(o.from), Places.name(o.to)), Ui.label(detail, 500, 13, Theme.DIM));
        PillButton cancel = new PillButton("Cancel offer", PillButton.Style.DANGER).size(38, 13);
        cancel.addActionListener(e -> app.send(Protocol.CANCEL_OFFER, o.id));
        JPanel top = Ui.row(14, icon, Ui.fill(title), cancel);
        int booked = o.seats - o.seatsLeft();
        JPanel bar = Ui.row(12, Ui.label("Seats booked", 600, 13, Theme.MUTED), Ui.label(booked + " of " + o.seats, 800, 14, Theme.TEXT), Ui.fill(new Segments(booked, o.seats)));
        JPanel people = Ui.clear(new GridLayout(0, 2, 10, 10));
        for (String p : o.passengerNames()) {
            LocalTime at = o.bookedAt(p);
            people.add(person(p, "Booked " + (at == null ? "" : Offer.fmt(at))));
        }
        if (o.seatsLeft() > 0) {
            people.add(openSeat(o.seatsLeft()));
        }
        card.add(top, BorderLayout.NORTH);
        card.add(bar, BorderLayout.CENTER);
        card.add(people, BorderLayout.SOUTH);
        return card;
    }

    private JComponent person(String name, String sub) {
        RoundPanel p = new RoundPanel(new BorderLayout(10, 0), new Color(255, 255, 255, 8), new Color(255, 255, 255, 14), 16);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        p.add(new Avatar(name, 34), BorderLayout.WEST);
        p.add(Ui.column(1, Ui.label(name, 700, 14, Theme.TEXT), Ui.label(sub, 500, 12, Theme.DIM)), BorderLayout.CENTER);
        return p;
    }

    private JComponent openSeat(int left) {
        RoundPanel p = new RoundPanel(new BorderLayout(10, 0), null, Theme.alpha(Theme.ACCENT, 0.3), 16);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 12));
        p.add(new IconView(Icons.PLUS, 16, Theme.ACCENT, 34, Theme.alpha(Theme.ACCENT, 0.12)).tile(Theme.alpha(Theme.ACCENT, 0.12), 17), BorderLayout.WEST);
        p.add(Ui.label(left == 1 ? "1 seat open" : left + " seats open", 600, 13.5f, Theme.ACCENT), BorderLayout.CENTER);
        return p;
    }

    private JComponent fact(String label, String value) {
        return Ui.column(3, Ui.label(label, 600, 12, Theme.DIM), Ui.label(value, 700, 14.5f, Theme.TEXT));
    }

    private JComponent empty(String head, String body, String action, String page) {
        RoundPanel p = new RoundPanel(new BorderLayout(16, 0), Theme.PANEL, Theme.LINE_SOFT, 22);
        p.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 18));
        p.add(Ui.column(4, Ui.label(head, 700, 15.5f, Theme.TEXT), Ui.label(body, 500, 13.5f, Theme.MUTED)), BorderLayout.CENTER);
        PillButton b = new PillButton(action, PillButton.Style.SOFT).icon(Icons.ARROW_RIGHT, true).size(40, 13.5f);
        b.addActionListener(e -> app.go(page));
        p.add(Ui.column(0, b), BorderLayout.EAST);
        return p;
    }

    private JComponent noteRow(ClientApp.Note n) {
        JPanel row = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g0) {
                if (!n.unread) {
                    return;
                }
                Graphics2D g = (Graphics2D) g0.create();
                Theme.hints(g);
                g.setColor(new Color(255, 255, 255, 8));
                g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 28, 28));
                g.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Color tint = ClientApp.noteColor(n.kind);
        row.add(new IconView(ClientApp.noteIcon(n.kind), 16, tint, 34, Theme.alpha(tint, 0.14)), BorderLayout.WEST);
        row.add(Ui.column(4, Ui.wrap(n.text, 500, 13.5f, Theme.TEXT, 230), Ui.label(Offer.fmt(n.time), 500, 12, Theme.DIM)), BorderLayout.CENTER);
        if (n.unread) {
            JComponent dot = new JComponent() {
                @Override
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    Theme.hints(g);
                    g.setColor(Theme.ACCENT);
                    g.fill(new Ellipse2D.Double(2, 6, 8, 8));
                    g.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(12, 20));
            row.add(Ui.column(0, dot), BorderLayout.EAST);
        }
        return row;
    }

    private static final class Segments extends JComponent {
        private final int on;
        private final int total;

        Segments(int on, int total) {
            this.on = on;
            this.total = total;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 10);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            double gap = 6;
            double w = (getWidth() - gap * (total - 1)) / total;
            for (int i = 0; i < total; i++) {
                g.setColor(i < on ? Theme.ACCENT : new Color(255, 255, 255, 18));
                g.fill(new RoundRectangle2D.Double(i * (w + gap), 0, w, getHeight(), getHeight(), getHeight()));
            }
            g.dispose();
        }
    }
}
