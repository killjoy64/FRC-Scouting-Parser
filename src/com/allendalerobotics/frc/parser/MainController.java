package com.allendalerobotics.frc.parser;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import javax.json.Json;
import javax.json.stream.JsonParser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created by Kyle Flynn on 4/10/2017.
 */
public class MainController implements Initializable {

    private ArrayList<String> teams;
    private ArrayList<String> usedFiles;

    @FXML
    private TextField textScoutingPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.teams = new ArrayList<>();
        this.usedFiles = new ArrayList<>();
    }

    @FXML
    private void openScoutingFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Resource File");
        File file = directoryChooser.showDialog(MainGUI.getMainStage());

        if (file.isDirectory() && file != null) {
            this.textScoutingPath.setText(file.getAbsolutePath());
        } else {
            System.out.println("Error in reading directory.");
        }
    }

    @FXML
    private void runParser() {
        if (this.textScoutingPath != null && this.textScoutingPath.getText().length() > 0) {
            File dir = new File(this.textScoutingPath.getText());
            File[] jsonFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".json");
                }
            });
            boolean teamNumber = false;
            for (int i = 0; i < jsonFiles.length; i++) {
                try {
                    File jsonFile = jsonFiles[i];
                    BufferedReader reader = new BufferedReader(new FileReader(jsonFile.getAbsoluteFile()));
                    JsonParser parser = Json.createParser(reader);
                    while (parser.hasNext()) {
                        JsonParser.Event e = parser.next();
                        if (e == JsonParser.Event.KEY_NAME) {
                            if (parser.getString().equalsIgnoreCase("team_number") && !this.teams.contains(parser.getString())) {
                                teamNumber = true;
                            }
                        }
                        if (e == JsonParser.Event.VALUE_STRING) {
                            if (teamNumber) {
                                String number = parser.getString();
                                teamNumber = false;
                                this.teams.add(number);
                                File newCSVFile = new File(dir.getAbsolutePath() + "\\" + number + ".csv");
                                boolean firstFile = true;
                                for (int j = i; j < jsonFiles.length; j++) {
                                    File newFile = jsonFiles[j];
                                    String fileNumber = newFile.getName().split("-")[2].replace(".json", "");
                                    BufferedWriter newWriter = new BufferedWriter(new FileWriter(newCSVFile, true));
                                    if (number.equalsIgnoreCase(fileNumber) && !this.usedFiles.contains(newFile.getName())) {
                                        this.usedFiles.add(newFile.getName());
                                        BufferedReader newReader = new BufferedReader(new FileReader(newFile.getAbsoluteFile()));
                                        JsonParser newParser = Json.createParser(newReader);
                                        String firstLine = "";
                                        String line = "";
                                        while (newParser.hasNext()) {
                                            JsonParser.Event newE = newParser.next();
                                            if (newE == JsonParser.Event.KEY_NAME) {
                                                if (firstFile && !newParser.getString().equalsIgnoreCase("general") && !newParser.getString().equalsIgnoreCase("autonomous") && !newParser.getString().equalsIgnoreCase("teleop")) {
                                                    firstLine += newParser.getString() + ",";
                                                }
                                            }
                                            if (newE == JsonParser.Event.VALUE_TRUE) {
                                                line += "true,";
                                            }
                                            if (newE == JsonParser.Event.VALUE_FALSE) {
                                                line += "false,";
                                            }
                                            if (newE == JsonParser.Event.VALUE_NULL) {
                                                line += "null,";
                                            }
                                            if (newE == JsonParser.Event.VALUE_NUMBER) {
                                                line += newParser.getInt() + ",";
                                            }
                                            if (newE == JsonParser.Event.VALUE_STRING) {
                                                line += "\"" + newParser.getString() + "\",";
                                            }
                                        }
                                        if (firstFile) {
                                            firstFile = false;
                                            firstLine = firstLine.substring(0, firstLine.length() - 1);
                                            newWriter.write(firstLine);
                                            newWriter.flush();
                                            newWriter.newLine();
                                            newWriter.flush();
                                        }
                                        line = line.substring(0, line.length() - 1);
                                        newWriter.write(line);
                                        newWriter.flush();
                                        newWriter.newLine();
                                        newWriter.close();
                                        newParser.close();
                                        newReader.close();
                                    }
                                    newWriter.close();
                                }
                            }
                        }
                    }
                    parser.close();
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
