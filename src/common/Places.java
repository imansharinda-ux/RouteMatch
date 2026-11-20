package common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Places {

    public static final class Place {
        public final String code;
        public final String name;
        public final String region;
        public final double x;
        public final double y;
        public final boolean major;
        public final boolean labelLeft;
        public final boolean town;

        Place(String code, String name, String region, double x, double y, boolean major, boolean labelLeft, boolean town) {
            this.code = code;
            this.name = name;
            this.region = region;
            this.x = x;
            this.y = y;
            this.major = major;
            this.labelLeft = labelLeft;
            this.town = town;
        }
    }

    private static final class Edge {
        final String a;
        final String b;
        final double[] via;

        Edge(String a, String b, double[] via) {
            this.a = a;
            this.b = b;
            this.via = via;
        }
    }

    private static final Map<String, Place> PLACES = new LinkedHashMap<>();
    private static final List<Place> GRAPH = new ArrayList<>();
    private static final List<Edge> EDGES = new ArrayList<>();
    private static final List<Place> SORTED = new ArrayList<>();

    static {
        add("col", "Colombo", 144, 1189, true, true);
        add("neg", "Negombo", 134, 1077, true, true);
        add("kan", "Kandy", 453, 1044, true, false);
        add("gal", "Galle", 288, 1539, true, true);
        add("fac", "Faculty of Engineering", 277, 1528, false, true);
        add("mat", "Matara", 422, 1578, true, false);
        add("jaf", "Jaffna", 210, 95, true, true);
        add("tri", "Trincomalee", 686, 525, true, false);
        add("bat", "Batticaloa", 870, 868, true, false);
        add("anu", "Anuradhapura", 361, 635, true, false);
        add("kur", "Kurunegala", 345, 965, true, false);
        add("nuw", "Nuwara Eliya", 516, 1180, true, false);
        add("ell", "Ella", 619, 1213, false, false);
        add("bad", "Badulla", 622, 1163, true, false);
        add("rat", "Ratnapura", 360, 1287, true, true);
        add("ham", "Hambantota", 647, 1510, true, false);
        add("put", "Puttalam", 131, 746, true, true);
        add("dam", "Dambulla", 460, 811, true, false);
        add("pol", "Polonnaruwa", 608, 784, true, false);
        add("vav", "Vavuniya", 399, 459, true, false);
        add("kil", "Kilinochchi", 351, 208, false, false);
        add("man", "Mannar", 162, 368, true, true);
        add("aru", "Arugam Bay", 935, 1224, true, false);
        add("kal", "Kalutara", 184, 1326, false, true);
        add("hik", "Hikkaduwa", 243, 1504, false, true);
        add("mir", "Mirissa", 389, 1581, false, false);
        add("chi", "Chilaw", 118, 930, false, true);
        add("mtl", "Matale", 449, 973, false, false);
        add("keg", "Kegalle", 339, 1059, false, true);
        add("mon", "Monaragala", 740, 1211, false, false);
        add("wel", "Wellawaya", 641, 1265, false, false);
        add("sig", "Sigiriya", 504, 777, false, false);
        add("hab", "Habarana", 500, 742, false, false);
        add("pel", "Pelmadulla", 416, 1312, false, false);
        add("emb", "Embilipitiya", 540, 1422, false, false);
        add("tan", "Tangalle", 518, 1548, false, false);
        add("ben", "Bentota", 198, 1390, false, true);

        edge("col", "keg");
        edge("keg", "kan");
        edge("col", "kal");
        edge("kal", "ben");
        edge("ben", "hik");
        edge("hik", "gal");
        edge("gal", "mir", 345, 1541);
        edge("mir", "mat");
        edge("mat", "tan");
        edge("tan", "ham", 580, 1509);
        edge("col", "neg", 188, 1116);
        edge("neg", "chi", 132, 1003);
        edge("chi", "put", 154, 850);
        edge("col", "rat");
        edge("rat", "pel");
        edge("pel", "wel");
        edge("wel", "mon");
        edge("mon", "aru");
        edge("kan", "nuw");
        edge("nuw", "ell");
        edge("ell", "bad");
        edge("ell", "wel");
        edge("kur", "dam");
        edge("dam", "hab");
        edge("hab", "tri", 560, 680, 620, 610, 660, 560);
        edge("kan", "mtl");
        edge("mtl", "dam");
        edge("dam", "anu");
        edge("anu", "vav");
        edge("vav", "kil");
        edge("kil", "jaf");
        edge("kan", "kur");
        edge("kur", "put");
        edge("col", "kur");
        edge("put", "anu");
        edge("anu", "tri");
        edge("hab", "pol");
        edge("pol", "bat", 700, 820, 790, 840, 840, 858);
        edge("man", "vav");
        edge("tri", "bat", 715, 600, 735, 680, 755, 740, 790, 800, 835, 850);
        edge("bat", "aru");
        edge("pel", "emb");
        edge("emb", "ham");
        edge("mon", "ham");
        edge("gal", "fac");
        edge("sig", "dam");
        edge("sig", "hab");
        edge("bad", "mon");
        loadTowns();
        SORTED.addAll(PLACES.values());
        SORTED.sort(Comparator.comparing((Place p) -> p.name.toLowerCase(Locale.ROOT)).thenComparing(p -> p.region));
    }

    private Places() {
    }

    private static void add(String code, String name, double x, double y, boolean major, boolean left) {
        Place p = new Place(code, name, "", x, y, major, left, false);
        PLACES.put(code, p);
        GRAPH.add(p);
    }

    private static void edge(String a, String b, double... via) {
        EDGES.add(new Edge(a, b, via));
    }

    private static void loadTowns() {
        try (InputStream in = Places.class.getResourceAsStream("/common/lk_places.txt")) {
            if (in == null) {
                return;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            int n = 0;
            while ((line = r.readLine()) != null) {
                String[] f = line.split("\\|", -1);
                if (f.length < 4) {
                    continue;
                }
                double lat = Double.parseDouble(f[1]);
                double lon = Double.parseDouble(f[2]);
                String code = "t" + (n++);
                PLACES.put(code, new Place(code, f[0], f[3], (lon - 79.5) * 400, (9.9 - lat) * 400, false, false, true));
            }
        } catch (Exception ignored) {
        }
    }

    public static List<Place> graph() {
        return new ArrayList<>(GRAPH);
    }

    public static List<Place> sorted() {
        return SORTED;
    }

    public static List<Place> search(String query, int limit) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Place> starts = new ArrayList<>();
        List<Place> contains = new ArrayList<>();
        for (Place p : SORTED) {
            String name = p.name.toLowerCase(Locale.ROOT);
            if (q.isEmpty() || name.startsWith(q)) {
                starts.add(p);
            } else if (name.contains(q)) {
                contains.add(p);
            }
            if (starts.size() >= limit) {
                break;
            }
        }
        if (q.isEmpty()) {
            List<Place> main = new ArrayList<>(GRAPH);
            main.sort(Comparator.comparing(p -> p.name));
            return main;
        }
        starts.addAll(contains);
        return starts.size() > limit ? new ArrayList<>(starts.subList(0, limit)) : starts;
    }

    public static Place findByName(String name) {
        String n = name == null ? "" : name.trim();
        for (Place p : GRAPH) {
            if (p.name.equalsIgnoreCase(n)) {
                return p;
            }
        }
        for (Place p : SORTED) {
            if (p.name.equalsIgnoreCase(n)) {
                return p;
            }
        }
        return null;
    }

    public static boolean exists(String code) {
        return PLACES.containsKey(code);
    }

    public static Place get(String code) {
        return PLACES.get(code);
    }

    public static String name(String code) {
        Place p = PLACES.get(code);
        return p == null ? code : p.name;
    }

    private static boolean isGraph(String code) {
        Place p = PLACES.get(code);
        return p != null && !p.town;
    }

    private static List<double[]> edgePath(String a, String b) {
        List<double[]> pts = new ArrayList<>();
        Place pa = PLACES.get(a);
        Place pb = PLACES.get(b);
        if (isGraph(a) && isGraph(b)) {
            for (Edge e : EDGES) {
                boolean forward = e.a.equals(a) && e.b.equals(b);
                boolean backward = e.a.equals(b) && e.b.equals(a);
                if (!forward && !backward) {
                    continue;
                }
                Place ea = PLACES.get(e.a);
                Place eb = PLACES.get(e.b);
                pts.add(new double[]{ea.x, ea.y});
                for (int i = 0; i + 1 < e.via.length; i += 2) {
                    pts.add(new double[]{e.via[i], e.via[i + 1]});
                }
                pts.add(new double[]{eb.x, eb.y});
                if (backward) {
                    Collections.reverse(pts);
                }
                return pts;
            }
        }
        pts.add(new double[]{pa.x, pa.y});
        pts.add(new double[]{pb.x, pb.y});
        return pts;
    }

    private static double length(List<double[]> pts) {
        double total = 0;
        for (int i = 1; i < pts.size(); i++) {
            total += Math.hypot(pts.get(i)[0] - pts.get(i - 1)[0], pts.get(i)[1] - pts.get(i - 1)[1]);
        }
        return total;
    }

    private static double distance(Place a, Place b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private static List<Place> nearest(Place p, int count) {
        List<Place> list = new ArrayList<>(GRAPH);
        list.sort(Comparator.comparingDouble(g -> distance(p, g)));
        return list.subList(0, Math.min(count, list.size()));
    }

    private static List<String> graphRoute(String from, String to) {
        List<String> result = new ArrayList<>();
        if (from.equals(to)) {
            result.add(from);
            return result;
        }
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> done = new HashSet<>();
        for (Place p : GRAPH) {
            dist.put(p.code, Double.MAX_VALUE);
        }
        dist.put(from, 0.0);
        while (true) {
            String u = null;
            for (Place p : GRAPH) {
                if (!done.contains(p.code) && (u == null || dist.get(p.code) < dist.get(u))) {
                    u = p.code;
                }
            }
            if (u == null || dist.get(u) == Double.MAX_VALUE || u.equals(to)) {
                break;
            }
            done.add(u);
            for (Edge e : EDGES) {
                String v = e.a.equals(u) ? e.b : e.b.equals(u) ? e.a : null;
                if (v == null || done.contains(v)) {
                    continue;
                }
                double nd = dist.get(u) + length(edgePath(u, v));
                if (nd < dist.get(v)) {
                    dist.put(v, nd);
                    prev.put(v, u);
                }
            }
        }
        if (!prev.containsKey(to)) {
            result.add(from);
            result.add(to);
            return result;
        }
        String cur = to;
        while (cur != null) {
            result.add(0, cur);
            cur = prev.get(cur);
        }
        return result;
    }

    private static double routeLength(List<String> ids) {
        double total = 0;
        for (int i = 1; i < ids.size(); i++) {
            total += length(edgePath(ids.get(i - 1), ids.get(i)));
        }
        return total;
    }

    public static List<String> route(String from, String to) {
        List<String> result = new ArrayList<>();
        if (!exists(from) || !exists(to)) {
            return result;
        }
        if (from.equals(to)) {
            result.add(from);
            return result;
        }
        Place a = PLACES.get(from);
        Place b = PLACES.get(to);
        List<Place> startOptions = a.town ? nearest(a, 2) : List.of(a);
        List<Place> endOptions = b.town ? nearest(b, 2) : List.of(b);
        List<String> best = null;
        double bestLength = Double.MAX_VALUE;
        for (Place s : startOptions) {
            for (Place e : endOptions) {
                List<String> ids = new ArrayList<>();
                if (a.town) {
                    ids.add(from);
                }
                for (String id : graphRoute(s.code, e.code)) {
                    if (ids.isEmpty() || !ids.get(ids.size() - 1).equals(id)) {
                        ids.add(id);
                    }
                }
                if (b.town) {
                    ids.add(to);
                }
                double len = routeLength(ids);
                if (len < bestLength) {
                    bestLength = len;
                    best = ids;
                }
            }
        }
        double direct = distance(a, b);
        if (best == null || (a.town && b.town && direct * 1.25 < bestLength && direct < 120)) {
            best = new ArrayList<>(List.of(from, to));
        }
        return best;
    }

    public static List<double[]> points(String from, String to) {
        List<String> ids = route(from, to);
        List<double[]> pts = new ArrayList<>();
        if (ids.isEmpty()) {
            return pts;
        }
        Place first = PLACES.get(ids.get(0));
        pts.add(new double[]{first.x, first.y});
        for (int i = 1; i < ids.size(); i++) {
            List<double[]> seg = edgePath(ids.get(i - 1), ids.get(i));
            pts.addAll(seg.subList(1, seg.size()));
        }
        return pts;
    }

    public static List<String> via(String from, String to) {
        List<String> ids = route(from, to);
        List<String> names = new ArrayList<>();
        for (int i = 1; i < ids.size() - 1; i++) {
            names.add(name(ids.get(i)));
        }
        return names;
    }

    public static String viaLabel(String from, String to) {
        List<String> v = via(from, to);
        if (v.isEmpty()) {
            return "Direct route";
        }
        if (v.size() <= 2) {
            return "via " + String.join(", ", v);
        }
        return "via " + v.get(0) + ", " + v.get(1) + " +" + (v.size() - 2);
    }
}
