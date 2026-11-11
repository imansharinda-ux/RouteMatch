package ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.util.function.IntConsumer;

public class Stepper extends JPanel {

    private final JLabel value = Ui.label("1", 800, 20, Theme.TEXT);
    private final PillButton minus = new PillButton("", PillButton.Style.GHOST);
    private final PillButton plus = new PillButton("", PillButton.Style.GHOST);
    private int min;
    private int max;
    private int current;
    private IntConsumer listener;

    public Stepper(int min, int max, int current) {
        super(new Ui.Row(10));
        setOpaque(false);
        this.min = min;
        this.max = max;
        this.current = current;
        minus.icon(Icons.MINUS, false).size(40, 15);
        plus.icon(Icons.PLUS, false).size(40, 15);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        value.setPreferredSize(new Dimension(34, 40));
        minus.addActionListener(e -> set(this.current - 1, true));
        plus.addActionListener(e -> set(this.current + 1, true));
        add(minus);
        add(value);
        add(plus);
        set(current, false);
    }

    public void onChange(IntConsumer listener) {
        this.listener = listener;
    }

    public void range(int min, int max) {
        this.min = min;
        this.max = max;
        set(current, true);
    }

    public int value() {
        return current;
    }

    public void set(int v, boolean fire) {
        int nv = Math.max(min, Math.min(max, v));
        boolean changed = nv != current;
        current = nv;
        value.setText(String.valueOf(current));
        minus.setEnabled(current > min);
        plus.setEnabled(current < max);
        if (fire && changed && listener != null) {
            listener.accept(current);
        }
    }
}
