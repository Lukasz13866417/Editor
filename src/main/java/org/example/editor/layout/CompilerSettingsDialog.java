package org.example.editor.layout;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.prefs.Preferences;

public class CompilerSettingsDialog extends Dialog<String> {
    private static final String PREF_KEY = "cpp.compiler.path";
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());


    public CompilerSettingsDialog(Window owner) {
        setTitle("Configure C++ Compiler");
        initOwner(owner);

        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().setAll(ok, ButtonType.CANCEL);

        TextField pathField = new TextField(prefs.get(PREF_KEY, ""));
        pathField.setPrefColumnCount(25);
        Button browse = new Button("Browse…");

        HBox row = new HBox(8, pathField, browse);
        row.setPadding(new Insets(10));
        getDialogPane().setContent(row);

        Node okBtn = getDialogPane().lookupButton(ok);
        okBtn.setDisable(true);

        Runnable validate = () -> {
            File f = new File(pathField.getText());
            okBtn.setDisable(!(f.isFile() && f.canExecute()));
        };
        pathField.textProperty().addListener((o, old, nw) -> validate.run());
        validate.run();

        browse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select C++ Compiler Executable");
            File pick = fc.showOpenDialog(owner);
            if (pick != null) pathField.setText(pick.getAbsolutePath());
        });

        setResultConverter(btn -> {
            if (btn == ok) {
                String p = pathField.getText();
                prefs.put(PREF_KEY, p);
                CppCompiler.setCompilerPath(p);
                return p;
            }
            return null;
        });
    }

    /**
     * Returns stored path, or if missing/invalid, shows dialog and returns new value (or null).
     */
    public static String getSavedCompilerPath() {
        Preferences prefs = Preferences.userNodeForPackage(CompilerSettingsDialog.class);
        return prefs.get("cpp.compiler.path", null);
    }

}
