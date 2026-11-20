package client;

import common.Offer;
import common.Protocol;
import ui.Icons;
import ui.Loader;
import ui.Theme;
import ui.Toast;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClientApp {

    public static final String LOGIN = "login";
    public static final String HOME = "home";
    public static final String OFFER = "offer";
    public static final String FIND = "find";
    public static final String RIDES = "rides";

    public static final class Note {
        public final String kind;
        public final String text;
        public final LocalTime time;
        public boolean unread;

        Note(String kind, String text, boolean unread) {
            this.kind = kind;
            this.text = text;
            this.time = LocalTime.now();
            this.unread = unread;
        }
    }

    private final JFrame frame = new JFrame("RouteMatch");
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final NavBar nav;
    private final Loader loader = new Loader();
    private final Toast toast = new Toast();
    private final Map<String, Screen> screens = new LinkedHashMap<>();
    private final LoginScreen login;
    private final HomeScreen home;
    private final OfferScreen offer;
    private final RequestScreen find;
    private final MatchListScreen rides;
    private final List<Note> notes = new ArrayList<>();
    private List<Offer> offers = new ArrayList<>();
    private Connection connection;
    private String user = "";
    private String address = "";
    private String page = "";
    private int online;
    private int openRequests;
    private int matched;

    public ClientApp() {
        nav = new NavBar(this);
        login = new LoginScreen(this);
        home = new HomeScreen(this);
        offer = new OfferScreen(this);
        find = new RequestScreen(this);
        rides = new MatchListScreen(this);
        screens.put(LOGIN, login);
        screens.put(HOME, home);
        screens.put(OFFER, offer);
        screens.put(FIND, find);
        screens.put(RIDES, rides);
        deck.setBackground(Theme.BG);
        for (Map.Entry<String, Screen> e : screens.entrySet()) {
            deck.add(e.getValue(), e.getKey());
        }
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.BG);
        shell.add(nav, BorderLayout.NORTH);
        shell.add(deck, BorderLayout.CENTER);
        nav.setVisible(false);
        frame.setContentPane(shell);
        JLayeredPane layers = frame.getLayeredPane();
        layers.add(toast, JLayeredPane.POPUP_LAYER);
        layers.add(loader, JLayeredPane.DRAG_LAYER);
        layers.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                loader.setBounds(0, 0, layers.getWidth(), layers.getHeight());
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (connection != null) {
                    connection.send(Protocol.LOGOUT);
                    connection.close();
                }
            }
        });
        frame.setSize(Theme.windowSize(1280, 800));
        frame.setMinimumSize(new Dimension(1000, 660));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Theme.BG);
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new ClientApp().start());
    }

    private void start() {
        frame.setVisible(true);
        loader.setBounds(0, 0, frame.getLayeredPane().getWidth(), frame.getLayeredPane().getHeight());
        go(LOGIN);
    }

    public void go(String target) {
        if (target.equals(page)) {
            return;
        }
        if (!target.equals(LOGIN) && user.isEmpty()) {
            return;
        }
        loader.play(() -> {
            page = target;
            nav.setVisible(!target.equals(LOGIN));
            nav.setActive(target);
            cards.show(deck, target);
            screens.get(target).shown();
        });
    }

    public void connect(String name, String host, int port) {
        login.status(LoginScreen.Status.BUSY, "Connecting to " + host + ":" + port + "\u2026");
        Thread t = new Thread(() -> {
            try {
                Connection c = new Connection(host, port);
                SwingUtilities.invokeLater(() -> {
                    connection = c;
                    address = host + ":" + port;
                    c.listen(this::handle, this::closed);
                    c.send(Protocol.LOGIN, name);
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> login.status(LoginScreen.Status.ERROR, "Could not reach a server at " + host + ":" + port));
            }
        }, "connect");
        t.setDaemon(true);
        t.start();
    }

    public void send(String command, Object... parts) {
        if (connection != null) {
            connection.send(command, parts);
        }
    }

    public void logout() {
        if (connection != null) {
            connection.send(Protocol.LOGOUT);
            connection.close();
        }
        reset();
        login.status(LoginScreen.Status.IDLE, "Signed out");
        go(LOGIN);
    }

    private void reset() {
        connection = null;
        user = "";
        offers = new ArrayList<>();
        notes.clear();
        online = 0;
        openRequests = 0;
        matched = 0;
        find.reset();
        offer.reset();
    }

    private void closed() {
        boolean wasIn = !user.isEmpty();
        reset();
        if (wasIn) {
            login.status(LoginScreen.Status.ERROR, "Connection to the server was lost");
            page = "";
            go(LOGIN);
        } else {
            login.status(LoginScreen.Status.ERROR, "The server closed the connection");
        }
    }

    private void handle(String line) {
        String[] p = Protocol.parse(line);
        switch (p[0]) {
            case Protocol.WELCOME:
                user = p[1];
                login.status(LoginScreen.Status.OK, "Connected as " + user);
                nav.update(user, address, unread());
                go(HOME);
                break;
            case Protocol.OFFERS:
                offers = Offer.decodeList(p.length > 1 ? p[1] : "");
                find.research();
                refresh();
                break;
            case Protocol.STATS:
                online = Integer.parseInt(p[1]);
                openRequests = Integer.parseInt(p[3]);
                matched = Integer.parseInt(p[4]);
                refresh();
                break;
            case Protocol.MATCHES:
                find.showMatches(Offer.decodeList(p.length > 1 ? p[1] : ""));
                break;
            case Protocol.OFFER_OK: {
                offer.posted(Integer.parseInt(p[1]));
                break;
            }
            case Protocol.BOOK_OK: {
                Offer o = offer(Integer.parseInt(p[1]));
                String text = o == null ? "Your seat is booked" : "You booked a seat with " + o.driver + " \u00B7 " + o.routeLabel() + " at " + Offer.fmt(o.time);
                addNote(Protocol.N_BOOKED, text, false);
                toast(Icons.CHECK, Theme.SAGE, "Seat booked", text);
                break;
            }
            case Protocol.BOOK_FAIL:
                toast(Icons.CLOSE, Theme.DANGER, "Booking failed", p.length > 2 ? p[2] : "Please try another ride");
                find.research();
                break;
            case Protocol.NOTIFY: {
                String kind = p[1];
                String text = p.length > 2 ? p[2] : "";
                addNote(kind, text, true);
                toast(noteIcon(kind), noteColor(kind), noteTitle(kind), text);
                break;
            }
            case Protocol.ERROR:
                if (user.isEmpty()) {
                    login.status(LoginScreen.Status.ERROR, p.length > 1 ? p[1] : "Login failed");
                    if (connection != null) {
                        connection.close();
                        connection = null;
                    }
                } else {
                    offer.reset();
                    toast(Icons.CLOSE, Theme.DANGER, "Server message", p.length > 1 ? p[1] : "Something went wrong");
                }
                break;
            default:
                break;
        }
    }

    private void addNote(String kind, String text, boolean unread) {
        notes.add(0, new Note(kind, text, unread));
        while (notes.size() > 50) {
            notes.remove(notes.size() - 1);
        }
        refresh();
    }

    public void markAllRead() {
        for (Note n : notes) {
            n.unread = false;
        }
        refresh();
    }

    private void refresh() {
        nav.update(user, address, unread());
        Screen s = screens.get(page);
        if (s != null && !page.equals(LOGIN)) {
            s.refresh();
        }
    }

    public void toast(Shape icon, Color tint, String title, String body) {
        toast.setTop(page.equals(LOGIN) ? 24 : 96);
        toast.show(icon, tint, title, body);
    }

    public void bookFromHome(Offer o) {
        find.prefill(o);
        go(FIND);
    }

    public Offer offer(int id) {
        for (Offer o : offers) {
            if (o.id == id) {
                return o;
            }
        }
        return null;
    }

    public List<Offer> offers() {
        return offers;
    }

    public List<Note> notes() {
        return notes;
    }

    public int unread() {
        int n = 0;
        for (Note note : notes) {
            if (note.unread) {
                n++;
            }
        }
        return n;
    }

    public String user() {
        return user;
    }

    public int online() {
        return online;
    }

    public int openRequests() {
        return openRequests;
    }

    public int matched() {
        return matched;
    }

    public static Shape noteIcon(String kind) {
        switch (kind) {
            case Protocol.N_BOOKED:
                return Icons.TICKET;
            case Protocol.N_CANCELLED:
            case Protocol.N_REMOVED:
                return Icons.CLOSE;
            case Protocol.N_MATCH:
                return Icons.SEARCH;
            default:
                return Icons.BELL;
        }
    }

    public static Color noteColor(String kind) {
        switch (kind) {
            case Protocol.N_BOOKED:
                return Theme.ACCENT;
            case Protocol.N_CANCELLED:
            case Protocol.N_REMOVED:
                return Theme.DANGER;
            case Protocol.N_MATCH:
                return Theme.SAGE;
            default:
                return Theme.SKY;
        }
    }

    public static String noteTitle(String kind) {
        switch (kind) {
            case Protocol.N_BOOKED:
                return "New booking";
            case Protocol.N_CANCELLED:
                return "Seat cancelled";
            case Protocol.N_REMOVED:
                return "Ride cancelled";
            case Protocol.N_MATCH:
                return "A ride matches your request";
            default:
                return "Notification";
        }
    }

    public static void avatar(Graphics2D g, String name, double x, double y, double size) {
        g.setPaint(new GradientPaint((float) x, (float) y, Theme.ACCENT_LIGHT, (float) (x + size), (float) (y + size), Theme.ACCENT_DEEP));
        g.fill(new Ellipse2D.Double(x, y, size, size));
        String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        g.setFont(Theme.font(800, (float) (size * 0.42)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.INK);
        g.drawString(initial, (float) (x + (size - fm.stringWidth(initial)) / 2), (float) (y + (size + fm.getAscent() - fm.getDescent()) / 2));
    }
}
