package ui;

import common.Places;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

public class PlaceSelect extends JPanel {

    private static final int ROW = 40;

    private final String caption;
    private final Color dot;
    private final TextBox.Input input = new TextBox.Input("Type a town or city");
    private final DefaultListModel<Places.Place> model = new DefaultListModel<>();
    private final JList<Places.Place> list = new JList<>(model);
    private final JPopupMenu popup = new JPopupMenu();
    private final JScrollPane scroll;
    private String code;
    private boolean typing;
    private Consumer<String> listener;

    public PlaceSelect(String caption, Color dot, String code) {
        super(new BorderLayout(8, 0));
        this.caption = caption;
        this.dot = dot;
        this.code = code;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(26, 16, 6, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        input.setFont(Theme.font(700, 16));
        add(input, BorderLayout.CENTER);
        add(new IconView(Icons.SEARCH, 16, Theme.MUTED), BorderLayout.EAST);
        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(Theme.PANEL);
        list.setFixedCellHeight(ROW);
        list.setCellRenderer(new Renderer());
        popup.setFocusable(false);
        popup.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        popup.setBackground(Theme.PANEL);
        scroll = Ui.scroll(list);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(Theme.PANEL);
        popup.add(scroll);
        showName();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                input.requestFocusInWindow();
            }
        });
        list.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) {
                    list.setSelectedIndex(i);
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int i = list.locationToIndex(e.getPoint());
                if (i >= 0) {
                    choose(model.get(i));
                }
            }
        });
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
                input.selectAll();
                update("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
                Places.Place exact = Places.findByName(input.getText());
                if (exact != null && !exact.code.equals(PlaceSelect.this.code)) {
                    choose(exact);
                } else {
                    popup.setVisible(false);
                    showName();
                }
            }
        });
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int i = list.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (!popup.isVisible()) {
                            update(input.getText());
                        } else if (i < model.size() - 1) {
                            list.setSelectedIndex(i + 1);
                            list.ensureIndexIsVisible(i + 1);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        if (i > 0) {
                            list.setSelectedIndex(i - 1);
                            list.ensureIndexIsVisible(i - 1);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (popup.isVisible() && !model.isEmpty()) {
                            choose(model.get(Math.max(0, i)));
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        popup.setVisible(false);
                        showName();
                        input.selectAll();
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void onChange(Consumer<String> listener) {
        this.listener = listener;
    }

    public String code() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        showName();
        repaint();
    }

    private void showName() {
        typing = true;
        input.setText(Places.name(code));
        input.setCaretPosition(0);
        typing = false;
    }

    private void changed() {
        if (typing || !input.isFocusOwner()) {
            return;
        }
        SwingUtilities.invokeLater(() -> update(input.getText()));
    }

    private void update(String query) {
        if (!input.isFocusOwner()) {
            return;
        }
        String q = query.equalsIgnoreCase(Places.name(code)) ? "" : query;
        List<Places.Place> found = Places.search(q, 60);
        model.clear();
        for (Places.Place p : found) {
            model.addElement(p);
        }
        if (model.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        list.setSelectedIndex(0);
        list.ensureIndexIsVisible(0);
        int height = Math.min(8, model.size()) * ROW + 2;
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            int below = w.getHeight() - (SwingUtilities.convertPoint(this, 0, getHeight(), w).y) - 50;
            height = Math.max(ROW * 3, Math.min(height, below));
        }
        scroll.setPreferredSize(new Dimension(Math.max(getWidth(), 260), height));
        popup.pack();
        if (!popup.isVisible()) {
            popup.show(this, 0, getHeight() + 6);
        } else {
            popup.revalidate();
            popup.repaint();
        }
        input.requestFocusInWindow();
    }

    private void choose(Places.Place p) {
        popup.setVisible(false);
        boolean changed = !p.code.equals(code);
        code = p.code;
        showName();
        repaint();
        if (changed && listener != null) {
            listener.accept(code);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 64);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.hints(g);
        double w = getWidth(), h = getHeight();
        boolean on = input.isFocusOwner();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 32, 32);
        g.setColor(Theme.alpha(Theme.FIELD, 0.85));
        g.fill(shape);
        g.setStroke(new BasicStroke(1f));
        g.setColor(on ? Theme.ACCENT : new Color(255, 255, 255, 22));
        g.draw(shape);
        g.setColor(dot);
        g.fill(new Ellipse2D.Double(16, 13, 7, 7));
        g.setFont(Theme.font(600, 12));
        g.setColor(Theme.DIM);
        g.drawString(caption, 30, 21);
        g.dispose();
    }

    private static final class Renderer extends JPanel implements ListCellRenderer<Places.Place> {
        private final JLabel name = Ui.label(" ", 600, 14.5f, Theme.TEXT);
        private final JLabel region = Ui.label(" ", 500, 12.5f, Theme.DIM);
        private boolean selected;

        Renderer() {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            add(name, BorderLayout.CENTER);
            add(region, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Places.Place> l, Places.Place p, int index, boolean isSelected, boolean focus) {
            selected = isSelected;
            name.setText(p.name);
            name.setFont(Theme.font(p.town ? 500 : 700, 14.5f));
            name.setForeground(isSelected ? Theme.ACCENT : Theme.TEXT);
            region.setText(p.town ? (p.region.isEmpty() ? "Sri Lanka" : p.region) : "Main city");
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(selected ? Theme.PANEL_2 : Theme.PANEL);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }
}
