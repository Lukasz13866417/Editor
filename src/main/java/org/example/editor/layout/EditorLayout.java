package org.example.editor.layout;

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
        super(new Pane(),"EDITOR_LAYOUT");
        currentMode = Mode.DESIGN;
        applyMode();
    }

    private void applyMode() {
        if (currentMode == Mode.DESIGN) {
            applyDesignGesturesRecursively(this);
        } else {
            clearDesignGesturesRecursively(this);
        }
    }
    
    private void applyDesignGesturesRecursively(Component component) {
        // Don't apply gestures to the root editor layout itself
        if (component != this) {
            // Apply gestures to this component's region
            DesignGestures.makeResizable(component.region);
            DesignGestures.makeDraggable(component.region);
        }
        
        // Recursively apply to all children
        for (Component child : component.getChildrenAsList()) {
            applyDesignGesturesRecursively(child);
        }
    }
    
    private void clearDesignGesturesRecursively(Component component) {
        // Don't clear gestures from the root editor layout itself
        if (component != this) {
            // Clear gestures from this component's region
            DesignGestures.clearResizable(component.region);
            DesignGestures.clearDraggable(component.region);
        }
        
        // Recursively clear from all children
        for (Component child : component.getChildrenAsList()) {
            clearDesignGesturesRecursively(child);
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
        
        // Apply design gestures to the newly added component and its children
        if (currentMode == Mode.DESIGN) {
            applyDesignGesturesRecursively(child);
        }
    }

    /** Where all Components live */
    private Pane getCanvas() {
        return (Pane) (region);
    }

    public Region getView() { return region; }

    private Mode currentMode;

}
