module org.example.editor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires com.google.gson;

    opens org.example.editor to javafx.fxml;
    exports org.example.editor;
    exports org.example.editor.layout;
    opens org.example.editor.layout to javafx.fxml;
}