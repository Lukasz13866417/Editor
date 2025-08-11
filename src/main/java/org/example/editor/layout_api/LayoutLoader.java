package org.example.editor.layout_api;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public class LayoutLoader {

    /**
     * You have EditorLayout object, so you know what components there are, and what they do.
     * But you don't know relative positions&sizes yet.
     * This method sets these, based on the layout json file at filaName
     */
    public static void load(String geomFile, EditorLayout editor) throws IOException {
        String jsonGeom = Files.readString(Paths.get(geomFile));
        Gson gson = new Gson();
        LayoutItem[] items = gson.fromJson(jsonGeom, LayoutItem[].class);
        if (items == null || items.length == 0) {
            throw new IllegalArgumentException("Layout file does not contain any items: " + geomFile);
        }

        LayoutItem root = findRoot(items);
        Map<String, ArrayList<LayoutItem>> childrenMap = buildChildrenMap(items);

        Map<String, Component> lookup = buildLookup(editor);
        editor.applyLayoutItem(root, childrenMap, lookup);
    }

    /**
     * Build json file based on whole editor layout tree
     */
    public static void save(String fileName, EditorLayout editor) throws IOException {
        List<LayoutItem> items = buildLayoutItems(editor);
        String json = new Gson().toJson(items);
        Files.writeString(Paths.get(fileName), json);
    }

    /*
     * Build list of LayoutItems
     */
    private static List<LayoutItem> buildLayoutItems(Component root) {
        List<LayoutItem> items = new ArrayList<>();
        Deque<Component> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Component curr = stack.pop();
            LayoutItem li = new LayoutItem();
            li.id = curr.getId();
            li.x = curr.region.getLayoutX();
            li.y = curr.region.getLayoutY();
            li.width = curr.region.getWidth();
            li.height = curr.region.getHeight();
            Component parent = curr.getParent();
            li.parentId = parent != null ? parent.getId() : null;

            // Check for relative sizing data stored in the node's user data
            Object userData = curr.region.getUserData();
            if (userData instanceof DesignGestures.RelativeSizeData relData) {
                li.relativeWidth = relData.relativeWidth;
                li.relativeHeight = relData.relativeHeight;
                li.relativeX = relData.relativeX;
                li.relativeY = relData.relativeY;
            } else if (curr.hasRelativeSize() || curr.hasRelativePosition()) {
                // Use component's stored relative data
                li.relativeWidth = curr.hasRelativeSize() ? curr.getRelativeWidth() : -1;
                li.relativeHeight = curr.hasRelativeSize() ? curr.getRelativeHeight() : -1;
                li.relativeX = curr.hasRelativePosition() ? curr.getRelativeX() : -1;
                li.relativeY = curr.hasRelativePosition() ? curr.getRelativeY() : -1;
            }


            items.add(li);
            for (Component child : curr.getChildrenAsList()) {
                stack.push(child);
            }
        }
        return items;
    }

    /*
     Helper: Build map of id->component
     */
    private static Map<String, ArrayList<LayoutItem>> buildChildrenMap(LayoutItem[] items) {
        Map<String, ArrayList<LayoutItem>> childrenMap = new HashMap<>();
        for (LayoutItem item : items) {
            if (item.parentId != null) {
                childrenMap.computeIfAbsent(item.parentId, k -> new ArrayList<>()).add(item);
            }
        }
        return childrenMap;
    }

    private static LayoutItem findRoot(LayoutItem[] items) {
        return Arrays.stream(items)
                .filter(i -> i.parentId == null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No root LayoutItem (parentId == null) found in layout file."));
    }

    private static Map<String, Component> buildLookup(Component root) {
        Map<String, Component> lookup = new HashMap<>();
        Deque<Component> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            Component curr = stack.pop();
            lookup.put(curr.getId(), curr);
            for (Component child : curr.getChildrenAsList()) {
                stack.push(child);
            }
        }

        return lookup;
    }
}