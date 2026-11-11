package client;

import ui.Theme;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public abstract class Screen extends JPanel {

    protected final ClientApp app;

    protected Screen(ClientApp app) {
        super(new BorderLayout());
        this.app = app;
        setBackground(Theme.BG);
    }

    public void shown() {
        refresh();
    }

    public abstract void refresh();
}
