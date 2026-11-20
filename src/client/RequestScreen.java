package client;

import common.Offer;
import common.Places;
import common.Protocol;
import common.Request;
import ui.Chips;
import ui.Icons;
import ui.PillButton;
import ui.PlaceSelect;
import ui.PulseDot;
import ui.RoundPanel;
import ui.RouteText;
import ui.Theme;
import ui.TimeBox;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RequestScreen extends Screen {

    private static final int[] WINDOWS = {15, 30, 60};

    private final PlaceSelect from = new PlaceSelect("From", Theme.TEXT, "col");
    private final PlaceSelect to = new PlaceSelect("To", Theme.PIN, "kan");
    private final TimeBox time = new TimeBox("I want to leave at", TimeBox.nextQuarter());
    private final Chips window = new Chips(new String[]{"\u00B115 min", "\u00B130 min", "\u00B160 min"}, null, false, 1);
    private final PillButton search = new PillButton("Find rides", PillButton.Style.ACCENT);
    private final JLabel same = Ui.label("Start and destination must be different places.", 600, 13, Theme.DANGER);
    private final JLabel title = Ui.label("Search for a ride", 800, 19, Theme.TEXT);
    private final JLabel subtitle = Ui.label("Pick your route and time, then press Find rides", 500, 13, Theme.DIM);
    private final JPanel results = Ui.column(10);
    private final MapPanel map = new MapPanel(true, true);
    private final JLabel requestChip = Ui.label(" ", 600, 13, Theme.TEXT);
    private final JLabel countChip = Ui.label(" ", 600, 13, Theme.TEXT);
    private final RoundPanel card = new RoundPanel(new BorderLayout(20, 0), new Color(22, 20, 18, 240), new Color(255, 255, 255, 22), 22);
    private final Avatar avatar = new Avatar("", 46);
    private final JLabel cardTitle = Ui.label(" ", 800, 18, Theme.TEXT);
    private final RouteText cardRoute = new RouteText(600, 14, Theme.TEXT_2);
    private final JLabel cardVia = Ui.label(" ", 500, 12.5f, Theme.DIM);
    private final JLabel cardTime = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel cardVehicle = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel cardSeats = Ui.label(" ", 700, 15, Theme.TEXT);
    private final PillButton cardBook = new PillButton("Book this seat", PillButton.Style.ACCENT);
    private List<Offer> matches = new ArrayList<>();
    private Request request;
    private int selectedId = -1;
    private int pendingSelect = -1;
    private boolean searching;

    public RequestScreen(ClientApp app) {
        super(app);
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel west = Ui.clear(new BorderLayout(0, 14));
        west.setPreferredSize(new Dimension(440, 100));
        west.add(form(), BorderLayout.NORTH);
        west.add(Ui.scroll(Ui.pad(results, 0, 0, 0, 8)), BorderLayout.CENTER);
        add(west, BorderLayout.WEST);
        add(mapSection(), BorderLayout.CENTER);
        from.onChange(c -> preview());
        to.onChange(c -> preview());
        search.addActionListener(e -> run());
        cardBook.addActionListener(e -> book(selectedId));
        preview();
        renderResults();
    }

    private JComponent form() {
        JPanel places = Ui.clear(new GridLayout(1, 2, 10, 0));
        places.add(from);
        places.add(to);
        time.setPreferredSize(new Dimension(300, 64));
        search.icon(Icons.SEARCH, false).size(50, 15.5f).radius(16);
        JPanel head = Ui.column(4, title, subtitle);
        Ui.pad(head, 8, 0, 0, 0);
        return Ui.column(12,
                Ui.label("Find a ride", 800, 30, Theme.TEXT),
                places,
                same,
                time,
                Ui.row(12, Ui.label("Flexible by", 600, 13, Theme.TEXT_2), window),
                search,
                head);
    }

    private JComponent mapSection() {
        map.fitPadding(80, 70, 210, 90);
        RoundPanel chips = new RoundPanel(new BorderLayout(10, 0), new Color(22, 20, 18, 230), new Color(255, 255, 255, 20), 999);
        chips.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 14));
        chips.add(Ui.row(8, new PulseDot(Theme.ACCENT), requestChip), BorderLayout.WEST);
        chips.add(countChip, BorderLayout.EAST);
        map.overlay(chips, "nw");
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 18));
        JPanel who = Ui.row(14, avatar, Ui.column(3, cardTitle, cardRoute, cardVia));
        JPanel facts = Ui.clear(new GridLayout(1, 3, 18, 0));
        facts.add(Ui.column(4, Ui.label("Leaves", 600, 12, Theme.DIM), cardTime));
        facts.add(Ui.column(4, Ui.label("Vehicle", 600, 12, Theme.DIM), cardVehicle));
        facts.add(Ui.column(4, Ui.label("Seats", 600, 12, Theme.DIM), cardSeats));
        cardBook.icon(Icons.ARROW_RIGHT, true).size(48, 15);
        card.add(who, BorderLayout.WEST);
        card.add(facts, BorderLayout.CENTER);
        card.add(Ui.column(0, Ui.space(1, 6), cardBook), BorderLayout.EAST);
        card.setVisible(false);
        map.overlay(card, "s");
        return map;
    }

    private int windowMinutes() {
        return WINDOWS[window.selected()];
    }

    private void preview() {
        boolean bad = from.code().equals(to.code());
        same.setVisible(bad);
        search.setEnabled(!bad);
        if (selectedOffer() == null) {
            map.setRoute(Places.points(from.code(), to.code()), Places.name(from.code()), Places.name(to.code()));
        }
        revalidate();
    }

    private void run() {
        if (from.code().equals(to.code())) {
            return;
        }
        request = new Request(app.user(), from.code(), to.code(), time.time(), windowMinutes());
        searching = true;
        title.setText("Searching\u2026");
        send();
    }

    private void send() {
        if (request == null) {
            return;
        }
        app.send(Protocol.FIND, request.from, request.to, Offer.hm(request.time), request.window);
    }

    public void research() {
        if (request != null) {
            send();
        }
    }

    public void prefill(Offer o) {
        from.setCode(o.from);
        to.setCode(o.to);
        time.set(o.time, false);
        window.select(1);
        pendingSelect = o.id;
        run();
    }

    public void reset() {
        request = null;
        matches = new ArrayList<>();
        selectedId = -1;
        searching = false;
        title.setText("Search for a ride");
        subtitle.setText("Pick your route and time, then press Find rides");
        renderResults();
    }

    public void showMatches(List<Offer> list) {
        searching = false;
        matches = list;
        if (pendingSelect >= 0) {
            selectedId = pendingSelect;
            pendingSelect = -1;
        }
        if (selectedOffer() == null) {
            selectedId = matches.isEmpty() ? -1 : matches.get(0).id;
        }
        renderResults();
    }

    private Offer selectedOffer() {
        for (Offer o : matches) {
            if (o.id == selectedId) {
                return o;
            }
        }
        return null;
    }

    private void book(int id) {
        Offer o = selectedOffer();
        if (o != null && o.id != id) {
            for (Offer m : matches) {
                if (m.id == id) {
                    o = m;
                }
            }
        }
        if (o == null) {
            return;
        }
        if (o.hasPassenger(app.user())) {
            app.send(Protocol.CANCEL_BOOKING, o.id);
        } else {
            app.send(Protocol.BOOK, o.id);
        }
    }

    private void renderResults() {
        results.removeAll();
        if (request == null) {
            requestChip.setText("Your request \u00B7 not sent yet");
            countChip.setText("");
            results.add(hint("Search to see rides", "RouteMatch shows every ride inside your time window, closest departure first."));
        } else {
            String routeName = Places.name(request.from) + " to " + Places.name(request.to);
            title.setText(matches.isEmpty() ? "No rides yet" : matches.size() + (matches.size() == 1 ? " ride " : " rides ") + "for " + routeName);
            subtitle.setText("Closest time first \u00B7 tap a ride to preview it");
            requestChip.setText("Your request \u00B7 \u00B1" + request.window + " min");
            countChip.setText(matches.size() + " matching");
            if (matches.isEmpty()) {
                results.add(hint("No rides in this time window", "Try a wider window like \u00B160 min. Your request stays open and you get a notification the moment a matching offer is posted."));
            }
            for (Offer o : matches) {
                results.add(new Row(o));
            }
        }
        Offer sel = selectedOffer();
        if (sel == null) {
            card.setVisible(false);
            map.setRoute(Places.points(from.code(), to.code()), Places.name(from.code()), Places.name(to.code()));
        } else {
            map.setRoute(Places.points(sel.from, sel.to), "Pickup \u00B7 " + sel.driver + " " + Offer.fmt(sel.time), Places.name(sel.to));
            avatar.setName(sel.driver);
            cardTitle.setText("Ride with " + sel.driver);
            cardRoute.set(Places.name(sel.from), Places.name(sel.to));
            cardVia.setText(Places.viaLabel(sel.from, sel.to));
            cardTime.setText(Offer.fmt(sel.time));
            cardVehicle.setText(sel.vehicle.label);
            cardSeats.setText(sel.isFull() ? "Full" : sel.seatsLeft() + " of " + sel.seats);
            boolean mine = sel.hasPassenger(app.user());
            cardBook.setText(mine ? "Cancel my seat" : sel.isFull() ? "No seats left" : "Book this seat");
            cardBook.style(mine ? PillButton.Style.DANGER : PillButton.Style.ACCENT);
            cardBook.setEnabled(mine || !sel.isFull());
            card.setVisible(true);
        }
        results.revalidate();
        results.repaint();
        map.revalidate();
        map.repaint();
    }

    private JComponent hint(String head, String body) {
        RoundPanel p = new RoundPanel(new BorderLayout(), Theme.PANEL, Theme.LINE_SOFT, 20);
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        p.add(Ui.column(6, Ui.label(head, 700, 15.5f, Theme.TEXT), Ui.wrap(body, 500, 13.5f, Theme.MUTED, 340)));
        return p;
    }

    @Override
    public void refresh() {
    }

    private final class Row extends JPanel {
        private final Offer offer;
        private boolean hover;

        Row(Offer offer) {
            super(new BorderLayout(12, 0));
            this.offer = offer;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            ui.IconView icon = new ui.IconView(Icons.vehicle(offer.vehicle), 24, offer.id == selectedId ? Theme.ACCENT : Theme.TEXT_2, 44, new Color(255, 255, 255, 12));
            long diff = request.diffMinutes(offer);
            String diffText = diff == 0 ? "On time" : (diff > 0 ? "+" : "\u2212") + Math.abs(diff) + " min";
            JLabel timeLabel = Ui.label(Offer.fmt(offer.time), 800, 17, Theme.TEXT);
            Badge badge = new Badge(diffText, diff == 0 ? Theme.SAGE : Theme.MUTED);
            JLabel who = Ui.label(offer.driver + " \u00B7 " + offer.vehicle.label + " \u00B7 " + offer.seatLabel(), 500, 13, Theme.MUTED);
            JPanel middle = Ui.column(4, Ui.row(8, timeLabel, badge), who);
            PillButton action;
            if (offer.hasPassenger(app.user())) {
                action = new PillButton("Booked \u00B7 Cancel", PillButton.Style.GHOST);
            } else if (offer.isFull()) {
                action = new PillButton("Full", PillButton.Style.GHOST);
                action.setEnabled(false);
            } else {
                action = new PillButton("Book", PillButton.Style.ACCENT);
            }
            action.size(38, 13.5f);
            action.addActionListener(e -> book(offer.id));
            add(icon, BorderLayout.WEST);
            add(middle, BorderLayout.CENTER);
            add(Ui.column(0, Ui.space(1, 3), action), BorderLayout.EAST);
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    selectedId = offer.id;
                    renderResults();
                }
            };
            addMouseListener(m);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            boolean on = offer.id == selectedId;
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 36, 36);
            g.setColor(on ? new Color(0x1E1A16) : hover ? Theme.PANEL_2 : Theme.PANEL);
            g.fill(shape);
            g.setStroke(new BasicStroke(on ? 1.5f : 1f));
            g.setColor(on ? Theme.ACCENT : Theme.LINE_SOFT);
            g.draw(shape);
            g.dispose();
        }
    }

    private static final class Badge extends JComponent {
        private final String text;
        private final Color tint;

        Badge(String text, Color tint) {
            this.text = text;
            this.tint = tint;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(700, 12));
            return new Dimension(fm.stringWidth(text) + 16, 22);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            g.setColor(Theme.alpha(tint, 0.14));
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g.setFont(Theme.font(700, 12));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(tint);
            g.drawString(text, 8, (getHeight() + fm.getAscent() - fm.getDescent()) / 2f);
            g.dispose();
        }
    }
}
