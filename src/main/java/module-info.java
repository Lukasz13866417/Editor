module org.example.editor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.prefs;

    opens org.example.editor to javafx.fxml;
    exports org.example.editor;
}