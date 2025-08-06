module org.example.editor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;
    requires pty4j;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    opens org.example.editor to javafx.fxml;
    exports org.example.editor;
    exports org.example.editor.layout;
    opens org.example.editor.layout to javafx.fxml;
    exports org.example.editor.config;
    exports org.example.editor.helper;
}