package org.example.editor;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.Alert;

public class HelloApplication extends Application {

    private static File currentFile;
    private String compilerPath = null;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        Pane canvas = new Pane(); // edytor + konsola

        // ========== Terminal w TabPane ==========
        TerminalBuilder terminalBuilder = new TerminalBuilder();
        TerminalTab terminal = terminalBuilder.newTerminal();
        TabPane tabPane = new TabPane(terminal);
        tabPane.setPrefHeight(200);

        // ========== SplitPane pionowy: kod + terminal ==========
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(canvas, tabPane);
        splitPane.setDividerPositions(0.75); // 75% kod, 25% terminal
        root.setCenter(splitPane);

        // ========== Komponenty ==========
        Component codeEditor    = new Component();
        Component consoleView   = new Component();
        SettingsBar settingsBar = new SettingsBar(codeEditor, consoleView);

        OpenFilesPanel openFilesPanel = new OpenFilesPanel(selectedFile -> {
            try {
                String content = java.nio.file.Files.readString(selectedFile.toPath());
                codeEditor.setCode(content);
                HelloApplication.setCurrentFile(selectedFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to load file: " + ex.getMessage()).showAndWait();
            }
        });
        root.setLeft(openFilesPanel);

        // ========== Toolbar ==========
        Button settingsBtn = new Button("Compiler…");
        settingsBtn.setOnAction(e -> {
            CompilerSettingsDialog dialog = new CompilerSettingsDialog(stage);
            dialog.showAndWait();
        });

        Button runBtn      = new RunButton(stage, codeEditor, consoleView, CompilerSettingsDialog.getSavedCompilerPath());
        OpenButton openBtn = new OpenButton(stage, codeEditor, openFilesPanel);
        Button saveBtn     = new SaveButton(stage, codeEditor);
        SaveAsButton saveAsBtn = new SaveAsButton(stage, codeEditor, openFilesPanel);
        OpenFolderButton openFolderBtn = new OpenFolderButton(stage, codeEditor, openFilesPanel);

        HBox toolBar = new HBox(6, openBtn, openFolderBtn, saveBtn, saveAsBtn, settingsBtn, runBtn, settingsBar);
        toolBar.setStyle("-fx-padding:8; -fx-background-color:#2b2b2b;");
        root.setTop(toolBar);

        // ========== Wczytywanie pozycji ==========
        String editorKey  = "codeEditor";
        String consoleKey = "consoleView";
        LayoutPersistence.load(codeEditor, editorKey);
        LayoutPersistence.load(consoleView, consoleKey);

        if (Double.isNaN(codeEditor.getLayoutX())) {
            codeEditor.setLayoutX(50);
            codeEditor.setLayoutY(50);
            codeEditor.setPrefSize(600, 400);
        }
        if (Double.isNaN(consoleView.getLayoutX())) {
            consoleView.setLayoutX(50);
            consoleView.setLayoutY(470);
            consoleView.setPrefSize(600, 150);
        }

        canvas.getChildren().addAll(codeEditor, consoleView);

        stage.setOnCloseRequest(evt -> {
            LayoutPersistence.save(codeEditor, editorKey);
            LayoutPersistence.save(consoleView, consoleKey);
        });

        // ========== Start ==========
        Scene scene = new Scene(root, 800, 650);
        stage.setScene(scene);
        stage.setTitle("Code Editor");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void setCurrentFile(File selectedFile) {
        currentFile = selectedFile;
    }

    public static File getCurrentFile() {
        return currentFile;
    }
}
