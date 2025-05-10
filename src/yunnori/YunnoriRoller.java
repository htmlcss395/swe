package src.yunnori;

import java.util.Random;

public class YunnoriRoller {
    private boolean isTestMode;
    private Random random;
    private YunnoriRoll testRoll = null;

    public YunnoriRoller(boolean isTestMode) {
        this.isTestMode = isTestMode;
        this.random = new Random();
    }

    public void setTestRoll(YunnoriRoll roll) {
        this.testRoll = roll;
    }

    public YunnoriRoll roll() {
        if (isTestMode) {
            return rollTest();
        } else {
            return rollReal();
        }
    }

    private YunnoriRoll rollReal() {
        int flats = 0;
        for (int i = 0; i < 4; i++) {
            if (random.nextBoolean()) {
                flats++;
            }
        }
        switch (flats) {
            case 0:
                return YunnoriRoll.MO;
            case 1:
                if (random.nextInt(4) == 0) {
                    return YunnoriRoll.BACK_DO;
                } else {
                    return YunnoriRoll.DO;
                }
            case 2:
                return YunnoriRoll.GAE;
            case 3:
                return YunnoriRoll.GEOL;
            case 4:
                return YunnoriRoll.YUT;
            default:
                throw new IllegalStateException("Invalid number of flats rolled: " + flats);
        }
    }

    private YunnoriRoll rollTest() {
        YunnoriRoll result = this.testRoll;
        this.testRoll = null;
        if (result == null) {
            System.err.println("Error: rollTest called but test roll was not set!");
            return YunnoriRoll.DO;
        }
        return result;
    }
}