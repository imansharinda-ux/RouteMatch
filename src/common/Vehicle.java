package common;

public enum Vehicle {
    CAR("Car", 4),
    TUK("Tuk-tuk", 3),
    BIKE("Bike", 1),
    VAN("Van", 8);

    public final String label;
    public final int maxSeats;

    Vehicle(String label, int maxSeats) {
        this.label = label;
        this.maxSeats = maxSeats;
    }

    public static Vehicle from(String text) {
        for (Vehicle v : values()) {
            if (v.name().equalsIgnoreCase(text)) {
                return v;
            }
        }
        return CAR;
    }
}
