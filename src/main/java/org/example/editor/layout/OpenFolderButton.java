package org.example.editor.layout;

import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenFolderButton extends Button {

    public OpenFolderButton(Stage stage, Component codeEditor, OpenFilesPanel openFilesPanel) {
        super("Open Folder");

        setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Open Folder");
            File selectedDir = directoryChooser.showDialog(stage);

            if (selectedDir != null && selectedDir.isDirectory()) {
                try (Stream<Path> paths = Files.walk(selectedDir.toPath(), 1)) {
                    List<File> files = paths
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".cpp"))
                            .map(Path::toFile)
                            .collect(Collectors.toList());

                    if (!files.isEmpty()) {
                        openFilesPanel.addFolder(selectedDir, files);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });
    }
}
