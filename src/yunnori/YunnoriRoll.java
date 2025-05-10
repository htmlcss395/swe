package src.yunnori;

public enum YunnoriRoll {
    DO(1), GAE(2), GEOL(3), YUT(4), MO(5), BACK_DO(-1);

    private final int steps;

    YunnoriRoll(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    // Helper to get roll by name (case-insensitive)
    public static YunnoriRoll fromName(String name) {
        for (YunnoriRoll roll : values()) {
            if (roll.name().equalsIgnoreCase(name)) {
                return roll;
            }
        }
        return null; // Or throw an exception or return a default
    }

    // Helper to get roll by step count (for testing/debugging)
    public static YunnoriRoll fromSteps(int steps) {
        for (YunnoriRoll roll : values()) {
            if (roll.getSteps() == steps) {
                return roll;
            }
        }
        return null; // Should not happen for valid steps
    }
}
