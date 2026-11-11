package ui;

import common.Offer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeBox extends JPanel {

    private static final Pattern TIME = Pattern.compile("^\\s*(\\d{1,2})(?:[:.\\s]?(\\d{2}))?\\s*([aApP])?\\.?\\s*[mM]?\\.?\\s*$");

    private final TextBox.Input input = new TextBox.Input("");
    private LocalTime time;
    private Consumer<LocalTime> listener;

    public TimeBox(String caption, LocalTime start) {
        super(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 10));
        time = start.withSecond(0).withNano(0);
        input.setFont(Theme.font(700, 16));
        JPanel text = Ui.column(2, Ui.label(caption, 600, 12, Theme.DIM), input);
        add(new IconView(Icons.CLOCK, 20, Theme.ACCENT), BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        PillButton minus = new PillButton("", PillButton.Style.GHOST).icon(Icons.MINUS, false).size(36, 13);
        PillButton plus = new PillButton("", PillButton.Style.GHOST).icon(Icons.PLUS, false).size(36, 13);
        minus.setToolTipText("15 minutes earlier");
        plus.setToolTipText("15 minutes later");
        minus.addActionListener(e -> set(time.minusMinutes(15), true));
        plus.addActionListener(e -> set(time.plusMinutes(15), true));
        add(Ui.row(6, minus, plus), BorderLayout.EAST);
        input.addActionListener(e -> commit());
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commit();
            }

            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }
        });
        addMouseWheelListener(e -> set(time.plusMinutes(e.getWheelRotation() < 0 ? 5 : -5), true));
        input.addMouseWheelListener(e -> set(time.plusMinutes(e.getWheelRotation() < 0 ? 5 : -5), true));
        set(time, false);
    }

    public static LocalTime nextQuarter() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        int add = 15 - now.getMinute() % 15;
        return now.plusMinutes(add);
    }

    public void onChange(Consumer<LocalTime> listener) {
        this.listener = listener;
    }

    public LocalTime time() {
        return time;
    }

    public void set(LocalTime t, boolean fire) {
        boolean changed = !t.equals(time);
        time = t;
        input.setText(Offer.fmt(t));
        if (fire && changed && listener != null) {
            listener.accept(t);
        }
    }

    private void commit() {
        LocalTime parsed = parse(input.getText());
        set(parsed == null ? time : parsed, true);
        repaint();
    }

    public static LocalTime parse(String text) {
        Matcher m = TIME.matcher(text);
        if (!m.matches()) {
            return null;
        }
        int h = Integer.parseInt(m.group(1));
        int min = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        String ap = m.group(3);
        if (ap != null) {
            boolean pm = ap.equalsIgnoreCase("p");
            if (h < 1 || h > 12) {
                return null;
            }
            h = h % 12 + (pm ? 12 : 0);
        }
        if (h > 23 || min > 59) {
            return null;
        }
        return LocalTime.of(h, min);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        boolean on = input.isFocusOwner();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 32, 32);
        g.setColor(Theme.alpha(Theme.FIELD, 0.85));
        g.fill(shape);
        g.setStroke(new BasicStroke(1f));
        g.setColor(on ? Theme.ACCENT : new Color(255, 255, 255, 22));
        g.draw(shape);
        g.dispose();
    }
}
