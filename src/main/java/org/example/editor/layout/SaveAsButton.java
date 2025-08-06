package org.example.editor.layout;

import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.example.editor.HelloApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SaveAsButton extends Button {

    public SaveAsButton(Stage stage, CodeEditor codeEditor, OpenFilesPanel openFilesPanel) {
        super("Save As");

        setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save C++ File As");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("C++ Files", "*.cpp")
            );

            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                try {
                    Files.writeString(selectedFile.toPath(), codeEditor.getCode());
                    HelloApplication.setCurrentFile(selectedFile);       // ✅ update current file
                    openFilesPanel.addFileToFolder(selectedFile);
                    // ✅ add to list
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(AlertType.ERROR, ex.getMessage()).showAndWait();
                }
            }
        });
    }
}
