package org.example.editor.layout;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Root layout of editor.
 * In design mode, components can be moved/resized.
 * In use mode, they’re fixed and interactive.
 */
public class EditorLayout extends Component {

    public enum Mode { DESIGN, USE }

    public EditorLayout() {
        super(new BorderPane(),"EDITOR_LAYOUT");
        currentMode = Mode.DESIGN;
        applyMode();
    }

    private void applyMode() {
        Pane canvas = getCanvas();
        if (currentMode == Mode.DESIGN) {
            canvas.getChildren().forEach(node -> {
                DesignGestures.makeResizable(node);
                DesignGestures.makeDraggable(node);
            });
        } else {
            canvas.getChildren().forEach(node -> {
                DesignGestures.clearResizable(node);
                DesignGestures.clearDraggable(node);
            });
        }
    }

    public void setMode(Mode mode) {
        if (mode != currentMode) {
            currentMode = mode;
            applyMode();
        }
    }

    public Mode getMode() {
        return currentMode;
    }

    /** Convenience: add a Component into the editor’s canvas. */
    @Override
    public void addChild(Component child) {
        // Use your existing addChild logic
        super.addChild(child);
        applyMode();  // re-apply so handlers get wired if needed
    }

    /** Where all Components live */
    @SuppressWarnings("unchecked")
    private Pane getCanvas() {
        return (Pane) (region);
    }

    public Region getView() { return region; }

    private Mode currentMode;

}
