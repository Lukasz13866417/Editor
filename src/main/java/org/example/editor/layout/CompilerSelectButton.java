package org.example.editor.layout;

import javafx.stage.Stage;
import javafx.scene.control.Button;
import org.example.editor.layout.CompilerSettingsDialog;
import org.example.editor.layout.CppCompiler;

public class CompilerSelectButton extends Button {
    private final Stage stage;

    public CompilerSelectButton(Stage stage) {
        super("Compiler Settings");
        this.stage = stage;

        setOnAction(e -> {
            CompilerSettingsDialog dialog = new CompilerSettingsDialog(stage);
            String selectedPath = dialog.showAndWait().orElse(null);
            if (selectedPath != null) {
                CppCompiler.setCompilerPath(selectedPath);
            }
        });
    }
}
