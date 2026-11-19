package common;

public final class Protocol {

    public static final int DEFAULT_PORT = 5000;

    public static final String LOGIN = "LOGIN";
    public static final String WELCOME = "WELCOME";
    public static final String OFFER = "OFFER";
    public static final String OFFER_OK = "OFFER_OK";
    public static final String FIND = "FIND";
    public static final String MATCHES = "MATCHES";
    public static final String BOOK = "BOOK";
    public static final String BOOK_OK = "BOOK_OK";
    public static final String BOOK_FAIL = "BOOK_FAIL";
    public static final String CANCEL_BOOKING = "CANCEL_BOOKING";
    public static final String CANCEL_OFFER = "CANCEL_OFFER";
    public static final String OFFERS = "OFFERS";
    public static final String STATS = "STATS";
    public static final String NOTIFY = "NOTIFY";
    public static final String ERROR = "ERROR";
    public static final String LOGOUT = "LOGOUT";

    public static final String N_BOOKED = "BOOKED";
    public static final String N_CANCELLED = "CANCELLED";
    public static final String N_REMOVED = "REMOVED";
    public static final String N_MATCH = "MATCH";
    public static final String N_INFO = "INFO";

    private Protocol() {
    }

    public static String build(String command, Object... parts) {
        StringBuilder sb = new StringBuilder(command);
        for (Object part : parts) {
            sb.append('|').append(part == null ? "" : String.valueOf(part).replace('|', ' ').replace('\n', ' '));
        }
        return sb.toString();
    }

    public static String[] parse(String line) {
        return line.split("\\|", -1);
    }

    public static String clean(String text, int max) {
        if (text == null) {
            return "";
        }
        String s = text.replaceAll("[|;,~+\\r\\n]", " ").replaceAll("\\s+", " ").trim();
        return s.length() > max ? s.substring(0, max).trim() : s;
    }
}
