package org.example.editor.layout;

import javafx.scene.control.ToggleButton;
import org.example.editor.layout.EditorLayout;

public class DesignToggleButton extends ToggleButton {
    public DesignToggleButton(EditorLayout layout, CodeEditor codeEditor, ConsoleView consoleView, TerminalTabPaneComponent terminalTabPaneComponent) {
        super("Design Mode");

        setOnAction(e -> {
            if (isSelected()) {
                layout.setMode(EditorLayout.Mode.DESIGN);
                codeEditor.setDesignMode(true);
                consoleView.setDesignMode(true);
                terminalTabPaneComponent.setDesignMode(true);
                setText("Design Mode: ON");
            } else {
                layout.setMode(EditorLayout.Mode.USE);
                codeEditor.setDesignMode(false);
                consoleView.setDesignMode(false);
                terminalTabPaneComponent.setDesignMode(false);
                setText("Design Mode: OFF");
            }
        });

        // start in USE mode
        setSelected(false);
        setText("Design Mode: OFF");
    }
}
