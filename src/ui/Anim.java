package ui;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Anim {

    private static final List<WeakReference<JComponent>> ITEMS = new ArrayList<>();
    private static final long START = System.nanoTime();
    private static Timer timer;

    private Anim() {
    }

    public static void register(JComponent c) {
        ITEMS.add(new WeakReference<>(c));
        if (timer == null) {
            timer = new Timer(16, e -> tick());
            timer.start();
        }
    }

    private static void tick() {
        Iterator<WeakReference<JComponent>> it = ITEMS.iterator();
        while (it.hasNext()) {
            JComponent c = it.next().get();
            if (c == null) {
                it.remove();
            } else if (c.isShowing()) {
                c.repaint();
            }
        }
    }

    public static double now() {
        return (System.nanoTime() - START) / 1e9;
    }

    public static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    public static double easeOut(double t) {
        double k = 1 - clamp(t);
        return 1 - k * k * k;
    }

    public static double easeInOut(double t) {
        double k = clamp(t);
        return k < 0.5 ? 4 * k * k * k : 1 - Math.pow(-2 * k + 2, 3) / 2;
    }
}
