    package org.example.editor;

    import javafx.application.Application;
    import javafx.scene.Scene;
    import javafx.scene.layout.BorderPane;
    import javafx.scene.layout.Pane;
    import javafx.stage.Stage;
    import javafx.scene.control.Button;
    import javafx.scene.layout.*;
    import java.io.File;
    import java.io.IOException;
    import javafx.scene.control.Alert;

    public class HelloApplication extends Application {

        private static File currentFile; //trzymac w edytorze
        private String compilerPath = null;

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

            OpenFilesPanel openFilesPanel = new OpenFilesPanel(selectedFile -> {
                try {
                    String content = java.nio.file.Files.readString(selectedFile.toPath());
                    codeEditor.setCode(content); // assuming setCode(String) loads it into TextArea
                    HelloApplication.setCurrentFile(selectedFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to load file: " + ex.getMessage()).showAndWait();
                }
            });


// Optional: wrap in a Component if you want it draggable
            /*Component openFilesComponent = new Component("Open Files", openFilesPanel);
            openFilesComponent.setLayoutX(660);
            openFilesComponent.setLayoutY(50);
            openFilesComponent.setPrefSize(130, 300);
            canvas.getChildren().add(openFilesComponent);*/
            root.setLeft(openFilesPanel);


            Button settingsBtn = new Button("Compiler…");

            settingsBtn.setOnAction(e -> {
                CompilerSettingsDialog compilerSettingsDialog = new CompilerSettingsDialog(stage);
                compilerSettingsDialog.showAndWait();
            });

            Button runBtn  = new RunButton(stage, codeEditor, consoleView, CompilerSettingsDialog.getSavedCompilerPath());
            OpenButton openBtn = new OpenButton(stage, codeEditor, openFilesPanel);

            Button saveBtn = new SaveButton(stage, codeEditor);
            SaveAsButton saveAsBtn = new SaveAsButton(stage, codeEditor, openFilesPanel);
            OpenFolderButton openFolderBtn = new OpenFolderButton(stage, codeEditor, openFilesPanel);


            HBox toolBar = new HBox(6,openBtn, openFolderBtn, saveBtn,saveAsBtn, settingsBtn, runBtn, settingsBar);

            root.setTop(toolBar);
            toolBar.setStyle("-fx-padding:8; -fx-background-color:#2b2b2b;");

            // 3) Give each an ID/key so we can persist separately
            String editorKey  = "codeEditor";
            String consoleKey = "consoleView";


            // wywalic do klasy component pozycjonowanie
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


        public static void setCurrentFile(File selectedFile) {
            currentFile = selectedFile;
        }

        public static File getCurrentFile() {
            return currentFile;
        }


    }
