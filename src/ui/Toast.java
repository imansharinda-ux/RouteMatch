package ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Toast extends RoundPanel {

    private static final int WIDTH = 380;

    private final IconView icon = new IconView(Icons.BELL, 18, Theme.ACCENT, 38, Theme.alpha(Theme.ACCENT, 0.14));
    private final JLabel title = Ui.label("", 700, 14.5f, Theme.TEXT);
    private final JLabel body = Ui.label("", 500, 13, Theme.MUTED);
    private final Timer timer;
    private double shownAt;
    private double hideAt;
    private float alpha;
    private int top = 96;

    public Toast() {
        super(new BorderLayout(12, 0), new Color(0x1F1B18), Theme.LINE, 18);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 16));
        JPanel text = Ui.column(3, title, body);
        add(icon, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
        setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                hideAt = Math.min(hideAt, Anim.now());
            }
        });
        timer = new Timer(16, e -> step());
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void show(Shape shape, Color tint, String heading, String message) {
        icon.set(shape, tint).tile(Theme.alpha(tint, 0.14), 12);
        title.setText(heading);
        body.setText(Ui.html(message, WIDTH - 110));
        shownAt = Anim.now();
        hideAt = shownAt + 4.8;
        setVisible(true);
        place();
        timer.restart();
    }

    private void place() {
        Container parent = getParent();
        if (parent == null) {
            return;
        }
        Dimension d = getPreferredSize();
        double t = Anim.now();
        double in = Anim.easeOut((t - shownAt) / 0.35);
        double out = Anim.clamp((t - hideAt) / 0.3);
        alpha = (float) Math.max(0, Math.min(in, 1 - out));
        int x = parent.getWidth() - WIDTH - 24;
        int y = (int) (top - 16 * (1 - in) - 10 * out);
        setBounds(x, y, WIDTH, d.height);
    }

    private void step() {
        place();
        repaint();
        if (Anim.now() > hideAt + 0.3) {
            timer.stop();
            setVisible(false);
        }
    }

    @Override
    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, alpha))));
        super.paint(g);
        g.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(WIDTH, d.height);
    }
}
