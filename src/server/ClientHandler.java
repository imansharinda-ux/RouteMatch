package server;

import common.Offer;
import common.Protocol;
import common.Request;
import common.Vehicle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MatchEngine engine;
    private final ServerLog log;
    private final String ip;
    private final LocalTime since = LocalTime.now();
    private PrintWriter out;
    private String name;
    private String threadName = "-";

    public ClientHandler(Socket socket, MatchEngine engine, ServerLog log) {
        this.socket = socket;
        this.engine = engine;
        this.log = log;
        this.ip = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        log.log("CONNECT", ip + " connected (" + threadName + ")");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            String line;
            while ((line = in.readLine()) != null) {
                if (!handle(line)) {
                    break;
                }
            }
        } catch (IOException e) {
            log.log("ERROR", ip + " " + e.getMessage());
        } finally {
            engine.logout(name);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            log.log("DISCONNECT", (name == null ? ip : name) + " disconnected");
        }
    }

    private boolean handle(String line) {
        String[] p = Protocol.parse(line);
        String cmd = p[0];
        try {
            if (name == null) {
                if (!cmd.equals(Protocol.LOGIN)) {
                    send(Protocol.build(Protocol.ERROR, "Please log in first"));
                    return true;
                }
                String wanted = Protocol.clean(p.length > 1 ? p[1] : "", 20);
                if (wanted.isEmpty()) {
                    send(Protocol.build(Protocol.ERROR, "Enter a username"));
                    return true;
                }
                if (!engine.login(wanted, this)) {
                    send(Protocol.build(Protocol.ERROR, "The name " + wanted + " is already online"));
                    return true;
                }
                name = wanted;
                send(Protocol.build(Protocol.WELCOME, name));
                send(engine.offersMessage());
                send(engine.statsMessage());
                return true;
            }
            switch (cmd) {
                case Protocol.OFFER: {
                    Offer o = engine.addOffer(name, p[1], p[2], Offer.parseTime(p[3]), Vehicle.from(p[4]),
                            Integer.parseInt(p[5]), Protocol.clean(p.length > 6 ? p[6] : "", 80));
                    send(Protocol.build(Protocol.OFFER_OK, o.id));
                    break;
                }
                case Protocol.FIND: {
                    Request r = new Request(name, p[1], p[2], Offer.parseTime(p[3]), Integer.parseInt(p[4]));
                    List<Offer> list = engine.find(r);
                    send(Protocol.build(Protocol.MATCHES, Offer.encodeList(list)));
                    break;
                }
                case Protocol.BOOK: {
                    int id = Integer.parseInt(p[1]);
                    String error = engine.book(id, name);
                    send(error == null ? Protocol.build(Protocol.BOOK_OK, id) : Protocol.build(Protocol.BOOK_FAIL, id, error));
                    break;
                }
                case Protocol.CANCEL_BOOKING:
                    engine.cancelBooking(Integer.parseInt(p[1]), name);
                    break;
                case Protocol.CANCEL_OFFER:
                    engine.cancelOffer(Integer.parseInt(p[1]), name);
                    break;
                case Protocol.LOGOUT:
                    return false;
                default:
                    send(Protocol.build(Protocol.ERROR, "Unknown command " + cmd));
            }
        } catch (IllegalArgumentException e) {
            send(Protocol.build(Protocol.ERROR, e.getMessage()));
        } catch (RuntimeException e) {
            log.log("ERROR", "Bad message from " + (name == null ? ip : name) + ": " + line);
            send(Protocol.build(Protocol.ERROR, "The server could not read that request"));
        }
        return true;
    }

    public void send(String message) {
        PrintWriter w = out;
        if (w == null) {
            return;
        }
        synchronized (this) {
            w.println(message);
        }
    }

    public String name() {
        return name == null ? "-" : name;
    }

    public String ip() {
        return ip;
    }

    public String threadName() {
        return threadName;
    }

    public LocalTime since() {
        return since;
    }
}
