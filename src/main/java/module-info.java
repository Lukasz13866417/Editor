module org.example.editor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.prefs;

    opens org.example.editor to javafx.fxml;
    exports org.example.editor;

    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires pty4j;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;

    exports org.example.editor.config;
    exports org.example.editor.helper;
}