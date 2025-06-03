module yunnori {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; // Goddamn SwingUI needs this line

    opens yunnori to javafx.fxml;

    exports yunnori;
    exports yunnori.core; // Where GameLogicController and models are
    exports yunnori.swingui; // Where YunnoriGUI is
    exports yunnori.fxui; // Where YunnoriJavaFXView is

}
