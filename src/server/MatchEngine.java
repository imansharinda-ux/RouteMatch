package server;

import common.Offer;
import common.Places;
import common.Protocol;
import common.Request;
import common.Vehicle;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MatchEngine {

    private final Map<Integer, Offer> offers = new ConcurrentHashMap<>();
    private final Map<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, Request> requests = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final AtomicInteger matches = new AtomicInteger();
    private final ServerLog log;

    public MatchEngine(ServerLog log) {
        this.log = log;
    }

    public boolean login(String name, ClientHandler handler) {
        if (clients.putIfAbsent(name, handler) != null) {
            return false;
        }
        log.log("LOGIN", name + " joined from " + handler.ip() + " on " + handler.threadName());
        broadcastStats();
        return true;
    }

    public void logout(String name) {
        if (name == null || clients.remove(name) == null) {
            return;
        }
        requests.remove(name);
        log.log("LOGOUT", name + " left");
        broadcastStats();
    }

    public Offer addOffer(String driver, String from, String to, LocalTime time, Vehicle vehicle, int seats, String note) {
        if (!Places.exists(from) || !Places.exists(to) || from.equals(to)) {
            throw new IllegalArgumentException("Choose two different places");
        }
        if (seats < 1 || seats > vehicle.maxSeats) {
            throw new IllegalArgumentException("A " + vehicle.label + " can share 1 to " + vehicle.maxSeats + " seats");
        }
        Offer offer = new Offer(nextId.getAndIncrement(), driver, from, to, time, vehicle, seats, note, LocalTime.now());
        offers.put(offer.id, offer);
        locks.put(offer.id, new ReentrantLock(true));
        log.log("OFFER", "#" + offer.id + " " + driver + " " + offer.routeLabel() + " " + Offer.fmt(time) + " " + vehicle.label + " " + seats + " seat(s)");
        for (Request r : requests.values()) {
            if (r.matches(offer)) {
                log.log("MATCH", "Offer #" + offer.id + " matches open request of " + r.rider);
                notifyUser(r.rider, Protocol.N_MATCH, "New ride: " + driver + " is going " + offer.routeLabel() + " at "
                        + Offer.fmt(time) + " (" + vehicle.label + ", " + offer.seatLabel() + ")");
            }
        }
        broadcastOffers();
        return offer;
    }

    public List<Offer> find(Request request) {
        requests.put(request.rider, request);
        List<Offer> list = new ArrayList<>();
        for (Offer o : offers.values()) {
            if (request.matches(o) || (request.sameRoute(o) && o.hasPassenger(request.rider))) {
                list.add(o);
            }
        }
        list.sort(Comparator.comparingLong((Offer o) -> Math.abs(request.diffMinutes(o))).thenComparing(o -> o.time));
        log.log("REQUEST", request.rider + " searched " + request.label() + " -> " + list.size() + " match(es)");
        broadcastStats();
        return list;
    }

    public String book(int id, String rider) {
        ReentrantLock lock = locks.get(id);
        Offer offer = offers.get(id);
        if (lock == null || offer == null) {
            return "This ride is no longer available";
        }
        lock.lock();
        try {
            log.log("LOCK", "Offer #" + id + " locked by " + rider + " (" + Thread.currentThread().getName() + ")");
            if (!offers.containsKey(id)) {
                return "This ride was just cancelled";
            }
            if (offer.driver.equals(rider)) {
                return "You cannot book your own ride";
            }
            if (offer.hasPassenger(rider)) {
                return "You already booked this ride";
            }
            if (offer.isFull()) {
                log.log("BOOK", rider + " was too late for offer #" + id + " (no seats left)");
                return "Sorry, the last seat was just taken";
            }
            offer.addPassenger(rider, LocalTime.now());
            matches.incrementAndGet();
            requests.remove(rider);
            log.log("BOOK", rider + " booked offer #" + id + " (" + offer.seatsLeft() + " seat(s) left)");
        } finally {
            log.log("LOCK", "Offer #" + id + " released by " + rider);
            lock.unlock();
        }
        notifyUser(offer.driver, Protocol.N_BOOKED, rider + " booked a seat on your " + offer.routeLabel() + " ride at " + Offer.fmt(offer.time));
        broadcastOffers();
        return null;
    }

    public void cancelBooking(int id, String rider) {
        ReentrantLock lock = locks.get(id);
        Offer offer = offers.get(id);
        if (lock == null || offer == null) {
            return;
        }
        boolean removed;
        lock.lock();
        try {
            removed = offer.removePassenger(rider);
        } finally {
            lock.unlock();
        }
        if (!removed) {
            return;
        }
        log.log("CANCEL", rider + " cancelled a seat on offer #" + id);
        notifyUser(offer.driver, Protocol.N_CANCELLED, rider + " cancelled their seat on your " + offer.routeLabel() + " ride");
        broadcastOffers();
    }

    public void cancelOffer(int id, String driver) {
        Offer offer = offers.get(id);
        if (offer == null || !offer.driver.equals(driver)) {
            return;
        }
        ReentrantLock lock = locks.get(id);
        lock.lock();
        try {
            offers.remove(id);
        } finally {
            lock.unlock();
            locks.remove(id);
        }
        log.log("CANCEL", driver + " cancelled offer #" + id);
        for (String p : offer.passengerNames()) {
            notifyUser(p, Protocol.N_REMOVED, driver + " cancelled the " + offer.routeLabel() + " ride at " + Offer.fmt(offer.time));
        }
        broadcastOffers();
    }

    public void notifyUser(String name, String kind, String text) {
        ClientHandler h = clients.get(name);
        if (h != null) {
            h.send(Protocol.build(Protocol.NOTIFY, kind, text));
        }
    }

    public List<Offer> sortedOffers() {
        List<Offer> list = new ArrayList<>(offers.values());
        list.sort(Comparator.comparing((Offer o) -> o.time).thenComparingInt(o -> o.id));
        return list;
    }

    public String offersMessage() {
        return Protocol.build(Protocol.OFFERS, Offer.encodeList(sortedOffers()));
    }

    public String statsMessage() {
        return Protocol.build(Protocol.STATS, clients.size(), offers.size(), requests.size(), matches.get());
    }

    public void broadcastOffers() {
        String msg = offersMessage();
        for (ClientHandler h : clients.values()) {
            h.send(msg);
        }
        broadcastStats();
    }

    public void broadcastStats() {
        String msg = statsMessage();
        for (ClientHandler h : clients.values()) {
            h.send(msg);
        }
    }

    public List<ClientHandler> clients() {
        List<ClientHandler> list = new ArrayList<>(clients.values());
        list.sort(Comparator.comparing(ClientHandler::since));
        return list;
    }

    public int onlineCount() {
        return clients.size();
    }

    public int offerCount() {
        return offers.size();
    }

    public int requestCount() {
        return requests.size();
    }

    public int matchCount() {
        return matches.get();
    }
}
