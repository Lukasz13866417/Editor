package org.example.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.editor.layout.Component;
import org.example.editor.layout.EditorLayout;
import org.example.editor.layout.LayoutLoader;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        EditorLayout editorLayout = new EditorLayout();

        // Saving geometry
        /*Pane containerPane = new Pane();
        containerPane.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-border-width: 2;");
        Component region1 = new Component(containerPane,"REGION1");
        region1.putInParent(50, 50, 400, 300);
        Component button1 = new Component(new Button("B1"),"BUTTON1");
        region1.addChild(button1);
        button1.putInParent(20, 20, 100, 40);
        editorLayout.addChild(region1);*/

        // Loading geometry
        Pane containerPane = new Pane();
        // Beautiful multicolor gradient background
        containerPane.setStyle(
            "-fx-background: linear-gradient(to bottom right, " +
            "#667eea 0%, #764ba2 25%, #f093fb 50%, #f5576c 75%, #4facfe 100%); " +
            "-fx-border-color: #2c3e50; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        Component region1 = new Component(containerPane,"REGION1");
        
        Button styledButton = new Button("B1");
        styledButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #ffffff, #f0f0f0); " +
            "-fx-border-color: #2c3e50; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 1, 1);"
        );
        Component button1 = new Component(styledButton,"BUTTON1");
        
        region1.addChild(button1);
        editorLayout.addChild(region1);
        try {
            LayoutLoader.load("example_layout.json",editorLayout);
        } catch (IOException e) {
            System.err.println("Could not load layout, using defaults: " + e.getMessage());
            // Set default positions using relative coordinates (0.0-1.0)
            // Region: 6.25% from left (50/800), 7.7% from top (50/650), 50% width, 46% height
            region1.putInParent(0.0625, 0.077, 0.5, 0.46);
            // Button: 10% from left, 10% from top, 50% width, 25% height (relative to region)
            button1.putInParent(0.1, 0.1, 0.5, 0.25);
        }


        Scene scene = new Scene(editorLayout.getView(), 800, 650);
        
        // Add keyboard shortcut to save layout (Ctrl+S)
        scene.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                try {
                    LayoutLoader.save("example_layout.json", editorLayout);
                    System.out.println("Layout saved manually (Ctrl+S)");
                } catch (IOException e) {
                    System.err.println("Failed to save layout: " + e.getMessage());
                }
                event.consume();
            }
        });
        
        stage.setScene(scene);
        stage.setTitle("Persistent EditorLayout Demo");
        
        // Save layout when the application closes
        stage.setOnCloseRequest(event -> {
            try {
                LayoutLoader.save("example_layout.json", editorLayout);
                System.out.println("Layout saved successfully on exit");
            } catch (IOException e) {
                System.err.println("Failed to save layout on exit: " + e.getMessage());
            }
        });
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
