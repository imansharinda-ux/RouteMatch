package common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class Offer {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    public final int id;
    public final String driver;
    public final String from;
    public final String to;
    public final LocalTime time;
    public final Vehicle vehicle;
    public final int seats;
    public final String note;
    public final LocalTime postedAt;
    private final List<String> passengers = new CopyOnWriteArrayList<>();

    public Offer(int id, String driver, String from, String to, LocalTime time, Vehicle vehicle, int seats, String note, LocalTime postedAt) {
        this.id = id;
        this.driver = driver;
        this.from = from;
        this.to = to;
        this.time = time;
        this.vehicle = vehicle;
        this.seats = seats;
        this.note = note == null ? "" : note;
        this.postedAt = postedAt;
    }

    public static String hm(LocalTime t) {
        return t.format(HM);
    }

    public static String fmt(LocalTime t) {
        return t.format(LABEL);
    }

    public static LocalTime parseTime(String text) {
        return LocalTime.parse(text.trim(), HM);
    }

    public int seatsLeft() {
        return seats - passengers.size();
    }

    public boolean isFull() {
        return seatsLeft() <= 0;
    }

    public void addPassenger(String name, LocalTime at) {
        passengers.add(name + "~" + hm(at));
    }

    public boolean removePassenger(String name) {
        for (String p : passengers) {
            if (p.startsWith(name + "~")) {
                return passengers.remove(p);
            }
        }
        return false;
    }

    public boolean hasPassenger(String name) {
        for (String p : passengers) {
            if (p.startsWith(name + "~")) {
                return true;
            }
        }
        return false;
    }

    public List<String> passengerNames() {
        List<String> names = new ArrayList<>();
        for (String p : passengers) {
            names.add(p.substring(0, p.indexOf('~')));
        }
        return names;
    }

    public LocalTime bookedAt(String name) {
        for (String p : passengers) {
            if (p.startsWith(name + "~")) {
                return parseTime(p.substring(p.indexOf('~') + 1));
            }
        }
        return null;
    }

    public int seatNumber(String name) {
        List<String> names = passengerNames();
        return names.indexOf(name) + 1;
    }

    public String routeLabel() {
        return Places.name(from) + " to " + Places.name(to);
    }

    public String seatLabel() {
        int left = seatsLeft();
        if (left <= 0) {
            return "Full";
        }
        return left == 1 ? "1 seat left" : left + " seats left";
    }

    public String encode() {
        return id + "," + driver + "," + from + "," + to + "," + hm(time) + "," + vehicle.name() + "," + seats + ","
                + String.join("+", passengers) + "," + note + "," + hm(postedAt);
    }

    public static Offer decode(String text) {
        String[] f = text.split(",", -1);
        Offer o = new Offer(Integer.parseInt(f[0]), f[1], f[2], f[3], parseTime(f[4]), Vehicle.from(f[5]),
                Integer.parseInt(f[6]), f[8], parseTime(f[9]));
        if (!f[7].isEmpty()) {
            for (String p : f[7].split("\\+")) {
                o.passengers.add(p);
            }
        }
        return o;
    }

    public static String encodeList(List<Offer> offers) {
        List<String> parts = new ArrayList<>();
        for (Offer o : offers) {
            parts.add(o.encode());
        }
        return String.join(";", parts);
    }

    public static List<Offer> decodeList(String text) {
        List<Offer> list = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return list;
        }
        for (String part : text.split(";")) {
            try {
                list.add(decode(part));
            } catch (RuntimeException ignored) {
            }
        }
        return list;
    }
}
