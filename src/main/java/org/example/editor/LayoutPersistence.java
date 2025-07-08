package org.example.editor;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class LayoutPersistence {
    private static final Preferences prefs =
            Preferences.userNodeForPackage(LayoutPersistence.class);

    /**
     * Save position (layoutX/Y) and size (prefWidth/Height) of a node under a given key.
     * @param node the UI node whose layout you want to persist
     * @param key  unique key (e.g. "codeEditor", "consoleView")
     */
    public static void save(Node node, String key) {
        prefs.putDouble(key + ".x", node.getLayoutX());
        prefs.putDouble(key + ".y", node.getLayoutY());
        if (node instanceof Region r) {
            prefs.putDouble(key + ".w", r.getPrefWidth());
            prefs.putDouble(key + ".h", r.getPrefHeight());
        }
    }

    /**
     * Apply saved layout to a node. If no values were saved, leaves it at whatever defaults it already has.
     * @param node the UI node to position/size
     * @param key  the same key you used in save(...)
     */
    public static void load(Node node, String key) {
        double x = prefs.getDouble(key + ".x", Double.NaN);
        double y = prefs.getDouble(key + ".y", Double.NaN);
        if (!Double.isNaN(x)) node.setLayoutX(x);
        if (!Double.isNaN(y)) node.setLayoutY(y);

        if (node instanceof Region r) {
            double w = prefs.getDouble(key + ".w", Double.NaN);
            double h = prefs.getDouble(key + ".h", Double.NaN);
            if (!Double.isNaN(w)) r.setPrefWidth(w);
            if (!Double.isNaN(h)) r.setPrefHeight(h);
        }
    }

    public static void clear(String key) {
        prefs.remove(key + ".x");
        prefs.remove(key + ".y");
        prefs.remove(key + ".w");
        prefs.remove(key + ".h");
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            System.err.println("Failed to flush preferences: " + e.getMessage());
        }
    }

    /**
     * Optional: Clear all saved layout preferences under this node.
     */
    public static void clearAll() {
        try {
            prefs.clear();
            prefs.flush();
        } catch (BackingStoreException e) {
            System.err.println("Failed to clear preferences: " + e.getMessage());
        }
    }
}
