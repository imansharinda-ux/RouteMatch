package client;

import ui.Theme;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Avatar extends JComponent {

    private String name;
    private final int size;

    public Avatar(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public void setName(String name) {
        this.name = name;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(size, size);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        ClientApp.avatar(g, name, (getWidth() - size) / 2.0, (getHeight() - size) / 2.0, size);
        g.dispose();
    }
}
