package co.wethinkcode.healthsafe;

import java.util.concurrent.atomic.AtomicInteger;

public final class AlertLevelStore {

    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 8;

    private final AtomicInteger level = new AtomicInteger(MIN_LEVEL);

    public int get() {
        return level.get();
    }

    public void set(int newLevel) {
        if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL) {
            throw new IllegalArgumentException("Alert level must be between 0 and 8");
        }
        level.set(newLevel);
    }
}
