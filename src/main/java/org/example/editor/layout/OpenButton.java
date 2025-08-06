package org.example.editor.layout;

import java.io.File;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.nio.file.Files;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.editor.HelloApplication;

public class OpenButton extends Button {
    public OpenButton(Stage stage, CodeEditor codeEditor, OpenFilesPanel openFilesPanel) {
        super("Open");

        setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open C++ File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("C++ Files", "*.cpp")
            );

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                openFile(selectedFile, codeEditor, openFilesPanel);
            }
        });
    }

    private void openFile(File selectedFile, CodeEditor codeEditor, OpenFilesPanel openFilesPanel) {
        try {
            String content = Files.readString(selectedFile.toPath());
            codeEditor.setCode(content);
            HelloApplication.setCurrentFile(selectedFile);
            openFilesPanel.addFile(selectedFile); // Add to open files list
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
