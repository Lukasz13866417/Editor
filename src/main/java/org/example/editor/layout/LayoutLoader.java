package org.example.editor.layout;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class LayoutLoader {

    /**
     * You have EditorLayout object, so you know what components there are, and what they do.
     * But you don't know relative positions&sizes yet.
     * This method sets these, based on the layout json file at filaName
     */
    public static void load(String fileName, EditorLayout editor) throws IOException {
        String json = Files.readString(Paths.get(fileName));
        LayoutItem root = new Gson().fromJson(json, LayoutItem.class);
        Map<String, Component> lookup = buildLookup(editor);
        editor.applyLayoutItem(root, lookup);
    }

    /**
     * Build json file based on whole editor layour tree
     */
    public static void save(String fileName, EditorLayout editor) throws IOException {
        LayoutItem root = editor.getLayoutItem();
        String json = new Gson().toJson(root);
        Files.writeString(Paths.get(fileName), json);
    }

    /*
     Helper: Build map of id->component
     */
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
