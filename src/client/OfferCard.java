package client;

import common.Offer;
import common.Places;
import ui.Icons;
import ui.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

public class OfferCard extends JComponent {

    private final Offer offer;
    private final String me;
    private boolean selected;
    private boolean hover;

    public OfferCard(Offer offer, String me, boolean selected, Runnable onPick) {
        this.offer = offer;
        this.me = me;
        this.selected = selected;
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
                onPick.run();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, 132);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double w = getWidth(), h = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 40, 40);
        g.setColor(selected ? new Color(0x1E1A16) : hover ? Theme.PANEL_2 : Theme.PANEL);
        g.fill(shape);
        g.setStroke(new BasicStroke(selected ? 1.5f : 1f));
        g.setColor(selected ? Theme.ACCENT : Theme.LINE_SOFT);
        g.draw(shape);
        g.setColor(new Color(255, 255, 255, 12));
        g.fill(new RoundRectangle2D.Double(16, 16, 42, 42, 24, 24));
        Icons.draw(g, Icons.vehicle(offer.vehicle), 24, 24, 26, selected ? Theme.ACCENT : Theme.TEXT_2, 1.7);
        boolean mine = offer.driver.equals(me);
        g.setFont(Theme.font(700, 15.5f));
        g.setColor(Theme.TEXT);
        g.drawString(mine ? offer.driver + " (you)" : offer.driver, 70, 34);
        g.setFont(Theme.font(500, 12.5f));
        g.setColor(Theme.DIM);
        g.drawString(offer.vehicle.label, 70, 52);
        g.setFont(Theme.font(800, 17));
        FontMetrics fm = g.getFontMetrics();
        String time = Offer.fmt(offer.time);
        g.setColor(Theme.TEXT);
        g.drawString(time, (float) (w - 18 - fm.stringWidth(time)), 36);
        double y = 82;
        g.setFont(Theme.font(600, 13.5f));
        fm = g.getFontMetrics();
        String from = Places.name(offer.from), to = Places.name(offer.to);
        g.setColor(Theme.TEXT);
        g.fill(new Ellipse2D.Double(18, y - 4, 8, 8));
        g.setColor(Theme.TEXT_2);
        g.drawString(from, 32, (float) (y + 5));
        double tx = w - 18 - 8 - 6 - fm.stringWidth(to);
        double lx1 = 32 + fm.stringWidth(from) + 10, lx2 = tx - 10;
        if (lx2 > lx1) {
            g.setColor(Theme.alpha(Theme.ROUTE, 0.5));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{3f, 4f}, 0f));
            g.draw(new Line2D.Double(lx1, y, lx2, y));
        }
        g.setColor(Theme.TEXT_2);
        g.drawString(to, (float) tx, (float) (y + 5));
        g.setColor(Theme.PIN);
        g.fill(new Ellipse2D.Double(w - 26, y - 4, 8, 8));
        g.setFont(Theme.font(700, 12.5f));
        String seat = offer.seatLabel();
        Color tint = offer.isFull() ? Theme.DANGER : Theme.SAGE;
        if (offer.hasPassenger(me)) {
            seat = "Booked by you";
            tint = Theme.ACCENT;
        }
        fm = g.getFontMetrics();
        double bw = fm.stringWidth(seat) + 18;
        g.setColor(Theme.alpha(tint, 0.13));
        g.fill(new RoundRectangle2D.Double(16, h - 36, bw, 22, 22, 22));
        g.setColor(tint);
        g.drawString(seat, 25, (float) (h - 21));
        g.setFont(Theme.font(500, 12.5f));
        g.setColor(Theme.DIM);
        String via = Places.viaLabel(offer.from, offer.to);
        g.drawString(via, (float) (16 + bw + 10), (float) (h - 21));
        g.dispose();
    }
}
