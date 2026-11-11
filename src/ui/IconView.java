package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public class IconView extends JComponent {

    private Shape icon;
    private Color color;
    private final int iconSize;
    private final int box;
    private Color tile;
    private int tileRadius = 12;
    private double stroke = 2;

    public IconView(Shape icon, int iconSize, Color color) {
        this(icon, iconSize, color, iconSize, null);
    }

    public IconView(Shape icon, int iconSize, Color color, int box, Color tile) {
        this.icon = icon;
        this.iconSize = iconSize;
        this.color = color;
        this.box = box;
        this.tile = tile;
        setOpaque(false);
    }

    public IconView set(Shape icon, Color color) {
        this.icon = icon;
        this.color = color;
        repaint();
        return this;
    }

    public IconView tile(Color tile, int radius) {
        this.tile = tile;
        this.tileRadius = radius;
        repaint();
        return this;
    }

    public IconView stroke(double stroke) {
        this.stroke = stroke;
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(box, box);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double x = (getWidth() - box) / 2.0, y = (getHeight() - box) / 2.0;
        if (tile != null) {
            g.setColor(tile);
            g.fill(new RoundRectangle2D.Double(x, y, box, box, tileRadius * 2.0, tileRadius * 2.0));
        }
        if (icon != null) {
            Icons.draw(g, icon, x + (box - iconSize) / 2.0, y + (box - iconSize) / 2.0, iconSize, color, stroke);
        }
        g.dispose();
    }
}
