package server;

import common.Offer;
import common.Protocol;
import common.Request;
import common.Vehicle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;

public class ClientHandler extends Thread {

    private final Socket client;
    private final BufferedReader in;
    private final PrintWriter out;
    private final int clientId;
    private final MatchEngine engine;
    private final ServerLog log;
    private final String ip;
    private final LocalTime since = LocalTime.now();
    private String name;

    public ClientHandler(Socket clientSocket, int cid, MatchEngine engine, ServerLog log) throws IOException {
        super("client-thread-" + cid);
        this.client = clientSocket;
        this.clientId = cid;
        this.engine = engine;
        this.log = log;
        this.ip = client.getInetAddress().getHostAddress();
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        setDaemon(true);
    }

    @Override
    public void run() {
        log.log("CONNECT", "Client " + clientId + " connected from " + ip + " (" + getName() + ")");
        try {
            while (true) {
                String input = in.readLine();
                if (input == null || !handle(input)) {
                    break;
                }
            }
        } catch (IOException e) {
            log.log("ERROR", ip + " " + e.getMessage());
        } finally {
            engine.logout(name);
            try {
                client.close();
                out.close();
                in.close();
            } catch (IOException e) {
                log.log("ERROR", "Could not close client " + clientId + ": " + e.getMessage());
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
        synchronized (out) {
            out.println(message);
        }
    }

    public String name() {
        return name == null ? "-" : name;
    }

    public String ip() {
        return ip;
    }

    public String threadName() {
        return getName();
    }

    public LocalTime since() {
        return since;
    }
}
