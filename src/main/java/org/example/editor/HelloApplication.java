package org.example.editor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.editor.config.DefaultTabNameGenerator;
import org.example.editor.config.TerminalConfig;
import org.example.editor.helper.ThreadHelper;
import org.example.editor.layout_api.EditorLayout;
import org.example.editor.layout_elements.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class HelloApplication extends Application {
    private static File currentFile;
    private String compilerPath = null;

    public static void setCurrentFile(File selectedFile) {
        currentFile = selectedFile;
    }

    public static File getCurrentFile() {
        return currentFile;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Layout edytora
        EditorLayout layout = new EditorLayout();

        // 2. Edytor kodu
        CodeEditor editor = new CodeEditor("code-1");
        editor.getRegion().setLayoutX(100);
        editor.getRegion().setLayoutY(100);
        editor.getRegion().setPrefSize(600, 400);
        layout.addChild(editor);

        editor.getRegion().setStyle(
                "-fx-border-color: #00bfff;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-color: #1e1e1e;"
        );

        // 3. Konsola (wewnątrz EditorLayout)
        ConsoleView consoleView = new ConsoleView("console-1");
        consoleView.getRegion().setLayoutX(100);
        consoleView.getRegion().setLayoutY(520);
        consoleView.getRegion().setPrefSize(600, 150);
        layout.addChild(consoleView);

        // 4. Panel otwartych plików
        OpenFilesPanel openFilesPanel = new OpenFilesPanel(file -> {
            try {
                String content = Files.readString(file.toPath());
                editor.setCode(content);
                HelloApplication.setCurrentFile(file);
            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });
        openFilesPanel.getRegion().setLayoutX(20);
        openFilesPanel.getRegion().setLayoutY(100);
        openFilesPanel.getRegion().setPrefSize(160, 600);
        layout.addChild(openFilesPanel);

        // 5. Terminal jako komponent wewnątrz EditorLayout

        // Dodaj to wcześniej w metodzie start(...)
        TerminalConfig terminalConfig = new TerminalConfig();
        DefaultTabNameGenerator tabNameGenerator = new DefaultTabNameGenerator();

        TerminalTabPaneComponent terminalTabPane = new TerminalTabPaneComponent(
                terminalConfig,
                tabNameGenerator,
                new File(System.getProperty("user.home")).toPath()
        );
        terminalTabPane.getRegion().setLayoutX(100);
        terminalTabPane.getRegion().setLayoutY(700);
        terminalTabPane.getRegion().setPrefSize(600, 200);

// TO WAŻNE — dodaj jako child do layoutu!
        layout.addChild(terminalTabPane);


        // 6. Układ główny: editor + terminal w zakładkach (pionowy układ)
        Region editorRegion = layout.getView();
        editorRegion.setPrefSize(800, 700);

        VBox centerContent = new VBox();
        centerContent.getChildren().add(editorRegion);
        VBox.setVgrow(editorRegion, Priority.ALWAYS);
        VBox.setVgrow(terminalTabPane.getRegion(), Priority.NEVER);

        // 7. Pasek przycisków
        OpenButton openButton = new OpenButton(stage, editor, openFilesPanel);
        SaveButton saveButton = new SaveButton(stage, editor);
        SaveAsButton saveAsButton = new SaveAsButton(stage, editor, openFilesPanel);
        RunButton runButton = new RunButton(stage, editor, consoleView, CompilerSettingsDialog.getSavedCompilerPath());
        OpenFolderButton openFolderButton = new OpenFolderButton(stage, editor, openFilesPanel);
        DesignToggleButton designToggleButton = new DesignToggleButton(layout, editor, consoleView, terminalTabPane);
        CompilerSelectButton compilerSelectButton = new CompilerSelectButton(stage);

        HBox buttonBar = new HBox(10);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.getChildren().addAll(openButton, saveButton, saveAsButton,
                compilerSelectButton, runButton, openFolderButton, designToggleButton);

        // 8. Root VBox: pasek + content
        VBox root = new VBox();
        root.getChildren().addAll(buttonBar, centerContent);
        VBox.setVgrow(centerContent, Priority.ALWAYS);


        //ukrywanie przyciskow
        var topButtons = List.of(openButton, saveButton, saveAsButton, compilerSelectButton, runButton, openFolderButton);

        layout.modeProperty().addListener((obs, oldMode, newMode) -> {
            boolean isDesign = newMode == EditorLayout.Mode.DESIGN;
            topButtons.forEach(btn -> btn.setDisable(isDesign)); // lub .setVisible(false)
        });

        // 9. Scena i pokazanie
        Scene scene = new Scene(root, 1200, 900);
        stage.setScene(scene);
        stage.setTitle("EditorLayout Test");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        ThreadHelper.stopExecutorService();
        Platform.exit();
        System.exit(0);
    }


    public static void main(String[] args) {
        launch();
    }
}
