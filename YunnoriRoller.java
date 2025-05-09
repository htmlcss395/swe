import java.util.Random;
import java.util.Scanner;
import java.util.InputMismatchException;

public class YunnoriRoller {
    private boolean isTestMode;
    private Scanner scanner;
    private Random random;

    public YunnoriRoller(boolean isTestMode, Scanner scanner) {
        this.isTestMode = isTestMode;
        this.scanner = scanner;
        this.random = new Random();
    }

    public YunnoriRoll roll() {
        if (isTestMode) {
            return rollTest();
        } else {
            return rollReal();
        }
    }

    private YunnoriRoll rollReal() {
        // Simulate rolling 4 sticks. Let's say 1 is 'round' and 0 is 'flat'.
        // Back-Do occurs if exactly one stick is flat (0) AND that stick is the
        // 'marked' one.
        // Let's simplify: roll 4 sticks (0 or 1). If the count is 1, roll a 5th virtual
        // stick (0 or 1) to be the marked one.
        // If count is 1 AND virtual 5th is 0 (flat), it's Back-Do. Otherwise it's Do.

        int flats = 0; // Count of sticks landing flat (0)
        for (int i = 0; i < 4; i++) {
            if (random.nextBoolean()) { // nextBoolean() is true ~50% -> flat
                flats++;
            }
        }

        // Map flat count to YunnoriRoll
        switch (flats) {
            case 0:
                return YunnoriRoll.MO; // 4 rounds
            case 1:
                // Check if the single flat stick is the 'marked' one
                // Simplified simulation: 1/4 chance it's the marked one AND it landed flat.
                // A standard yut set has one stick marked on one side.
                // Back Do is when ONE stick is flat and it's the marked stick.
                // Total 4 sticks: F R R R. Is F the marked one? 1/4 chance.
                // R F R R. Is F the marked one? 1/4 chance.
                // R R F R. Is F the marked one? 1/4 chance.
                // R R R F. Is F the marked one? 1/4 chance.
                // So if there's exactly one flat, Back Do happens with probability 1/4
                // (assuming marked side is flat).
                // If marked side is ROUND, Back Do is when one is ROUND and it's the marked
                // one.
                // Let's assume marked side is FLAT.
                if (random.nextInt(4) == 0) { // 1 in 4 chance the single flat is the marked one
                    return YunnoriRoll.BACK_DO;
                } else {
                    return YunnoriRoll.DO;
                }
            case 2:
                return YunnoriRoll.GAE;
            case 3:
                return YunnoriRoll.GEOL;
            case 4:
                return YunnoriRoll.YUT; // 4 flats
            default: // Should not happen with 4 sticks
                throw new IllegalStateException("Invalid number of flats rolled: " + flats);
        }
    }

    private YunnoriRoll rollTest() {
        YunnoriRoll roll = null;
        while (roll == null) {
            System.out.print("Enter roll (Do, Gae, Geol, Yut, Mo, Back_Do): ");
            String input = scanner.nextLine().trim();
            roll = YunnoriRoll.fromName(input);
            if (roll == null) {
                System.out.println("Invalid roll name. Please enter one of: Do, Gae, Geol, Yut, Mo, Back_Do");
            }
        }
        return roll;
    }
}