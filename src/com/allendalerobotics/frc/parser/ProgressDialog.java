package com.allendalerobotics.frc.parser;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Kyle Flynn on 4/18/2017.
 */
public class ProgressDialog implements Initializable {

    @FXML ProgressBar progressBar;
    @FXML ProgressIndicator progressIndicator;
    @FXML Label progressLabel;

    public void bindProgressProperty(ReadOnlyDoubleProperty progressProperty) {
        progressBar.progressProperty().bind(progressProperty);
        progressIndicator.progressProperty().bind(progressProperty);
    }

    public void bindMessageProperty(ReadOnlyStringProperty messageProperty) {
        progressLabel.textProperty().bind(messageProperty);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressBar.setProgress(0.0);
        progressIndicator.setProgress(0.0);
        progressLabel.setText("Loading...");
    }

}
