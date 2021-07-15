package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("A5 algorithm");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.onCloseRequestProperty().set(event -> {
            String[] ObjButtons = {"Yes", "No"};
            int PromptResult = JOptionPane.showOptionDialog(null, "Are you sure you want to exit?",
                    "A5 algorithm", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, ObjButtons, ObjButtons[1]);
            if (PromptResult == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
