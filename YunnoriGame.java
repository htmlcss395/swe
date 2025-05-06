import java.util.Scanner;

public class YunnoriGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get number of teams and pieces
        System.out.print("Enter number of teams (2-4): ");
        int numTeams = scanner.nextInt();
        System.out.print("Enter number of pieces per team (2-5): ");
        int numPieces = scanner.nextInt();

        // Set the game mode
        boolean isTest = false;
        System.out.print("The roll can be manually fixed for the test purpose. If you want, enter 'y'.");
        String option = scanner.nextLine();
        if (option.equals('y'))
            isTest = true;

        // Start the game
        Game game = new Game(numTeams, numPieces, isTest);
        game.startGame(scanner);
    }
}