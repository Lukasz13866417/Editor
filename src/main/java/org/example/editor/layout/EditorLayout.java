package org.example.editor.layout;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Root layout of editor.
 * In design mode, components can be moved/resized.
 * In use mode, they’re fixed and interactive.
 */
public class EditorLayout extends Component {

    public enum Mode { DESIGN, USE }

    private final ObjectProperty<Mode> modeProperty = new SimpleObjectProperty<>(Mode.USE);

    public EditorLayout() {
        super(new Pane(), "EDITOR_LAYOUT");
        applyMode();
    }

    private void applyMode() {
        if (getMode() == Mode.DESIGN) {
            applyDesignGesturesRecursively(this);
        } else {
            clearDesignGesturesRecursively(this);
        }
    }

    private void applyDesignGesturesRecursively(Component component) {
        if (component != this) {
            DesignGestures.makeResizable(component.region);
            DesignGestures.makeDraggable(component.region);
        }
        for (Component child : component.getChildrenAsList()) {
            applyDesignGesturesRecursively(child);
        }
    }

    private void clearDesignGesturesRecursively(Component component) {
        if (component != this) {
            DesignGestures.clearResizable(component.region);
            DesignGestures.clearDraggable(component.region);
        }
        for (Component child : component.getChildrenAsList()) {
            clearDesignGesturesRecursively(child);
        }
    }

    public void setMode(Mode mode) {
        if (mode != modeProperty.get()) {
            modeProperty.set(mode);
            applyMode();
        }
    }

    public Mode getMode() {
        return modeProperty.get();
    }

    public ObjectProperty<Mode> modeProperty() {
        return modeProperty;
    }

    @Override
    public void addChild(Component child) {
        super.addChild(child);
        if (getMode() == Mode.DESIGN) {
            applyDesignGesturesRecursively(child);
        }
    }

    public Region getView() {
        return region;
    }

    private Pane getCanvas() {
        return (Pane) region;
    }
}
