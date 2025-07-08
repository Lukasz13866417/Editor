package org.example.editor;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import java.util.List;

/**
 * A simple settings bar with a toggle for entering/exiting design mode.
 * Pass one or more Component instances that should respond to design mode state.
 */
public class SettingsBar extends HBox {
    private final ToggleButton designModeToggle;

    /**
     * Create a SettingsBar that controls the given components.
     * @param components components whose designMode will be toggled
     */
    public SettingsBar(Component... components) {
        this(List.of(components));
    }

    /**
     * Create a SettingsBar that controls the given list of components.
     * @param components list of components to control
     */
    public SettingsBar(List<Component> components) {
        // Initialize toggle button
        designModeToggle = new ToggleButton("Design Mode");
        designModeToggle.setOnAction(event -> {
            boolean enabled = designModeToggle.isSelected();
            // Update each component
            components.forEach(comp -> comp.setDesignMode(enabled));
            // Update button text
            designModeToggle.setText(enabled ? "Exit Design Mode" : "Design Mode");
        });

        // Add to HBox layout
        getChildren().add(designModeToggle);
        setSpacing(8);
        setStyle("-fx-padding: 4; -fx-background-color: #2b2b2b;");
    }

    /**
     * Check if design mode is currently enabled.
     */
    public boolean isDesignMode() {
        return designModeToggle.isSelected();
    }
}
