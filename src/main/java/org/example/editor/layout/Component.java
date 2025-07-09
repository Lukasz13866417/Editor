package org.example.editor.layout;

import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Component {

    public Component(Region region, String id) {
        this.region = region;
        this.id = id;
        this.children = new ArrayList<>();
    }

    public String getId() { return id; }

    /** Recursively build a LayoutItem tree from this component */
    public LayoutItem getLayoutItem() {
        LayoutItem item = new LayoutItem();
        item.id     = id;
        item.x      = region.getLayoutX();
        item.y      = region.getLayoutY();
        item.width  = region.getWidth();
        item.height = region.getHeight();
        for (Component c : children) {
            item.children.add(c.getLayoutItem());
        }
        return item;
    }

    /** Apply geometry data to our component, based on our ID */
    public void applyLayoutItem(LayoutItem item, Map<String, Component> lookup) {
        Component me = lookup.get(item.id);
        if (me != null) {
            me.region.relocate(item.x, item.y);
            me.region.resize(item.width, item.height);
            for (LayoutItem childItem : item.children) {
                applyLayoutItem(childItem, lookup);
            }
        }else{
            throw new IllegalStateException("Cannot find component with id: " + item.id);
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
            }
        }
        p.getChildren().add(child.region);
        children.add(child);
        child.parent = this;
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
        }
    }

    public Component getParent() {
        return parent;
    }

    public List<Component> getChildrenAsList() {
        return List.copyOf(children);
    }

    public void placeInParent(double x, double y) {
        this.region.relocate(x, y);
    }

    public void setSizeInParent(double w, double h) {
        this.region.resize(w, h);
    }

    public void putInParent(double x, double y, double w, double h) {
        placeInParent(x,y);
        setSizeInParent(w,h);
    }

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
        placeInWorld(x,y);
        setSizeInWorld(w,h);
    }

    private final String id; // unique, for keeping component info in files
    protected final Region region;
    private Component parent;
    private final List<Component> children;

}
