package client;

import common.Offer;
import common.Places;
import common.Vehicle;
import ui.Chips;
import ui.Icons;
import ui.PillButton;
import ui.RoundPanel;
import ui.RouteText;
import ui.Theme;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class HomeScreen extends Screen {

    private static final String[] FILTERS = {"All", "Car", "Tuk-tuk", "Bike", "Van"};

    private final MapPanel map = new MapPanel(true, true);
    private final Chips filters = new Chips(FILTERS, null, false, 0);
    private final JPanel list = Ui.column(12);
    private final JLabel statOffers = Ui.label("0", 800, 14, Theme.TEXT);
    private final JLabel statRequests = Ui.label("0", 800, 14, Theme.TEXT);
    private final JLabel statMatches = Ui.label("0", 800, 14, Theme.TEXT);
    private final RoundPanel card = new RoundPanel(new BorderLayout(24, 0), new Color(22, 20, 18, 240), new Color(255, 255, 255, 22), 22);
    private final RouteText cardRoute = new RouteText(800, 22, Theme.TEXT);
    private final JLabel cardVia = Ui.label(" ", 500, 13, Theme.DIM);
    private final JLabel cardDriver = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel cardVehicle = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel cardTime = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel cardSeats = Ui.label(" ", 700, 15, Theme.TEXT);
    private final PillButton cta = new PillButton("Book a seat", PillButton.Style.ACCENT);
    private int selectedId = -1;

    public HomeScreen(ClientApp app) {
        super(app);
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(aside(), BorderLayout.WEST);
        add(mapSection(), BorderLayout.CENTER);
        filters.onChange(i -> refresh());
    }

    private JComponent aside() {
        JPanel aside = Ui.clear(new BorderLayout(0, 16));
        aside.setPreferredSize(new Dimension(390, 100));
        JPanel head = Ui.column(14,
                Ui.column(6, Ui.label("Leaving soon", 800, 30, Theme.TEXT),
                        Ui.label("Live offers from people already making the trip.", 500, 14.5f, Theme.MUTED)),
                filters);
        aside.add(head, BorderLayout.NORTH);
        aside.add(Ui.scroll(list), BorderLayout.CENTER);
        return aside;
    }

    private JComponent mapSection() {
        map.fitPadding(80, 70, 220, 90);
        RoundPanel stats = new RoundPanel(new GridLayout(1, 3, 6, 0), new Color(22, 20, 18, 230), new Color(255, 255, 255, 20), 999);
        stats.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        stats.add(stat(statOffers, "active offers"));
        stats.add(stat(statRequests, "open requests"));
        stats.add(stat(statMatches, "matched today"));
        map.overlay(stats, "nw");
        RoundPanel hint = new RoundPanel(new BorderLayout(), new Color(22, 20, 18, 200), null, 999);
        hint.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        hint.add(Ui.label("Drag to move \u00B7 scroll to zoom", 500, 12.5f, Theme.MUTED));
        map.overlay(hint, "ne");
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 18));
        JLabel caption = Ui.label("SELECTED RIDE", 700, 11.5f, Theme.ACCENT);
        JPanel left = Ui.column(4, caption, cardRoute, cardVia);
        JPanel facts = Ui.clear(new GridLayout(1, 4, 18, 0));
        facts.add(fact("Driver", cardDriver));
        facts.add(fact("Vehicle", cardVehicle));
        facts.add(fact("Leaves", cardTime));
        facts.add(fact("Seats", cardSeats));
        cta.icon(Icons.ARROW_UP_RIGHT, true).size(48, 15);
        cta.addActionListener(e -> {
            Offer o = app.offer(selectedId);
            if (o == null) {
                return;
            }
            if (o.driver.equals(app.user()) || o.hasPassenger(app.user())) {
                app.go(ClientApp.RIDES);
            } else {
                app.bookFromHome(o);
            }
        });
        card.add(left, BorderLayout.WEST);
        card.add(facts, BorderLayout.CENTER);
        card.add(Ui.column(0, Ui.space(1, 8), cta), BorderLayout.EAST);
        map.overlay(card, "s");
        return map;
    }

    private JComponent stat(JLabel value, String text) {
        JPanel p = Ui.row(6, value, Ui.label(text, 500, 13, Theme.MUTED));
        Ui.pad(p, 4, 10, 4, 10);
        return p;
    }

    private JComponent fact(String label, JLabel value) {
        return Ui.column(4, Ui.label(label, 600, 12, Theme.DIM), value);
    }

    private List<Offer> visible() {
        List<Offer> out = new ArrayList<>();
        int f = filters.selected();
        for (Offer o : app.offers()) {
            if (f == 0 || o.vehicle == Vehicle.values()[f - 1]) {
                out.add(o);
            }
        }
        return out;
    }

    @Override
    public void refresh() {
        statOffers.setText(String.valueOf(app.offers().size()));
        statRequests.setText(String.valueOf(app.openRequests()));
        statMatches.setText(String.valueOf(app.matched()));
        List<Offer> shown = visible();
        if (app.offer(selectedId) == null || shown.stream().noneMatch(o -> o.id == selectedId)) {
            selectedId = shown.isEmpty() ? -1 : shown.get(0).id;
        }
        list.removeAll();
        if (shown.isEmpty()) {
            list.add(empty());
        }
        for (Offer o : shown) {
            list.add(new OfferCard(o, app.user(), o.id == selectedId, () -> {
                selectedId = o.id;
                refresh();
            }));
        }
        list.revalidate();
        list.repaint();
        List<List<double[]>> others = new ArrayList<>();
        for (Offer o : shown) {
            if (o.id != selectedId) {
                others.add(Places.points(o.from, o.to));
            }
        }
        map.setOthers(others);
        Offer sel = app.offer(selectedId);
        if (sel == null) {
            map.clearRoute();
            card.setVisible(false);
        } else {
            map.setRoute(Places.points(sel.from, sel.to), Places.name(sel.from), Places.name(sel.to));
            cardRoute.set(Places.name(sel.from), Places.name(sel.to));
            cardVia.setText(Places.viaLabel(sel.from, sel.to));
            cardDriver.setText(sel.driver.equals(app.user()) ? "You" : sel.driver);
            cardVehicle.setText(sel.vehicle.label);
            cardTime.setText(Offer.fmt(sel.time));
            cardSeats.setText(sel.isFull() ? "Full" : sel.seatsLeft() + " of " + sel.seats);
            boolean mine = sel.driver.equals(app.user()) || sel.hasPassenger(app.user());
            cta.setText(mine ? "View in My rides" : sel.isFull() ? "Ride is full" : "Book a seat");
            cta.setEnabled(mine || !sel.isFull());
            card.setVisible(true);
        }
        map.revalidate();
        map.repaint();
    }

    private JComponent empty() {
        RoundPanel p = new RoundPanel(new BorderLayout(0, 14), Theme.PANEL, Theme.LINE_SOFT, 22);
        p.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        JPanel text = Ui.column(6,
                Ui.label("No rides yet", 700, 17, Theme.TEXT),
                Ui.wrap("When someone offers a ride it shows up here instantly. Be the first one.", 500, 13.5f, Theme.MUTED, 300));
        PillButton go = new PillButton("Offer a ride", PillButton.Style.SOFT).icon(Icons.PLUS, false).size(40, 14);
        go.addActionListener(e -> app.go(ClientApp.OFFER));
        p.add(text, BorderLayout.CENTER);
        p.add(Ui.row(0, go), BorderLayout.SOUTH);
        return p;
    }
}
