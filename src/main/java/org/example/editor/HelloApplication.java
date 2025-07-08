package org.example.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // 1) Create a Pane for absolute positioning
        BorderPane root = new BorderPane();
        Pane canvas = new Pane();
        root.setCenter(canvas);

        //LayoutPersistence.clearAll();

        // 2) Instantiate your components
        Component codeEditor    = new Component();
        Component consoleView   = new Component();
        SettingsBar settingsBar = new SettingsBar(codeEditor, consoleView);

        root.setTop(settingsBar);

        // 3) Give each an ID/key so we can persist separately
        String editorKey  = "codeEditor";
        String consoleKey = "consoleView";


        // 4) Load their last-saved layout (if any)
        LayoutPersistence.load(codeEditor,  editorKey);
        LayoutPersistence.load(consoleView, consoleKey);

        // 5) If first run, you’ll need defaults:
        if (Double.isNaN(codeEditor.getLayoutX())) {
            codeEditor.setLayoutX(50);
            codeEditor.setLayoutY(50);
            codeEditor.setPrefSize(600, 400);
        }
        if (Double.isNaN(consoleView.getLayoutX())) {
            consoleView.setLayoutX(50);
            consoleView.setLayoutY(470);
            consoleView.setPrefSize(600, 150);
        }


        // 6) Add them to the root Pane
        canvas.getChildren().addAll(codeEditor, consoleView);

        // 7) When the window is closing, save their layout
        stage.setOnCloseRequest(evt -> {
            LayoutPersistence.save(codeEditor,  editorKey);
            LayoutPersistence.save(consoleView, consoleKey);
        });

        // 8) Show the scene
        Scene scene = new Scene(root, 800, 650);
        stage.setScene(scene);
        stage.setTitle("Persistent Layout Demo");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
