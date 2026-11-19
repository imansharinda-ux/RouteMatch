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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    public static final int MAX_CLIENTS = 12;

    private final int port;
    private final String password;
    private final ServerLog log = new ServerLog("server.log");
    private final MatchEngine engine = new MatchEngine(log);
    private final ThreadPoolExecutor pool;
    private final LocalTime startedAt = LocalTime.now();
    private final String address = localAddress();

    public Server(int port, String password) {
        this.port = port;
        this.password = password;
        AtomicInteger count = new AtomicInteger();
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "client-thread-" + count.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_CLIENTS, factory);
    }

    public static void main(String[] args) {
        Theme.install();
        Properties config = loadConfig();
        int port = Protocol.DEFAULT_PORT;
        try {
            port = Integer.parseInt(config.getProperty("server.port", String.valueOf(Protocol.DEFAULT_PORT)).trim());
        } catch (NumberFormatException ignored) {
        }
        Server server = new Server(port, config.getProperty("admin.password"));
        SwingUtilities.invokeLater(() -> new ServerDashboard(server).setVisible(true));
        server.listen();
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("config.properties")) {
            p.load(in);
        } catch (IOException ignored) {
        }
        return p;
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.log("SERVER", "RouteMatch server started on " + address + ":" + port);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.execute(new ClientHandler(socket, engine, log));
            }
        } catch (IOException e) {
            log.log("ERROR", "Server stopped: " + e.getMessage());
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

    public ThreadPoolExecutor pool() {
        return pool;
    }

    public LocalTime startedAt() {
        return startedAt;
    }

    public String address() {
        return address;
    }
}
