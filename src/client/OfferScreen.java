package client;

import common.Offer;
import common.Places;
import common.Protocol;
import common.Vehicle;
import ui.Chips;
import ui.IconView;
import ui.Icons;
import ui.PillButton;
import ui.PlaceSelect;
import ui.PulseDot;
import ui.RoundPanel;
import ui.Stepper;
import ui.TextBox;
import ui.Theme;
import ui.TimeBox;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.List;

public class OfferScreen extends Screen {

    private final PlaceSelect from = new PlaceSelect("From", Theme.TEXT, "col");
    private final PlaceSelect to = new PlaceSelect("To", Theme.PIN, "kan");
    private final JLabel same = Ui.label("Start and destination must be different places.", 600, 13, Theme.DANGER);
    private final TimeBox time = new TimeBox("Departure time", TimeBox.nextQuarter());
    private final Chips vehicles;
    private final Stepper seats = new Stepper(1, Vehicle.CAR.maxSeats, 3);
    private final JLabel seatHint = Ui.label(" ", 500, 12.5f, Theme.DIM);
    private final TextBox note = new TextBox(null, "I'll wait near the main gate", false);
    private final PillButton post = new PillButton("Post offer", PillButton.Style.ACCENT);
    private final RoundPanel posted = new RoundPanel(new BorderLayout(12, 0), Theme.alpha(Theme.SAGE, 0.08), Theme.alpha(Theme.SAGE, 0.3), 18);
    private final JLabel postedText = Ui.label(" ", 500, 13, Theme.SAGE_TEXT);
    private final MapPanel map = new MapPanel(true, true);
    private final JLabel stopsTitle = Ui.label(" ", 700, 12, Theme.ACCENT);
    private final JLabel stops = Ui.label(" ", 600, 14, Theme.TEXT);
    private final JLabel previewName = Ui.label(" ", 700, 15, Theme.TEXT);
    private final JLabel previewTime = Ui.label(" ", 800, 15, Theme.TEXT);
    private final JLabel previewSeats = Ui.label(" ", 600, 13, Theme.SAGE);
    private boolean waiting;

    public OfferScreen(ClientApp app) {
        super(app);
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        String[] names = new String[Vehicle.values().length];
        Shape[] icons = new Shape[names.length];
        for (Vehicle v : Vehicle.values()) {
            names[v.ordinal()] = v.label;
            icons[v.ordinal()] = Icons.vehicle(v);
        }
        vehicles = new Chips(names, icons, true, 0);
        JPanel form = form();
        form.setPreferredSize(new Dimension(430, form.getPreferredSize().height));
        JPanel west = Ui.clear(new BorderLayout());
        west.setPreferredSize(new Dimension(440, 100));
        west.add(Ui.scroll(Ui.pad(form, 0, 0, 0, 10)), BorderLayout.CENTER);
        add(west, BorderLayout.WEST);
        add(mapSection(), BorderLayout.CENTER);
        from.onChange(c -> update());
        to.onChange(c -> update());
        time.onChange(t -> update());
        vehicles.onChange(i -> {
            Vehicle v = Vehicle.values()[i];
            seats.range(1, v.maxSeats);
            update();
        });
        seats.onChange(n -> update());
        post.addActionListener(e -> submit());
        update();
    }

    private JPanel form() {
        JPanel head = Ui.column(6, Ui.label("Offer a ride", 800, 30, Theme.TEXT),
                Ui.wrap("Already making the trip? Share your empty seats with people going the same way.", 500, 14.5f, Theme.MUTED, 400));
        PillButton swap = new PillButton("", PillButton.Style.GHOST).icon(Icons.SWAP, false).size(40, 14);
        swap.setToolTipText("Swap start and destination");
        swap.addActionListener(e -> {
            String a = from.code();
            from.setCode(to.code());
            to.setCode(a);
            update();
        });
        JPanel places = Ui.row(8, Ui.fill(from), swap, Ui.fill(to));
        time.setPreferredSize(new Dimension(300, 64));
        RoundPanel seatBox = new RoundPanel(new BorderLayout(), Theme.alpha(Theme.FIELD, 0.85), new Color(255, 255, 255, 22), 16);
        seatBox.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 10));
        seatBox.add(Ui.column(3, Ui.label("Seats to share", 600, 15, Theme.TEXT), seatHint), BorderLayout.CENTER);
        seatBox.add(seats, BorderLayout.EAST);
        note.height(48);
        post.icon(Icons.ARROW_RIGHT, true).size(52, 16).radius(16);
        posted.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        posted.add(new IconView(Icons.CHECK, 18, Theme.SAGE, 36, Theme.alpha(Theme.SAGE, 0.15)), BorderLayout.WEST);
        PillButton view = new PillButton("View in My rides", PillButton.Style.SAGE).size(34, 13);
        view.addActionListener(e -> app.go(ClientApp.RIDES));
        posted.add(Ui.column(4, Ui.label("Your offer is live", 700, 15, Theme.TEXT), postedText, Ui.row(0, view)), BorderLayout.CENTER);
        posted.setVisible(false);
        return Ui.column(14,
                head,
                Ui.space(1, 2),
                places,
                same,
                time,
                Ui.column(8, Ui.label("Vehicle", 600, 13, Theme.TEXT_2), vehicles),
                seatBox,
                Ui.column(8, Ui.label("Pickup note (optional)", 600, 13, Theme.TEXT_2), note),
                Ui.space(1, 2),
                post,
                posted);
    }

    private JComponent mapSection() {
        map.fitPadding(80, 70, 200, 90);
        RoundPanel chip = new RoundPanel(new BorderLayout(8, 0), new Color(22, 20, 18, 230), new Color(255, 255, 255, 20), 999);
        chip.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 14));
        chip.add(new PulseDot(Theme.ACCENT), BorderLayout.WEST);
        chip.add(Ui.label("Live route preview", 600, 13, Theme.TEXT), BorderLayout.CENTER);
        map.overlay(chip, "nw");
        RoundPanel card = new RoundPanel(new BorderLayout(24, 0), new Color(22, 20, 18, 240), new Color(255, 255, 255, 22), 22);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 18));
        card.add(Ui.column(6, stopsTitle, stops), BorderLayout.CENTER);
        RoundPanel preview = new RoundPanel(new BorderLayout(0, 6), Theme.alpha(Theme.BG, 0.6), new Color(255, 255, 255, 18), 16);
        preview.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        preview.add(Ui.label("RIDERS WILL SEE", 700, 11, Theme.DIM), BorderLayout.NORTH);
        preview.add(Ui.row(16, Ui.fill(previewName), previewTime), BorderLayout.CENTER);
        preview.add(previewSeats, BorderLayout.SOUTH);
        preview.setPreferredSize(new Dimension(280, preview.getPreferredSize().height));
        card.add(preview, BorderLayout.EAST);
        map.overlay(card, "s");
        return map;
    }

    private Vehicle vehicle() {
        return Vehicle.values()[vehicles.selected()];
    }

    private void update() {
        boolean bad = from.code().equals(to.code());
        same.setVisible(bad);
        post.setEnabled(!bad && !waiting);
        Vehicle v = vehicle();
        seatHint.setText("Up to " + v.maxSeats + " for a " + v.label.toLowerCase());
        map.setRoute(Places.points(from.code(), to.code()), Places.name(from.code()), Places.name(to.code()));
        List<String> ids = Places.route(from.code(), to.code());
        stopsTitle.setText("YOUR ROUTE \u00B7 " + ids.size() + (ids.size() == 1 ? " STOP" : " STOPS"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append("  \u203A  ");
            }
            sb.append(Places.name(ids.get(i)));
        }
        stops.setText(Ui.html(sb.toString(), 360));
        String name = app.user().isEmpty() ? "You" : app.user();
        previewName.setText(name + " \u00B7 " + v.label);
        previewTime.setText(Offer.fmt(time.time()));
        previewSeats.setText(seats.value() == 1 ? "1 seat available" : seats.value() + " seats available");
        revalidate();
        repaint();
    }

    private void submit() {
        if (from.code().equals(to.code())) {
            return;
        }
        waiting = true;
        post.setEnabled(false);
        post.setText("Posting\u2026");
        app.send(Protocol.OFFER, from.code(), to.code(), Offer.hm(time.time()), vehicle().name(), seats.value(), Protocol.clean(note.text(), 80));
    }

    public void posted(int id) {
        waiting = false;
        post.setText("Post offer");
        postedText.setText(Ui.html("Riders going " + Places.name(from.code()) + " to " + Places.name(to.code()) + " can see it now (offer #" + id + ").", 300));
        posted.setVisible(true);
        javax.swing.SwingUtilities.invokeLater(() -> posted.scrollRectToVisible(new java.awt.Rectangle(0, 0, posted.getWidth(), posted.getHeight() + 20)));
        note.setText("");
        app.toast(Icons.CHECK, Theme.SAGE, "Your offer is live", Places.name(from.code()) + " to " + Places.name(to.code()) + " at " + Offer.fmt(time.time()));
        update();
    }

    public void reset() {
        waiting = false;
        post.setText("Post offer");
        posted.setVisible(false);
        update();
    }

    @Override
    public void shown() {
        posted.setVisible(false);
        waiting = false;
        post.setText("Post offer");
        update();
    }

    @Override
    public void refresh() {
        if (waiting) {
            return;
        }
        post.setEnabled(!from.code().equals(to.code()));
    }
}
