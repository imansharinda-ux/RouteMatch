package client;

import ui.Icons;
import ui.Logo;
import ui.PulseDot;
import ui.RoundPanel;
import ui.Theme;
import ui.Ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class NavBar extends JPanel {

    private final ClientApp app;
    private final List<Item> items = new ArrayList<>();
    private final JLabel live = Ui.label("Live", 600, 13, Theme.SAGE_TEXT);
    private final PulseDot dot = new PulseDot(Theme.SAGE);
    private final Bell bell = new Bell();
    private final UserChip user = new UserChip();
    private String active = "";

    public NavBar(ClientApp app) {
        super(new BorderLayout());
        this.app = app;
        setOpaque(true);
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(14, 20, 0, 20));
        RoundPanel bar = new RoundPanel(new BorderLayout(), Theme.PANEL, new Color(255, 255, 255, 18), 22);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 12));
        Logo logo = new Logo(40, true);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                app.go(ClientApp.HOME);
            }
        });
        RoundPanel menu = new RoundPanel(new FlowLayout(FlowLayout.CENTER, 4, 0), new Color(13, 12, 11, 170), new Color(255, 255, 255, 15), 999);
        menu.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        items.add(new Item(ClientApp.HOME, "Home", Icons.HOME));
        items.add(new Item(ClientApp.OFFER, "Offer a ride", Icons.PLUS));
        items.add(new Item(ClientApp.FIND, "Find a ride", Icons.SEARCH));
        items.add(new Item(ClientApp.RIDES, "My rides", Icons.TICKET));
        for (Item i : items) {
            menu.add(i);
        }
        JPanel center = Ui.clear(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.add(menu);
        RoundPanel livePill = new RoundPanel(new BorderLayout(8, 0), Theme.alpha(Theme.SAGE, 0.08), Theme.alpha(Theme.SAGE, 0.25), 999);
        livePill.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 14));
        livePill.add(dot, BorderLayout.WEST);
        livePill.add(live, BorderLayout.CENTER);
        JPanel right = Ui.row(10, livePill, bell, user);
        bar.add(logo, BorderLayout.WEST);
        bar.add(center, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        add(bar, BorderLayout.CENTER);
    }

    public void setActive(String page) {
        active = page;
        repaint();
    }

    public void update(String name, String address, int unread) {
        live.setText("Live \u00B7 " + address);
        user.name = name;
        bell.unread = unread;
        revalidate();
        repaint();
    }

    private final class Item extends JComponent {
        private final String page;
        private final String label;
        private final Shape icon;
        private boolean hover;

        Item(String page, String label, Shape icon) {
            this.page = page;
            this.label = label;
            this.icon = icon;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
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
                    app.go(page);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(700, 14));
            return new Dimension(fm.stringWidth(label) + 36 + 25, 40);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            boolean on = page.equals(active);
            if (on) {
                g.setColor(Theme.TEXT);
                g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
            } else if (hover) {
                g.setColor(new Color(255, 255, 255, 14));
                g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 40, 40));
            }
            Color fg = on ? Theme.BG : hover ? Theme.TEXT : Theme.MUTED;
            Icons.draw(g, icon, 18, (getHeight() - 17) / 2.0, 17, fg, 2.1);
            g.setFont(Theme.font(on ? 700 : 500, 14));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(fg);
            g.drawString(label, 18 + 25, (getHeight() - fm.getAscent() - fm.getDescent()) / 2f + fm.getAscent());
            g.dispose();
        }
    }

    private final class Bell extends JComponent {
        int unread;
        boolean hover;

        Bell() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Notifications");
            addMouseListener(new MouseAdapter() {
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
                    app.go(ClientApp.RIDES);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(44, 44);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            Ellipse2D c = new Ellipse2D.Double(0.5, 0.5, 43, 43);
            g.setColor(new Color(255, 255, 255, hover ? 22 : 10));
            g.fill(c);
            g.setColor(new Color(255, 255, 255, 22));
            g.draw(c);
            Icons.draw(g, Icons.BELL, 12, 12, 20, Theme.TEXT, 1.9);
            if (unread > 0) {
                String n = unread > 9 ? "9+" : String.valueOf(unread);
                g.setFont(Theme.font(800, 10.5f));
                FontMetrics fm = g.getFontMetrics();
                double bw = Math.max(18, fm.stringWidth(n) + 10);
                RoundRectangle2D badge = new RoundRectangle2D.Double(44 - bw + 2, 0, bw, 18, 18, 18);
                g.setColor(Theme.ACCENT);
                g.fill(badge);
                g.setColor(Theme.PANEL);
                g.setStroke(new BasicStroke(2f));
                g.draw(badge);
                g.setColor(Theme.INK);
                g.drawString(n, (float) (badge.getX() + (bw - fm.stringWidth(n)) / 2), 13f);
            }
            g.dispose();
        }
    }

    private final class UserChip extends JComponent {
        String name = "";
        boolean hover;

        UserChip() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
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
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem out = new JMenuItem("Sign out");
                    out.addActionListener(a -> app.logout());
                    menu.add(out);
                    menu.show(UserChip.this, 0, getHeight() + 6);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.font(600, 14));
            return new Dimension(5 + 34 + 10 + fm.stringWidth(name) + 10 + 14 + 14, 44);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.hints(g);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, 43, 44, 44);
            g.setColor(new Color(255, 255, 255, hover ? 22 : 10));
            g.fill(shape);
            g.setColor(new Color(255, 255, 255, 22));
            g.draw(shape);
            ClientApp.avatar(g, name, 5, 5, 34);
            g.setFont(Theme.font(600, 14));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Theme.TEXT);
            g.drawString(name, 49, 22 + (fm.getAscent() - fm.getDescent()) / 2f);
            Icons.draw(g, Icons.CHEVRON_DOWN, 49 + fm.stringWidth(name) + 8, 15, 14, Theme.MUTED, 2.4);
            g.dispose();
        }
    }
}
