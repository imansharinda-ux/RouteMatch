package common;

import java.time.Duration;
import java.time.LocalTime;

public class Request {

    public final String rider;
    public final String from;
    public final String to;
    public final LocalTime time;
    public final int window;

    public Request(String rider, String from, String to, LocalTime time, int window) {
        this.rider = rider;
        this.from = from;
        this.to = to;
        this.time = time;
        this.window = window;
    }

    public long diffMinutes(Offer offer) {
        return Duration.between(time, offer.time).toMinutes();
    }

    public boolean sameRoute(Offer offer) {
        return offer.from.equals(from) && offer.to.equals(to);
    }

    public boolean matches(Offer offer) {
        return sameRoute(offer) && !offer.driver.equals(rider) && Math.abs(diffMinutes(offer)) <= window;
    }

    public String label() {
        return Places.name(from) + " to " + Places.name(to) + " at " + Offer.fmt(time) + " (\u00B1" + window + " min)";
    }
}
