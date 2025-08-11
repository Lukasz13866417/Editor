package org.example.editor.demo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.example.editor.layout_api.Component;
import org.example.editor.layout_api.EditorLayout;

public class RelativeResizeDemo extends Application {

    @Override
    public void start(Stage stage) {
        // Root container for the scene
        BorderPane root = new BorderPane();

        // Editor layout (design mode enabled so components are draggable/resizable)
        EditorLayout editorLayout = new EditorLayout();
        editorLayout.setMode(EditorLayout.Mode.DESIGN);

        Region editorView = editorLayout.getView();
        editorView.setPrefSize(1000, 700);
        editorView.setStyle("-fx-background-color: #242933;");

        root.setCenter(editorView);

        // Parent component that will hold relative children
        Pane parentPane = new Pane();
        parentPane.setStyle("-fx-background-color: #2e3440; -fx-border-color: #88c0d0; -fx-border-width: 2;");
        Component parent = new Component(parentPane, "DEMO_PARENT");
        editorLayout.addChild(parent);


        // Four relative children with distinct colors
        Component childA = makeColorBlock("CHILD_A", "#bf616a"); // red-ish
        Component childB = makeColorBlock("CHILD_B", "#a3be8c"); // green-ish
        Component childC = makeColorBlock("CHILD_C", "#5e81ac"); // blue-ish
        Component childD = makeColorBlock("CHILD_D", "#ebcb8b"); // yellow-ish

        parent.addChild(childA);
        parent.addChild(childB);
        parent.addChild(childC);
        parent.addChild(childD);

        // Delay relative placement until after first layout pass so parent sizes are known
        Platform.runLater(() -> {
            // Place the parent inside the editor with margins
            parent.placeInParentRelative(0.05, 0.05);
            parent.setSizeInParentRelative(0.9, 0.85);

            // Quadrants for children
            childA.placeInParentRelative(0.00, 0.00);
            childA.setSizeInParentRelative(0.40, 0.40);
            childB.placeInParentRelative(0.50, 0.00);
            childB.setSizeInParentRelative(0.40, 0.40);
            childC.placeInParentRelative(0.00, 0.50);
            childC.setSizeInParentRelative(0.40, 0.40);
            childD.placeInParentRelative(0.50, 0.50);
            childD.setSizeInParentRelative(0.40, 0.40);
        });

        System.out.println("Parent size: " + parent.getRegion().getWidth() + "x" + parent.getRegion().getHeight());

        stage.setTitle("Relative Resize Demo");
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    private static Component makeColorBlock(String id, String color) {
        Region r = new Region();
        r.setStyle("-fx-background-color: " + color + "; -fx-border-color: rgba(0,0,0,0.35); -fx-border-width: 1;");
        return new Component(r, id);
    }

    public static void main(String[] args) {
        launch(args);
    }
}


