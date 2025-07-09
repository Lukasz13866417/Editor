package org.example.editor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
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
        /*Component button1 = new Component(new Button("B1"),"BUTTON1");
        button1.putInParent(100,100,100,100);
        editorLayout.addChild(button1);
        LayoutLoader.save("example_layout.json",editorLayout);*/

        // Loading geometry
        Component button1 = new Component(new Button("B1"),"BUTTON1");
        editorLayout.addChild(button1);
        LayoutLoader.load("example_layout.json",editorLayout);

        // class XD extends Button{}
        // XD xd = new XD();
        // -> Component xd = new Component(new XD(), "coś");

        Scene scene = new Scene(editorLayout.getView(), 800, 650);
        stage.setScene(scene);
        stage.setTitle("Persistent EditorLayout Demo");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
