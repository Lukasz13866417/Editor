package org.example.editor.layout;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Utility methods for making arbitrary Nodes draggable and resizable.
 */
public class DesignGestures {

    // — Draggability — //

    public static void makeDraggable(Node node) {
        final Delta dragDelta = new Delta();
        node.setOnMousePressed(event -> {
            // record the mouse’s offset from the node’s origin
            dragDelta.x = event.getX();
            dragDelta.y = event.getY();
            node.setCursor(Cursor.MOVE);
            event.consume();
        });
        node.setOnMouseDragged(event -> {
            // reposition the node relative to its parent
            node.setLayoutX(event.getSceneX() - dragDelta.x);
            node.setLayoutY(event.getSceneY() - dragDelta.y);
            event.consume();
        });
        node.setOnMouseReleased(event -> {
            node.setCursor(Cursor.DEFAULT);
            event.consume();
        });
    }

    public static void clearDraggable(Node node) {
        node.setOnMousePressed(null);
        node.setOnMouseDragged(null);
        node.setOnMouseReleased(null);
        node.setCursor(Cursor.DEFAULT);
    }

    public static void makeResizable(Node node) {
        // every node here is also a Region
        // because Component takes Region in ctor
        node.setOnMouseMoved(event -> {
            if (isInResizeZone(node, event)) {
                node.setCursor(Cursor.SE_RESIZE);
            } else {
                node.setCursor(Cursor.DEFAULT);
            }
            event.consume();
        });

        final Delta resizeDelta = new Delta();
        node.setOnMousePressed(event -> {
            if (isInResizeZone(node, event)) {
                resizeDelta.x = node.getBoundsInLocal().getWidth() - event.getX();
                resizeDelta.y = node.getBoundsInLocal().getHeight() - event.getY();
                node.setCursor(Cursor.SE_RESIZE);
                event.consume();
            }
        });
        node.setOnMouseDragged(event -> {
            if (node.getCursor() == Cursor.SE_RESIZE) {
                double newW = event.getX() + resizeDelta.x;
                double newH = event.getY() + resizeDelta.y;
                if (newW > 20) ((javafx.scene.layout.Region) node).setPrefWidth(newW);
                if (newH > 20) ((javafx.scene.layout.Region) node).setPrefHeight(newH);
                event.consume();
            }
        });
        node.setOnMouseReleased(event -> {
            node.setCursor(Cursor.DEFAULT);
            event.consume();
        });
    }

    public static void clearResizable(Node node) {
        node.setOnMouseMoved(null);
        node.setOnMousePressed(null);
        node.setOnMouseDragged(null);
        node.setOnMouseReleased(null);
        node.setCursor(Cursor.DEFAULT);
    }

    // Helper: decide if mouse is in the resize “handle” zone (e.g. bottom-right corner)
    private static boolean isInResizeZone(Node node, MouseEvent e) {
        final double border = 8;
        return e.getX() > node.getBoundsInLocal().getWidth() - border
            && e.getY() > node.getBoundsInLocal().getHeight() - border;
    }

    // Simple struct to hold mouse offsets
    private static class Delta { double x, y; }
}
