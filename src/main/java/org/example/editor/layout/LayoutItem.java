package org.example.editor.layout;

public class LayoutItem {
    // unique
    public String id;
    // geom data relative to parent (absolute pixels)
    public double x, y, width, height;
    // geom data as percentage of parent (0.0 to 1.0)
    public double relativeX = -1, relativeY = -1, relativeWidth = -1, relativeHeight = -1;
    // null if root
    public String parentId;

    // Helper methods
    public boolean hasRelativeSize() {
        return relativeWidth >= 0 && relativeHeight >= 0;
    }

    public boolean hasRelativePosition() {
        return relativeX >= 0 && relativeY >= 0;
    }
}