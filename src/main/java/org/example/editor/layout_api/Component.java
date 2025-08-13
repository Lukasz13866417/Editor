package org.example.editor.layout_api;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;

public class Component {

    public Component(Region region, String id) {
        this.region = region;
        this.id = id;
        this.children = new ArrayList<>();

        // Register mapping from Region to Component for hit testing and traversal
        REGION_TO_COMPONENT.put(this.region, this);

        // Ensure region is pickable even if transparent or empty
        this.region.setPickOnBounds(true);
    }

    public String getId() {
        return id;
    }

    /**
     * Apply geometry data to our component, based on our ID
     */
    public void applyLayoutItem(LayoutItem item,
                                Map<String, ArrayList<LayoutItem>> childrenMap,
                                Map<String, Component> lookup) {
        Component me = lookup.get(item.id);
        if (me == null) {
            throw new IllegalStateException("Cannot find component with id: " + item.id);
        }

        // Determine relative geometry first, then apply via relative APIs
        if (item.hasRelativeSize()) {
            me.setRelativeSize(item.relativeWidth, item.relativeHeight);
        } else if (me.parent != null) {
            double parentWidth = me.parent.region.getWidth();
            double parentHeight = me.parent.region.getHeight();
            if (parentWidth > 0 && parentHeight > 0) {
                me.setRelativeSize(item.width / parentWidth, item.height / parentHeight);
            }
        }

        if (item.hasRelativePosition()) {
            me.setRelativePosition(item.relativeX, item.relativeY);
        } else if (me.parent != null) {
            double parentWidth = me.parent.region.getWidth();
            double parentHeight = me.parent.region.getHeight();
            if (parentWidth > 0 && parentHeight > 0) {
                me.setRelativePosition(item.x / parentWidth, item.y / parentHeight);
            }
        }

        // With bindings, geometry will follow when linked to a parent

        // Force immediate layout update
        if (me.region.getParent() != null) {
            me.region.getParent().requestLayout();
            me.region.getParent().applyCss();
            me.region.getParent().layout();
        }

        // Fetch children list (may be null if no children)
        ArrayList<LayoutItem> parsedChildren = childrenMap.get(item.id);

        // Clear and repopulate the child list for 'me'
        me.children.clear();

        if (parsedChildren != null) {
            for (LayoutItem childItem : parsedChildren) {
                // Recursively apply geometry to each child first
                applyLayoutItem(childItem, childrenMap, lookup);

                // After geometry is applied, link the child into this component's hierarchy
                Component childComp = lookup.get(childItem.id);
                if (childComp != null) {
                    me.children.add(childComp);
                    childComp.parent = me;

                    // CRITICAL: Add the child region to the parent's scene graph
                    if (me.region instanceof Pane parentPane) {
                        if (!parentPane.getChildren().contains(childComp.region)) {
                            parentPane.getChildren().add(childComp.region);
                        }
                    }
                }
            }
        }
    }


    /**
     * Adds given child to our component tree
     * and to our Region (which must be a Pane).
     */
    public void addChild(Component child) {
        if (!(region instanceof Pane p)) {
            throw new IllegalStateException(
                    "Cannot addChild(): '" + region.getClass().getSimpleName()
                            + "' is not a Pane");
        }
        if (child.region.getParent() instanceof Pane old) {
            old.getChildren().remove(child.region);
            if (child.parent != null) {
                child.parent.children.remove(child);
                child.unbindFromParentGeometry();
            }
        }
        p.getChildren().add(child.region);
        children.add(child);
        child.parent = this;
        child.bindToParentGeometry();

        // If in design mode, ensure gestures are attached appropriately after structure change
        notifyDesignModeIfActive();
    }

    /**
     * Removes child from our tree and from scene-graph.
     */
    public void removeChild(Component child) {
        if (children.remove(child)) {
            if (region instanceof Pane p) {
                p.getChildren().remove(child.region);
            }
            child.parent = null;

            // If in design mode, ensure gestures are updated after structure change
            notifyDesignModeIfActive();
            child.unbindFromParentGeometry();
        }
    }

    public Component getParent() {
        return parent;
    }

    public List<Component> getChildrenAsList() {
        return List.copyOf(children);
    }

    /**
     * Place component using relative coordinates (0.0 to 1.0)
     */
    public void placeInParentRelative(double relX, double relY) {
        // Store relative position
        setRelativePosition(relX, relY);
    }

    /**
     * Set component size using relative dimensions (0.0 to 1.0)
     */
    public void setSizeInParentRelative(double relW, double relH) {
        // Store relative size
        setRelativeSize(relW, relH);
    }

    /**
     * Internal method for absolute positioning (pixels)
     */
    // Removed absolute placement/size methods; use relative APIs instead

    public void placeInWorld(double x, double y) {
        Parent parent = region.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Region must already be added to a Parent");
        }
        Point2D parentCoords = parent.sceneToLocal(x, y);
        region.relocate(parentCoords.getX(), parentCoords.getY());
    }

    public void setSizeInWorld(double sceneWidth, double sceneHeight) {
        Parent parent = region.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Region must already be added to a Parent");
        }
        Point2D origin = parent.sceneToLocal(0, 0);
        Point2D pW = parent.sceneToLocal(sceneWidth, 0);
        double localWidth = pW.getX() - origin.getX();
        Point2D pH = parent.sceneToLocal(0, sceneHeight);
        double localHeight = pH.getY() - origin.getY();
        region.resize(localWidth, localHeight);
    }

    public void putInWorld(double x, double y, double w, double h) {
        placeInWorld(x, y);
        setSizeInWorld(w, h);
    }

    /**
     * Dynamically resize all children that have relative sizing enabled
     */
    public void resizeChildrenDynamically() {
        // No-op with bindings; children follow automatically
    }

    /**
     * Set size without triggering recursive resizing (to avoid infinite loops)
     */
    private void setSizeInParentWithoutTrigger(double w, double h) {
        this.region.setPrefWidth(w);
        this.region.setPrefHeight(h);
        this.region.setMinWidth(w);
        this.region.setMinHeight(h);
        this.region.setMaxWidth(w);
        this.region.setMaxHeight(h);
        this.region.resize(w, h);
    }

    /**
     * Enable relative sizing for this component
     */
    public void setRelativeSize(double relWidth, double relHeight) {
        this.relativeWidthProperty.set(Math.max(0.0, Math.min(1.0, relWidth)));
        this.relativeHeightProperty.set(Math.max(0.0, Math.min(1.0, relHeight)));
        rebindToParentGeometryIfNeeded();
    }

    /**
     * Enable relative positioning for this component
     */
    public void setRelativePosition(double relX, double relY) {
        double maxX = hasRelativeSize() ? (1.0 - Math.max(0.0, Math.min(1.0, relativeWidthProperty.get()))) : 1.0;
        double maxY = hasRelativeSize() ? (1.0 - Math.max(0.0, Math.min(1.0, relativeHeightProperty.get()))) : 1.0;
        this.relativeXProperty.set(Math.max(0.0, Math.min(maxX, relX)));
        this.relativeYProperty.set(Math.max(0.0, Math.min(maxY, relY)));
        rebindToParentGeometryIfNeeded();
    }

    public boolean hasRelativeSize() {
        return relativeWidthProperty.get() >= 0 && relativeHeightProperty.get() >= 0;
    }

    public boolean hasRelativePosition() {
        return relativeXProperty.get() >= 0 && relativeYProperty.get() >= 0;
    }

    public Region getRegion() {
        return region;
    }

    /**
     * Returns the deepest component at the given scene coordinates, or null if none.
     */
    public Component findDeepestAt(double sceneX, double sceneY) {
        if (!containsScenePoint(sceneX, sceneY)) {
            return null;
        }
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            Component deep = child.findDeepestAt(sceneX, sceneY);
            if (deep != null) {
                return deep;
            }
        }
        return this;
    }

    private boolean containsScenePoint(double sceneX, double sceneY) {
        javafx.geometry.Point2D local = region.sceneToLocal(sceneX, sceneY);
        return region.getBoundsInLocal().contains(local);
    }

    public double getRelativeWidth() { return relativeWidthProperty.get(); }
    public double getRelativeHeight() { return relativeHeightProperty.get(); }
    public double getRelativeX() { return relativeXProperty.get(); }
    public double getRelativeY() { return relativeYProperty.get(); }

    public DoubleProperty relativeWidthProperty() { return relativeWidthProperty; }
    public DoubleProperty relativeHeightProperty() { return relativeHeightProperty; }
    public DoubleProperty relativeXProperty() { return relativeXProperty; }
    public DoubleProperty relativeYProperty() { return relativeYProperty; }

    private final String id; // unique, for keeping component info in files
    protected final Region region;
    private Component parent;
    private final List<Component> children;

    // Relative fields as bindable properties (-1 means disabled)
    private final DoubleProperty relativeWidthProperty = new SimpleDoubleProperty(-1);
    private final DoubleProperty relativeHeightProperty = new SimpleDoubleProperty(-1);
    private final DoubleProperty relativeXProperty = new SimpleDoubleProperty(-1);
    private final DoubleProperty relativeYProperty = new SimpleDoubleProperty(-1);

    // Global mapping from Region to Component (weak to avoid leaks)
    private static final Map<Region, Component> REGION_TO_COMPONENT = Collections.synchronizedMap(new WeakHashMap<>());

    public static Component fromNode(javafx.scene.Node node) {
        javafx.scene.Node current = node;
        while (current != null) {
            if (current instanceof Region r) {
                Component c = REGION_TO_COMPONENT.get(r);
                if (c != null) return c;
            }
            current = current.getParent();
        }
        return null;
    }

    public Component getRoot() {
        Component root = this;
        while (root.parent != null) root = root.parent;
        return root;
    }

    /**
     * If the root is an EditorLayout in DESIGN mode, re-apply design gestures everywhere.
     */
    private void notifyDesignModeIfActive() {
        Component root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        if (root instanceof EditorLayout editor && editor.getMode() == EditorLayout.Mode.DESIGN) {
            editor.refreshDesignGestures();
        }
    }

    private void bindToParentGeometry() {
        if (parent == null) return;
        Region pr = parent.region;

        // Bind only when relative values are enabled
        if (hasRelativePosition()) {
            DoubleBinding boundX = Bindings.createDoubleBinding(
                    () -> pr.getWidth() * relativeXProperty.get(),
                    pr.widthProperty(), relativeXProperty);
            DoubleBinding boundY = Bindings.createDoubleBinding(
                    () -> pr.getHeight() * relativeYProperty.get(),
                    pr.heightProperty(), relativeYProperty);
            region.layoutXProperty().bind(boundX);
            region.layoutYProperty().bind(boundY);
        }
        if (hasRelativeSize()) {
            DoubleBinding boundW = Bindings.createDoubleBinding(
                    () -> pr.getWidth() * relativeWidthProperty.get(),
                    pr.widthProperty(), relativeWidthProperty);
            DoubleBinding boundH = Bindings.createDoubleBinding(
                    () -> pr.getHeight() * relativeHeightProperty.get(),
                    pr.heightProperty(), relativeHeightProperty);
            region.prefWidthProperty().bind(boundW);
            region.prefHeightProperty().bind(boundH);
            region.minWidthProperty().bind(boundW);
            region.minHeightProperty().bind(boundH);
            region.maxWidthProperty().bind(boundW);
            region.maxHeightProperty().bind(boundH);
        }
    }

    private void unbindFromParentGeometry() {
        region.layoutXProperty().unbind();
        region.layoutYProperty().unbind();
        region.prefWidthProperty().unbind();
        region.prefHeightProperty().unbind();
        region.minWidthProperty().unbind();
        region.minHeightProperty().unbind();
        region.maxWidthProperty().unbind();
        region.maxHeightProperty().unbind();
    }

    private void rebindToParentGeometryIfNeeded() {
        if (parent == null) return;
        // Rebind position if it became enabled
        if (hasRelativePosition() && !region.layoutXProperty().isBound()) {
            bindToParentGeometry();
        }
        if (hasRelativeSize() && !region.prefWidthProperty().isBound()) {
            bindToParentGeometry();
        }
    }

}