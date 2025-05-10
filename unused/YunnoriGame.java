package unused;

/*
 * import java.util.InputMismatchException;
 * import java.util.Scanner;
 */
/*
 * public class YunnoriGame {
 * 
 * public static void main(String[] args) {
 * Scanner scanner = new Scanner(System.in); // Create scanner here
 * 
 * int numTeams = 0;
 * while (numTeams < 2 || numTeams > 4) {
 * System.out.print("Enter number of teams (2-4): ");
 * try {
 * numTeams = scanner.nextInt();
 * } catch (InputMismatchException e) {
 * System.out.println("Invalid input. Please enter a number.");
 * // Do nothing, loop continues
 * } finally {
 * scanner.nextLine(); // consume the rest of the line
 * }
 * }
 * 
 * int numPieces = 0;
 * while (numPieces < 2 || numPieces > 5) {
 * System.out.print("Enter number of pieces per team (2-5): ");
 * try {
 * numPieces = scanner.nextInt();
 * } catch (InputMismatchException e) {
 * System.out.println("Invalid input. Please enter a number.");
 * // Do nothing, loop continues
 * } finally {
 * scanner.nextLine(); // consume the rest of the line
 * }
 * }
 * 
 * // Set the game mode
 * boolean isTest = false;
 * System.out.print("Enable test mode (manual roll)? (y/n): ");
 * String option = scanner.nextLine().trim().toLowerCase();
 * if (option.equals("y")) {
 * isTest = true;
 * }
 * 
 * // Start the game
 * Game game = new Game(numTeams, numPieces, isTest, scanner); // Pass scanner
 * game.startGame(); // Call startGame
 * 
 * // scanner.close(); // Close scanner when done
 * }
 * }
 */