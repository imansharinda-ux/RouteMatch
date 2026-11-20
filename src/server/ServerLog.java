package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ServerLog {

    public static final class Entry {
        public final long seq;
        public final String time;
        public final String tag;
        public final String message;

        Entry(long seq, String time, String tag, String message) {
            this.seq = seq;
            this.time = time;
            this.tag = tag;
            this.message = message;
        }
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int KEEP = 400;

    private final List<Entry> entries = new ArrayList<>();
    private BufferedWriter writer;
    private long seq;

    public ServerLog(String file) {
        try {
            writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true));
        } catch (IOException e) {
            writer = null;
        }
    }

    public synchronized void log(String tag, String message) {
        String time = LocalTime.now().format(TIME);
        String line = time + " [" + tag + "] " + message;
        System.out.println(line);
        entries.add(new Entry(++seq, time, tag, message));
        if (entries.size() > KEEP) {
            entries.remove(0);
        }
        if (writer != null) {
            try {
                writer.write(line);
                writer.newLine();
                writer.flush();
            } catch (IOException ignored) {
            }
        }
    }

    public synchronized List<Entry> since(long lastSeq) {
        List<Entry> list = new ArrayList<>();
        for (Entry e : entries) {
            if (e.seq > lastSeq) {
                list.add(e);
            }
        }
        return list;
    }
}
