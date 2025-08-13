package org.example.editor.layout_api;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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

    /**
     * Re-apply design gestures across the current tree when in DESIGN mode.
     * Useful after dynamic structure changes (adding/removing children).
     */
    public void refreshDesignGestures() {
        if (getMode() == Mode.DESIGN) {
            clearDesignGesturesRecursively(this);
            applyDesignGesturesRecursively(this);
        }
    }

    private void applyDesignGesturesRecursively(Component component) {
        if (component != this) {
            // Attach gestures to all components; DesignGestures determines the deepest target
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

    /**
     * Enable parent-change interaction: press 'P' to pick a source component, then click a new parent region.
     */
    public void enableParentChangeInteraction(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (getMode() != Mode.DESIGN) return;
            if (e.getCode() == KeyCode.P) {
                scene.setCursor(javafx.scene.Cursor.CROSSHAIR);
                // Step 1: pick child
                scene.setOnMouseClicked(ev1 -> {
                    if (ev1.getButton() != MouseButton.PRIMARY) return;
                    Component pickedChild = Component.fromNode(ev1.getPickResult().getIntersectedNode());
                    // Ensure we don't pick the root as a child
                    if (pickedChild == null || pickedChild == this) {
                        resetHandlers(scene);
                        return;
                    }
                    // Step 2: pick new parent
                    scene.setOnMouseClicked(ev2 -> {
                        if (ev2.getButton() != MouseButton.PRIMARY) return;
                        Component pickedParent = Component.fromNode(ev2.getPickResult().getIntersectedNode());
                        if (pickedParent == null || pickedParent == pickedChild) {
                            resetHandlers(scene);
                            return;
                        }
                        // Prevent creating cycles: new parent cannot be a descendant of child
                        Component cursor = pickedParent;
                        boolean cycles = false;
                        while (cursor != null) {
                            if (cursor == pickedChild) { cycles = true; break; }
                            cursor = cursor.getParent();
                        }
                        if (!cycles) {
                            reparentChild(pickedChild, pickedParent);
                        }
                        resetHandlers(scene);
                    });
                });
            }
        });
    }

    private void resetHandlers(Scene scene) {
        scene.setCursor(javafx.scene.Cursor.DEFAULT);
        scene.setOnMouseClicked(null);
    }

    private void reparentChild(Component child, Component newParent) {
        Component oldParent = child.getParent();
        if (oldParent == null || newParent == null || oldParent == newParent) return;

        // Validate that the new parent can host children
        if (!(newParent.getRegion() instanceof Pane)) {
            new Alert(AlertType.WARNING,
                    "Selected component cannot contain children. Please choose a container component.").showAndWait();
            return;
        }

        // Compute absolute position and size in the new parent's coordinate space
        Region childNode = child.getRegion();
        Region newParentNode = newParent.getRegion();
        javafx.geometry.Point2D topLeftInScene = childNode.localToScene(0, 0);
        javafx.geometry.Point2D topLeftInNewParent = newParentNode.sceneToLocal(topLeftInScene);

        double absX = topLeftInNewParent.getX();
        double absY = topLeftInNewParent.getY();
        double absW = childNode.getBoundsInLocal().getWidth();
        double absH = childNode.getBoundsInLocal().getHeight();

        double parentW = newParentNode.getWidth();
        double parentH = newParentNode.getHeight();
        if (parentW <= 0 || parentH <= 0) {
            newParentNode.applyCss();
            newParentNode.layout();
            parentW = newParentNode.getWidth();
            parentH = newParentNode.getHeight();
        }

        double relX = parentW > 0 ? absX / parentW : 0.0;
        double relY = parentH > 0 ? absY / parentH : 0.0;
        double relW = parentW > 0 ? absW / parentW : 0.0;
        double relH = parentH > 0 ? absH / parentH : 0.0;

        // Detach and attach
        oldParent.removeChild(child);
        newParent.addChild(child);

        // Apply relative geometry (will bind to new parent)
        child.setRelativePosition(relX, relY);
        child.setRelativeSize(relW, relH);
    }
}
