package server;

import common.Protocol;
import ui.Theme;

import javax.swing.SwingUtilities;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private final int port;
    private final String password;
    private final ServerLog log = new ServerLog("server.log");
    private final MatchEngine engine = new MatchEngine(log);
    private final List<ClientHandler> handlers = new CopyOnWriteArrayList<>();
    private final LocalTime startedAt = LocalTime.now();
    private final String address = localAddress();

    public Server(int port, String password) {
        this.port = port;
        this.password = password;
    }

    public static void main(String[] args) throws IOException {
        Theme.install();
        Properties config = loadConfig();
        int port = Protocol.DEFAULT_PORT;
        try {
            port = Integer.parseInt(config.getProperty("server.port", String.valueOf(Protocol.DEFAULT_PORT)).trim());
        } catch (NumberFormatException ignored) {
        }
        Server server = new Server(port, config.getProperty("admin.password"));
        SwingUtilities.invokeLater(() -> new ServerDashboard(server).setVisible(true));
        try {
            server.listen();
        } catch (IOException e) {
            server.log.log("ERROR", "Server stopped: " + e.getMessage());
        }
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(in);
        } catch (IOException ignored) {
        }
        return p;
    }

    private void listen() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        log.log("SERVER", "RouteMatch server started on " + address + ":" + port);
        int currentClients = 1;
        try {
            while (true) {
                Socket client = serverSocket.accept();
                try {
                    ClientHandler ct = new ClientHandler(client, currentClients, engine, log);
                    handlers.add(ct);
                    ct.start();
                    currentClients++;
                } catch (IOException e) {
                    log.log("ERROR", "Could not start a thread for " + client.getInetAddress().getHostAddress());
                    client.close();
                }
            }
        } finally {
            serverSocket.close();
        }
    }

    private static String localAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (a instanceof Inet4Address && a.isSiteLocalAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return "127.0.0.1";
    }

    public int port() {
        return port;
    }

    public String password() {
        return password;
    }

    public ServerLog log() {
        return log;
    }

    public MatchEngine engine() {
        return engine;
    }

    public int runningThreads() {
        handlers.removeIf(h -> !h.isAlive());
        return handlers.size();
    }

    public LocalTime startedAt() {
        return startedAt;
    }

    public String address() {
        return address;
    }
}
