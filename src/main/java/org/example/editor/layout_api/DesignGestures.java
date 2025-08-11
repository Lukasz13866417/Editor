package org.example.editor.layout_api;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Utility methods for making arbitrary Nodes draggable and resizable.
 */
public class DesignGestures {

    // — Combined Draggability and Resizability — //
    public static void makeDraggableAndResizable(Node node) {
        final Delta dragDelta = new Delta();
        final Delta resizeDelta = new Delta();
        final Node[] activeNodeRef = new Node[1];

        // Mouse moved handler - show appropriate cursor
        node.setOnMouseMoved(event -> {
            ResizeDirection direction = getResizeDirection(node, event);
            node.setCursor(getCursorForDirection(direction));
            event.consume();
        });

        // Mouse pressed handler - determine if we're resizing or dragging; ensure we act on deepest component
        node.setOnMousePressed(event -> {
            // If this node is part of a Component tree, redirect to the deepest component under cursor
            Component targetComponent = resolveDeepestComponent(node, event);
            Node targetNode = targetComponent != null ? targetComponent.getRegion() : node;
            activeNodeRef[0] = targetNode;
            ResizeDirection direction = getResizeDirectionAtScenePoint(targetNode, event.getSceneX(), event.getSceneY());
            if (direction != ResizeDirection.NONE) {
                // Start resizing - capture initial state using parent coordinates
                resizeDelta.direction = direction;
                // Convert scene coordinates to parent coordinates
                Point2D parentCoords = targetNode.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
                resizeDelta.x = parentCoords.getX();
                resizeDelta.y = parentCoords.getY();
                resizeDelta.startX = targetNode.getLayoutX();
                resizeDelta.startY = targetNode.getLayoutY();
                resizeDelta.startWidth = targetNode.getBoundsInLocal().getWidth();
                resizeDelta.startHeight = targetNode.getBoundsInLocal().getHeight();
                targetNode.setCursor(getCursorForDirection(direction));

                // Highlight parent during resize
                highlightParent(targetNode, resizeDelta);
                // Make component slightly transparent during resize
                targetNode.setOpacity(0.8);
            } else {
                // Start dragging - store offset from mouse to component origin
                Point2D parentCoords = targetNode.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
                dragDelta.x = parentCoords.getX() - targetNode.getLayoutX();
                dragDelta.y = parentCoords.getY() - targetNode.getLayoutY();
                targetNode.setCursor(Cursor.MOVE);

                // Highlight parent during drag
                highlightParent(targetNode, dragDelta);
                // Make component slightly transparent during drag
                targetNode.setOpacity(0.8);
            }
            event.consume();
        });

        // Mouse dragged handler - resize or drag based on mode
        node.setOnMouseDragged(event -> {
            Node targetNode = activeNodeRef[0] != null ? activeNodeRef[0] : (Node) event.getSource();
            if (resizeDelta.direction != ResizeDirection.NONE) {
                // Resize mode - calculate new dimensions and position using parent coordinates
                Point2D parentCoords = targetNode.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
                double deltaX = parentCoords.getX() - resizeDelta.x;
                double deltaY = parentCoords.getY() - resizeDelta.y;

                double newX = resizeDelta.startX;
                double newY = resizeDelta.startY;
                double newW = resizeDelta.startWidth;
                double newH = resizeDelta.startHeight;

                // Calculate new position and size based on resize direction
                switch (resizeDelta.direction) {
                    case NW -> { // Top-left corner
                        newX = resizeDelta.startX + deltaX;
                        newY = resizeDelta.startY + deltaY;
                        newW = resizeDelta.startWidth - deltaX;
                        newH = resizeDelta.startHeight - deltaY;
                    }
                    case NE -> { // Top-right corner
                        newY = resizeDelta.startY + deltaY;
                        newW = resizeDelta.startWidth + deltaX;
                        newH = resizeDelta.startHeight - deltaY;
                    }
                    case SW -> { // Bottom-left corner
                        newX = resizeDelta.startX + deltaX;
                        newW = resizeDelta.startWidth - deltaX;
                        newH = resizeDelta.startHeight + deltaY;
                    }
                    case SE -> { // Bottom-right corner
                        newW = resizeDelta.startWidth + deltaX;
                        newH = resizeDelta.startHeight + deltaY;
                    }
                }

                // Apply minimum size constraints
                if (newW > 20 && newH > 20) {
                    javafx.scene.layout.Region region = (javafx.scene.layout.Region) targetNode;

                    // Update position and size together using relocate and resize
                    region.relocate(newX, newY);

                    // Set preferred size
                    region.setPrefWidth(newW);
                    region.setPrefHeight(newH);
                    // Also set min and max size to force the resize
                    region.setMinWidth(newW);
                    region.setMinHeight(newH);
                    region.setMaxWidth(newW);
                    region.setMaxHeight(newH);
                    // Force immediate resize
                    region.resize(newW, newH);

                    // Force immediate layout update to ensure position is committed
                    if (region.getParent() != null) {
                        region.getParent().requestLayout();
                        // Force immediate layout pass
                        region.getParent().applyCss();
                        region.getParent().layout();
                    }


                }
            } else {
                // Drag mode - calculate new position from current mouse position
                Point2D currentParentCoords = targetNode.getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
                double newX = currentParentCoords.getX() - dragDelta.x;
                double newY = currentParentCoords.getY() - dragDelta.y;

                // Apply position directly to avoid triggering relative calculations during drag
                targetNode.relocate(newX, newY);

                // Force immediate layout update to ensure position is committed
                if (targetNode.getParent() != null) {
                    targetNode.getParent().requestLayout();
                    // Force immediate layout pass
                    targetNode.getParent().applyCss();
                    targetNode.getParent().layout();
                }


            }
            event.consume();
        });

        // Mouse released handler - reset cursor, restore parent styling, and calculate relative coordinates
        node.setOnMouseReleased(event -> {
            Node targetNode = activeNodeRef[0] != null ? activeNodeRef[0] : (Node) event.getSource();
            // Calculate and store relative coordinates for both resize and drag operations
            if (resizeDelta.direction != ResizeDirection.NONE || targetNode.getCursor() == Cursor.MOVE) {
                if (targetNode.getParent() != null) {
                    double parentWidth = targetNode.getParent().getBoundsInLocal().getWidth();
                    double parentHeight = targetNode.getParent().getBoundsInLocal().getHeight();

                    if (parentWidth > 0 && parentHeight > 0) {
                        double relativeWidth = targetNode.getBoundsInLocal().getWidth() / parentWidth;
                        double relativeHeight = targetNode.getBoundsInLocal().getHeight() / parentHeight;
                        double relativeX = targetNode.getLayoutX() / parentWidth;
                        double relativeY = targetNode.getLayoutY() / parentHeight;

                        // Store relative sizing in the node's user data for retrieval during save
                        targetNode.setUserData(new RelativeSizeData(relativeWidth, relativeHeight, relativeX, relativeY));

                        // Persist relative geometry into the owning Component so dynamic resizing uses updated values
                        Component comp = Component.fromNode(targetNode);
                        if (comp != null) {
                            comp.setRelativeSize(relativeWidth, relativeHeight);
                            comp.setRelativePosition(relativeX, relativeY);
                        }
                    }
                }

                // Restore parent styling based on operation type
                if (resizeDelta.direction != ResizeDirection.NONE) {
                    restoreParent(targetNode, resizeDelta);
                } else {
                    restoreParent(targetNode, dragDelta);
                }

                // Reset resize direction but keep the size locked
                resizeDelta.direction = ResizeDirection.NONE;
            }

            // Restore component opacity
            targetNode.setOpacity(1.0);

            // Reset cursor to default, let mouse moved handler set it appropriately
            targetNode.setCursor(Cursor.DEFAULT);
            activeNodeRef[0] = null;
            event.consume();
        });
    }

    private static ResizeDirection getResizeDirectionAtScenePoint(Node node, double sceneX, double sceneY) {
        Point2D local = node.sceneToLocal(sceneX, sceneY);
        final double border = 8;
        double width = node.getBoundsInLocal().getWidth();
        double height = node.getBoundsInLocal().getHeight();
        double x = local.getX();
        double y = local.getY();

        boolean left = x <= border;
        boolean right = x >= width - border;
        boolean top = y <= border;
        boolean bottom = y >= height - border;

        if (top && left) return ResizeDirection.NW;
        if (top && right) return ResizeDirection.NE;
        if (bottom && left) return ResizeDirection.SW;
        if (bottom && right) return ResizeDirection.SE;
        return ResizeDirection.NONE;
    }

    private static Component resolveDeepestComponent(Node node, MouseEvent event) {
        Component start = Component.fromNode(node);
        if (start == null) return null;
        double sx = event.getSceneX();
        double sy = event.getSceneY();
        return start.getRoot().findDeepestAt(sx, sy);
    }

    public static void clearDraggableAndResizable(Node node) {
        node.setOnMouseMoved(null);
        node.setOnMousePressed(null);
        node.setOnMouseDragged(null);
        node.setOnMouseReleased(null);
        node.setCursor(Cursor.DEFAULT);
    }

    public static void makeDraggable(Node node) {
        makeDraggableAndResizable(node);
    }

    public static void clearDraggable(Node node) {
        clearDraggableAndResizable(node);
    }

    public static void makeResizable(Node node) {
        makeDraggableAndResizable(node);
    }

    public static void clearResizable(Node node) {
        clearDraggableAndResizable(node);
    }

    // Resize direction constants
    private enum ResizeDirection {
        NONE, NW, NE, SW, SE
    }

    // Decide which corner the mouse is in (for resizing)
    private static ResizeDirection getResizeDirection(Node node, MouseEvent e) {
        final double border = 8;
        double width = node.getBoundsInLocal().getWidth();
        double height = node.getBoundsInLocal().getHeight();
        double x = e.getX();
        double y = e.getY();

        boolean left = x <= border;
        boolean right = x >= width - border;
        boolean top = y <= border;
        boolean bottom = y >= height - border;

        if (top && left) return ResizeDirection.NW;
        if (top && right) return ResizeDirection.NE;
        if (bottom && left) return ResizeDirection.SW;
        if (bottom && right) return ResizeDirection.SE;

        return ResizeDirection.NONE;
    }

    private static Cursor getCursorForDirection(ResizeDirection direction) {
        return switch (direction) {
            case NW -> Cursor.NW_RESIZE;
            case NE -> Cursor.NE_RESIZE;
            case SW -> Cursor.SW_RESIZE;
            case SE -> Cursor.SE_RESIZE;
            case NONE -> Cursor.DEFAULT;
        };
    }

    private static class Delta {
        double x, y;
        ResizeDirection direction = ResizeDirection.NONE;
        double startX, startY, startWidth, startHeight;
        String originalParentStyle; // Store original parent style for restoration
    }

    public static class RelativeSizeData {
        public final double relativeWidth, relativeHeight, relativeX, relativeY;

        public RelativeSizeData(double relativeWidth, double relativeHeight, double relativeX,double relativeY) {
            this.relativeWidth = relativeWidth;
            this.relativeHeight = relativeHeight;
            this.relativeX = relativeX;
            this.relativeY = relativeY;
        }
    }

    private static void highlightParent(Node node, Delta delta) {
        Node parent = node.getParent();
        if (parent instanceof javafx.scene.layout.Region parentRegion) {
            // Store original style
            delta.originalParentStyle = parentRegion.getStyle();

            // Create highlighted style with thicker, inverted border
            String highlightStyle = delta.originalParentStyle;

            // Remove existing border properties and add highlighted ones
            highlightStyle = highlightStyle.replaceAll("-fx-border-width:\\s*[^;]*;?", "");
            highlightStyle = highlightStyle.replaceAll("-fx-border-color:\\s*[^;]*;?", "");

            // Add highlight border
            highlightStyle += " -fx-border-width: 4; -fx-border-color: #ff6b6b; -fx-border-style: dashed;";

            parentRegion.setStyle(highlightStyle);
        }
    }

    private static void restoreParent(Node node, Delta delta) {
        Node parent = node.getParent();
        if (parent instanceof javafx.scene.layout.Region parentRegion && delta.originalParentStyle != null) {
            parentRegion.setStyle(delta.originalParentStyle);
            delta.originalParentStyle = null;
        }
    }
}