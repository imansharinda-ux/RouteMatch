package client;

import common.Protocol;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class Connection {

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile boolean closed;

    public Connection(String host, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 4000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void listen(Consumer<String> onMessage, Runnable onClosed) {
        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String message = line;
                    SwingUtilities.invokeLater(() -> onMessage.accept(message));
                }
            } catch (IOException ignored) {
            }
            if (!closed) {
                SwingUtilities.invokeLater(onClosed);
            }
        }, "server-listener");
        reader.setDaemon(true);
        reader.start();
    }

    public synchronized void send(String command, Object... parts) {
        out.println(Protocol.build(command, parts));
    }

    public void close() {
        closed = true;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
