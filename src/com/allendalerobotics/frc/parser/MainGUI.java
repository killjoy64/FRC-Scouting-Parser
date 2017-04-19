package com.allendalerobotics.frc.parser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Kyle Flynn on 4/10/2017.
 */
public class MainGUI extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ScoutingParser.fxml"));
            Scene scene = new Scene(root);

            this.mainStage = primaryStage;

            this.mainStage.setScene(scene);
            this.mainStage.setTitle("FRC Scouting Parser v0.0.0");
            this.mainStage.setResizable(false);
            this.mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getMainStage() {
        return MainGUI.mainStage;
    }

}