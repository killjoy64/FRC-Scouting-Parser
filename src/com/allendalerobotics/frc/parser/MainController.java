package com.allendalerobotics.frc.parser;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    private HashMap<String, Integer> teamMatches;
    private ArrayList<String> teams;
    private ArrayList<String> usedFiles;

    private Stage dialogStage;

    private String event;

    @FXML
    private TextField textScoutingPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.teamMatches = new HashMap<>();
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

    private void openProgressDialog(Service service) {

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ProgressDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Scene scene = new Scene(page);

            this.dialogStage = new Stage();
            this.dialogStage.setTitle("Progress");
            this.dialogStage.initStyle(StageStyle.UTILITY);
            this.dialogStage.initModality(Modality.APPLICATION_MODAL);
            this.dialogStage.initOwner(MainGUI.getMainStage());
            this.dialogStage.setScene(scene);
            this.dialogStage.show();

            ProgressDialog controller = (ProgressDialog) loader.getController();
            controller.bindProgressProperty(service.progressProperty());
            controller.bindMessageProperty(service.messageProperty());
            service.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void runParser() {
        if (this.textScoutingPath != null && this.textScoutingPath.getText().length() > 0) {

            Service<Void> service = new Service<Void>() {
                @Override
                public Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        public Void call()throws InterruptedException {
                            updateMessage("Initializing Task");
                            File dir = new File(textScoutingPath.getText());
                            File[] jsonFiles = dir.listFiles(new FilenameFilter() {
                                public boolean accept(File dir, String name) {
                                    return name.toLowerCase().endsWith(".json");
                                }
                            });

                            updateProgress(0, jsonFiles.length);

                            boolean teamNumber = false;
                            for (int i = 0; i < jsonFiles.length; i++) {
                                updateMessage("Loading file " + jsonFiles[i].getName());
                                updateProgress(i, jsonFiles.length);
                                try {
                                    File jsonFile = jsonFiles[i];
                                    BufferedReader reader = new BufferedReader(new FileReader(jsonFile.getAbsoluteFile()));
                                    JsonParser parser = Json.createParser(reader);

                                    event = jsonFile.getName().split("-")[0];

                                    while (parser.hasNext()) {
                                        JsonParser.Event e = parser.next();
                                        if (e == JsonParser.Event.KEY_NAME) {
                                            if (parser.getString().equalsIgnoreCase("team_number") && !teams.contains(parser.getString())) {
                                                teamNumber = true;
                                            }
                                        }
                                        if (e == JsonParser.Event.VALUE_STRING) {
                                            if (teamNumber) {
                                                String number = parser.getString();
                                                teamNumber = false;
                                                File newCSVFile = new File(dir.getAbsolutePath() + "\\" + number + ".csv");
                                                boolean firstFile = true;
                                                int matches = 0;
                                                for (int j = i; j < jsonFiles.length; j++) {
                                                    File newFile = jsonFiles[j];
                                                    String[] fileParams = newFile.getName().split("-");
                                                    String fileNumber = fileParams[2].replace(".json", "");
                                                    BufferedWriter newWriter = new BufferedWriter(new FileWriter(newCSVFile, true));
                                                    if (number.equalsIgnoreCase(fileNumber) && !usedFiles.contains(newFile.getName())) {
                                                        if (!teams.contains(number)) {
                                                            teams.add(number);
                                                        }
                                                        updateMessage("Parsing file " + jsonFiles[j].getName());
                                                        matches++;
                                                        teamMatches.put(number, matches);
                                                        usedFiles.add(newFile.getName());
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
                                                                if (newParser.getString().contains("\n")) {
                                                                    line += "\"" + newParser.getString().replace("\n", "") + "\",";
                                                                } else {
                                                                    line += "\"" + newParser.getString() + "\",";
                                                                }
                                                            }
                                                        }
                                                        if (firstFile) {
                                                            FileWriter fileOut = new FileWriter(newCSVFile);
                                                            fileOut.write("");
                                                            fileOut.close();

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

                            for (String team : teamMatches.keySet()) {
//                                System.out.println("Team: " + team + " Matches Played: " + teamMatches.get(team));
                            }

                            updateMessage("Done Running Parser.");
                            updateProgress(jsonFiles.length, jsonFiles.length);

                            ArrayList<Integer> avgIndices = new ArrayList<>();
                            File avgCSVFile = new File(dir.getAbsolutePath() + "\\" + event + ".csv");
                            boolean firstFile = true;

                            try {
                                FileWriter avgFileOut = new FileWriter(avgCSVFile);
                                avgFileOut.write("");
                                avgFileOut.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            for (String team : teams) {
                                try {
                                    File csvFile = new File(dir.getAbsolutePath() + "\\" + team + ".csv");
                                    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                                    ArrayList<String> lines = new ArrayList<>();
                                    BufferedWriter newWriter = new BufferedWriter(new FileWriter(avgCSVFile, true));
                                    String line;

                                    while ((line = reader.readLine()) != null) {
                                        lines.add(line);
                                    }

                                    for (int i = 1; i < lines.size(); i++) {
                                        line = "";

                                        if (firstFile) {
                                            String[] csv = lines.get(0).split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
                                            firstFile = false;
                                            for (int j = 0; j < csv.length; j++) {
                                                if (!csv[j].equals("name") && !csv[j].equals("comments") && !csv[j].equals("match_number") && csv.length > 2) {
                                                    line += csv[j] + ",";
                                                    avgIndices.add(j);
                                                }
                                            }

                                            line = line.substring(0, line.length() - 1);
                                            newWriter.write(line);
                                            newWriter.flush();
                                            newWriter.newLine();
                                            newWriter.flush();
                                            line = "";

                                            csv = lines.get(i).split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
                                            for (int j = 0; j < csv.length; j++) {
                                                if (avgIndices.contains(j) && csv.length > 0) {
                                                    line += csv[j] + ",";
                                                }
                                            }
                                        } else {
                                            String[] csv = lines.get(i).split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
                                            for (int j = 0; j < csv.length; j++) {
                                                if (avgIndices.contains(j) && csv.length > 0) {
                                                     line += csv[j] + ",";
                                                }
                                            }
                                        }

                                        if (line.length() > 0) {
                                            line = line.substring(0, line.length() - 1);
                                            newWriter.write(line);
                                            newWriter.flush();
                                            newWriter.newLine();
                                            newWriter.flush();
                                        }

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            return null;
                        }
                    };
                }
            };

            this.openProgressDialog(service);

            service.setOnSucceeded((event) -> {
                this.dialogStage.close();
            });

        }
    }

}
