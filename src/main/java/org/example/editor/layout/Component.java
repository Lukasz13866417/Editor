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

        // Apply geometry to this component's region using absolute methods directly
        // (since we're loading from saved absolute coordinates)
        me.placeInParentAbsolute(item.x, item.y);
        me.setSizeInParentAbsolute(item.width, item.height);

        // Apply relative sizing if available in the saved data
        if (item.hasRelativeSize()) {
            me.setRelativeSize(item.relativeWidth, item.relativeHeight);
        } else if (me.parent != null) {
            // Calculate relative coordinates from absolute ones if not explicitly saved
            double parentWidth = me.parent.region.getWidth();
            double parentHeight = me.parent.region.getHeight();
            if (parentWidth > 0 && parentHeight > 0) {
                me.setRelativeSize(item.width / parentWidth, item.height / parentHeight);
            }
        }
        
        if (item.hasRelativePosition()) {
            me.setRelativePosition(item.relativeX, item.relativeY);
        } else if (me.parent != null) {
            // Calculate relative position from absolute ones if not explicitly saved
            double parentWidth = me.parent.region.getWidth();
            double parentHeight = me.parent.region.getHeight();
            if (parentWidth > 0 && parentHeight > 0) {
                me.setRelativePosition(item.x / parentWidth, item.y / parentHeight);
            }
        }

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

    /**
     * Place component using relative coordinates (0.0 to 1.0)
     */
    public void placeInParent(double relX, double relY) {
        // Store relative position
        setRelativePosition(relX, relY);
        
        // Calculate and apply absolute position
        if (parent != null) {
            double parentWidth = parent.region.getWidth();
            double parentHeight = parent.region.getHeight();
            double absoluteX = parentWidth * relX;
            double absoluteY = parentHeight * relY;
            placeInParentAbsolute(absoluteX, absoluteY);
        } else {
            // If no parent, treat as absolute for now
            placeInParentAbsolute(relX, relY);
        }
    }

    /**
     * Set component size using relative dimensions (0.0 to 1.0)
     */
    public void setSizeInParent(double relW, double relH) {
        // Store relative size
        setRelativeSize(relW, relH);
        
        // Calculate and apply absolute size
        if (parent != null) {
            double parentWidth = parent.region.getWidth();
            double parentHeight = parent.region.getHeight();
            double absoluteW = parentWidth * relW;
            double absoluteH = parentHeight * relH;
            setSizeInParentAbsolute(absoluteW, absoluteH);
        } else {
            // If no parent, treat as absolute for now
            setSizeInParentAbsolute(relW, relH);
        }
    }

    /**
     * Set both position and size using relative coordinates
     */
    public void putInParent(double relX, double relY, double relW, double relH) {
        placeInParent(relX, relY);
        setSizeInParent(relW, relH);
    }

    /**
     * Internal method for absolute positioning (pixels)
     */
    private void placeInParentAbsolute(double x, double y) {
        this.region.relocate(x, y);
    }

    /**
     * Internal method for absolute sizing (pixels)
     */
    private void setSizeInParentAbsolute(double w, double h) {
        // Force the exact size by setting preferred, min, and max
        setSizeInParentWithoutTrigger(w,h);
        
        // Trigger dynamic resizing of children after this component changes size
        resizeChildrenDynamically();
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
        placeInWorld(x, y);
        setSizeInWorld(w, h);
    }

    /**
     * Dynamically resize all children that have relative sizing enabled
     */
    public void resizeChildrenDynamically() {
        double parentWidth = region.getWidth();
        double parentHeight = region.getHeight();
        
        for (Component child : children) {
            // Check if child has relative sizing stored
            if (child.hasRelativeSize()) {
                double newWidth = parentWidth * child.getRelativeWidth();
                double newHeight = parentHeight * child.getRelativeHeight();
                child.setSizeInParentWithoutTrigger(newWidth, newHeight);
            }
            
            // Check if child has relative positioning
            if (child.hasRelativePosition()) {
                double newX = parentWidth * child.getRelativeX();
                double newY = parentHeight * child.getRelativeY();
                child.placeInParentAbsolute(newX, newY);
            }
        }
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
        this.relativeWidth = Math.max(0.0, Math.min(1.0, relWidth));
        this.relativeHeight = Math.max(0.0, Math.min(1.0, relHeight));
    }
    
    /**
     * Enable relative positioning for this component
     */
    public void setRelativePosition(double relX, double relY) {
        this.relativeX = Math.max(0.0, Math.min(1.0, relX));
        this.relativeY = Math.max(0.0, Math.min(1.0, relY));
    }
    
    public boolean hasRelativeSize() {
        return relativeWidth >= 0 && relativeHeight >= 0;
    }
    
    public boolean hasRelativePosition() {
        return relativeX >= 0 && relativeY >= 0;
    }
    
    public double getRelativeWidth() { return relativeWidth; }
    public double getRelativeHeight() { return relativeHeight; }
    public double getRelativeX() { return relativeX; }
    public double getRelativeY() { return relativeY; }

    private final String id; // unique, for keeping component info in files
    protected final Region region;
    private Component parent;
    private final List<Component> children;
    
    // Relative sizing fields
    private double relativeWidth = -1;  // -1 means disabled, 0.0-1.0 means percentage
    private double relativeHeight = -1;
    private double relativeX = -1;
    private double relativeY = -1;

}
