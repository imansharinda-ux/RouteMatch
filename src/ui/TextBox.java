package ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.KeyboardFocusManager;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class TextBox extends JPanel {

    private final JTextComponent field;
    private final JPanel trailing;
    private Color fill = Theme.alpha(Theme.FIELD, 0.85);
    private int height = 52;

    public TextBox(Shape icon, String placeholder, boolean secret) {
        super(new Ui.Row(12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, icon == null ? 16 : 16, 0, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        if (icon != null) {
            add(new IconView(icon, 18, Theme.DIM));
        }
        field = secret ? new Secret(placeholder) : new Input(placeholder);
        hook(field);
        add(Ui.fill(field));
        trailing = Ui.clear(new Ui.Row(12));
        trailing.setVisible(false);
        add(trailing);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                field.requestFocusInWindow();
            }
        });
    }

    public TextBox fill(Color fill) {
        this.fill = fill;
        return this;
    }

    public TextBox height(int height) {
        this.height = height;
        return this;
    }

    public void hook(JTextComponent c) {
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    public void addTrailing(JComponent c) {
        trailing.add(c);
        trailing.setVisible(true);
        if (c instanceof JTextComponent) {
            hook((JTextComponent) c);
        }
    }

    public JTextComponent field() {
        return field;
    }

    public String text() {
        return field.getText().trim();
    }

    public void setText(String text) {
        field.setText(text);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, height);
    }

    private boolean focused() {
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return owner != null && SwingUtilities.isDescendingFrom(owner, this);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        boolean on = focused();
        RoundRectangle2D shape = new RoundRectangle2D.Double(2.5, 2.5, getWidth() - 5, getHeight() - 5, 30, 30);
        if (on) {
            g.setColor(Theme.alpha(Theme.ACCENT, 0.14));
            g.setStroke(new BasicStroke(4f));
            g.draw(shape);
        }
        g.setColor(fill);
        g.fill(shape);
        g.setStroke(new BasicStroke(1f));
        g.setColor(on ? Theme.ACCENT : new Color(255, 255, 255, 22));
        g.draw(shape);
        g.dispose();
    }

    static void style(JTextComponent f) {
        f.setOpaque(false);
        f.setBorder(BorderFactory.createEmptyBorder());
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setSelectionColor(Theme.alpha(Theme.ACCENT, 0.35));
        f.setSelectedTextColor(Theme.TEXT);
        f.setFont(Theme.font(500, 15));
    }

    static void placeholder(Graphics g0, JTextComponent f, String text) {
        if (!f.getText().isEmpty() || text == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        g.setFont(f.getFont());
        g.setColor(Theme.DIM);
        FontMetrics fm = g.getFontMetrics();
        int y = (f.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, f.getInsets().left, y);
        g.dispose();
    }

    public static class Input extends JTextField {
        private final String hint;

        public Input(String hint) {
            this.hint = hint;
            style(this);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, 40), 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            placeholder(g, this, hint);
        }
    }

    public static class Secret extends JPasswordField {
        private final String hint;

        public Secret(String hint) {
            this.hint = hint;
            style(this);
            setEchoChar('\u2022');
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, 40), 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            placeholder(g, this, hint);
        }
    }
}
