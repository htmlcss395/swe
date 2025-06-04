package yunnori;

import yunnori.core.BoardType; // For the setup dialogs
import yunnori.swingui.YunnoriSwingView;
import yunnori.fxui.YunnoriFXView;
import javafx.application.Application; // For launching JavaFX
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.awt.Font;

public class Launcher {

    public static void main(String[] args) {
        // Optional: Set UIManager properties for the initial Swing dialog
        // This ensures the UI selection dialog itself looks consistent if scaled.
        try {
            int dialogFontSize = 18; // Consistent with your YunnoriGUI main
            Font dialogFont = new Font("SansSerif", Font.PLAIN, dialogFontSize);
            Font dialogButtonFont = new Font("SansSerif", Font.BOLD, dialogFontSize - 2);
            UIManager.put("OptionPane.messageFont", dialogFont);
            UIManager.put("OptionPane.buttonFont", dialogButtonFont);
            UIManager.put("Label.font", dialogFont);
        } catch (Exception e) {
            System.err.println("Could not set UIManager for initial dialog: " + e.getMessage());
        }

        Object[] options = { "Swing UI", "JavaFX UI" };
        int choice = JOptionPane.showOptionDialog(null,
                "Choose your User Interface:",
                "Yunnori Game Launcher",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        // Game Setup Dialogs (common for both UIs)
        // These are the Swing dialogs from YunnoriGUI's main method
        BoardType boardType = BoardType.RECTANGLE; // Default
        int numTeams = 2; // Default
        int numPieces = 2; // Default
        boolean isTestMode = false; // Default
        boolean setupComplete = false;

        try {
            String[] boardTypes = { "Rectangle", "Pentagon", "Hexagon" };
            int boardTypeIndex = JOptionPane.showOptionDialog(null, "Select the type of Yutnori board:",
                    "Board Type", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, boardTypes,
                    boardTypes[0]);
            if (boardTypeIndex == JOptionPane.CLOSED_OPTION)
                System.exit(0);
            boardType = switch (boardTypeIndex) {
                case 1 -> BoardType.PENTAGON;
                case 2 -> BoardType.HEXAGON;
                default -> BoardType.RECTANGLE;
            };

            String teamsInput = JOptionPane.showInputDialog(null, "Enter number of teams (2-4):", "Game Setup",
                    JOptionPane.QUESTION_MESSAGE);
            if (teamsInput == null)
                System.exit(0);
            numTeams = Integer.parseInt(teamsInput);
            if (numTeams < 2 || numTeams > 4)
                throw new IllegalArgumentException("Invalid number of teams (2-4)");

            String piecesInput = JOptionPane.showInputDialog(null, "Enter number of pieces per team (2-5):",
                    "Game Setup", JOptionPane.QUESTION_MESSAGE);
            if (piecesInput == null)
                System.exit(0);
            numPieces = Integer.parseInt(piecesInput);
            if (numPieces < 2 || numPieces > 5)
                throw new IllegalArgumentException("Invalid number of pieces per team (2-5)");

            int testModeOption = JOptionPane.showConfirmDialog(null, "Enable test mode (manual roll)?", "Game Setup",
                    JOptionPane.YES_NO_OPTION);
            if (testModeOption == JOptionPane.CLOSED_OPTION)
                System.exit(0);
            isTestMode = (testModeOption == JOptionPane.YES_OPTION);
            setupComplete = true;
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Invalid input:\n" + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) { // Catch other potential errors like NumberFormatException
            JOptionPane.showMessageDialog(null, "Error during setup:\n" + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (!setupComplete) {
            System.out.println("Game setup was not completed.");
            System.exit(0);
        }

        // --- Store final values to pass to the chosen UI ---
        final BoardType finalBoardType = boardType;
        final int finalNumTeams = numTeams;
        final int finalNumPieces = numPieces;
        final boolean finalIsTestMode = isTestMode;

        if (choice == 0) { // Swing UI
            // YunnoriSwingView's main method used SwingUtilities.invokeLater, so we do too.
            javax.swing.SwingUtilities.invokeLater(() -> {
                new YunnoriSwingView(finalNumTeams, finalNumPieces, finalIsTestMode, finalBoardType);
            });
        } else { // JavaFX UI
            // Pass parameters to JavaFX application
            YunnoriFXView.setGameParameters(finalNumTeams, finalNumPieces, finalIsTestMode, finalBoardType);
            Application.launch(YunnoriFXView.class, args);
        }
    }
}