package client;

import common.Protocol;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection extends Thread {

    private final String host;
    private final int port;
    private final String username;
    private final ClientApp app;
    private Socket socket;
    private BufferedReader in;
    private volatile PrintWriter out;
    private volatile boolean closed;

    public Connection(String host, int port, String username, ClientApp app) {
        super("server-listener");
        this.host = host;
        this.port = port;
        this.username = username;
        this.app = app;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            InetAddress ipAddress = InetAddress.getByName(host);
            socket = new Socket(ipAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> app.connectFailed(host, port));
            return;
        }
        send(Protocol.LOGIN, username);
        try {
            while (true) {
                String input = in.readLine();
                if (input == null) {
                    break;
                }
                SwingUtilities.invokeLater(() -> app.handle(input));
            }
        } catch (IOException ignored) {
        } finally {
            closeSocket();
            if (!closed) {
                SwingUtilities.invokeLater(app::closed);
            }
        }
    }

    public synchronized void send(String command, Object... parts) {
        if (out != null) {
            out.println(Protocol.build(command, parts));
        }
    }

    public void close() {
        closed = true;
        closeSocket();
    }

    private void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }
    }
}
