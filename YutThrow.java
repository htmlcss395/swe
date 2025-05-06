import java.util.Random;
import java.util.Scanner;

public class YutThrow {

    // which ways to choose: manual vs random
    public static int[] throwSticks(Scanner scanner, boolean isTest) {
        if (isTest)
            return cheat(scanner);
        else
            return justice();
    }

    // for testing
    private static int[] cheat(Scanner scanner) {
        System.out.println("Enter the Yut sequence (Do=1, Gae=2, Geol=3, Yut=4, Mo=5):");

        int[] rolls = new int[5]; // Max 5 rolls per turn
        int rollCount = 0;

        while (true) {
            System.out.print("Throw " + (rollCount + 1) + ": ");
            int result = scanner.nextInt();

            // Validate the result
            if (result < 1 || result > 5) {
                System.out.println("Invalid input! Please enter a number between 1 and 5.");
                continue; // Skip invalid input
            }

            rolls[rollCount] = result;
            rollCount++;

            // Check if we should stop (not Yut or Mo)
            if (result != 4 && result != 5) {
                break;
            }

            if (rollCount == 5)
                break; // Max of 5 rolls per turn
        }

        // Trim the array to the actual number of rolls
        int[] finalRolls = new int[rollCount];
        System.arraycopy(rolls, 0, finalRolls, 0, rollCount);
        return finalRolls;
    }

    // for real game
    private static int[] justice() {
        Random random = new Random();
        int[] rolls = new int[5];
        int rollCount = random.nextInt(5) + 1; // Random number of rolls (1 to 5)

        System.out.println("Randomly rolled sequence: ");
        for (int i = 0; i < rollCount; i++) {
            rolls[i] = random.nextInt(5) + 1; // Random roll between 1 and 5 (Do=1, Gae=2, Geol=3, Yut=4, Mo=5)
            System.out.print(rolls[i] + " ");
        }
        System.out.println();

        // Trim the array to the actual number of rolls
        int[] finalRolls = new int[rollCount];
        System.arraycopy(rolls, 0, finalRolls, 0, rollCount);
        return finalRolls;
    }

    // Extra turn for Yut & Mo
    public static boolean isExtraTurn(int result) {
        return result == 4 || result == 5;
    }
}