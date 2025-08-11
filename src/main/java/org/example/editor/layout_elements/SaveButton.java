package org.example.editor.layout_elements;

import javafx.stage.Stage;
import java.nio.file.Files;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import org.example.editor.HelloApplication;

import java.io.IOException;
import java.io.File;


public class SaveButton extends Button {

    public SaveButton(Stage stage, CodeEditor codeEditor) {
        super("Save");
        setOnAction(e -> {
            if (HelloApplication.getCurrentFile() == null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save C++ File As");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("C++ Files", "*.cpp")
                );
                File selectedFile = fileChooser.showSaveDialog(stage);
                if (selectedFile != null) {
                    try {
                        Files.writeString(selectedFile.toPath(), codeEditor.getCode());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
                    }
                }
            } else {
                try {
                    Files.writeString(HelloApplication.getCurrentFile().toPath(), codeEditor.getCode());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "File Save Failed", ex.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

}
