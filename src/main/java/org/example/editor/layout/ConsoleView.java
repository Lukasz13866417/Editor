package org.example.editor.layout;

import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

/**
 * Komponent konsoli tekstowej, służący do wyświetlania logów lub wyników.
 */
public class ConsoleView extends Component {

    private final TextArea consoleArea;

    public ConsoleView(String id) {
        super(new Pane(), id);
        consoleArea = new TextArea();
        consoleArea.setEditable(false);
        consoleArea.setWrapText(true);
        consoleArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        Pane pane = (Pane) this.region;
        pane.getChildren().add(consoleArea);

        pane.widthProperty().addListener((obs, oldVal, newVal) -> consoleArea.setPrefWidth(newVal.doubleValue()));
        pane.heightProperty().addListener((obs, oldVal, newVal) -> consoleArea.setPrefHeight(newVal.doubleValue()));
    }

    /**
     * Czyści całą zawartość konsoli.
     */
    public void clear() {
        consoleArea.clear();
    }

    /**
     * Ustawia nowy tekst w konsoli.
     */
    public void setCode(String text) {
        consoleArea.setText(text);
    }

    /**
     * Dodaje nową linię tekstu na końcu konsoli.
     */
    public void appendCode(String text) {
        consoleArea.appendText(text);
    }

    /**
     * Zwraca cały tekst znajdujący się w konsoli.
     */
    public String getText() {
        return consoleArea.getText();
    }

    /**
     * Zwraca referencję do TextArea, jeśli potrzebna do dalszej konfiguracji.
     */
    public TextArea getTextArea() {
        return consoleArea;
    }

    public void setDesignMode(boolean isDesign) {
        consoleArea.setMouseTransparent(isDesign); // wyłącza przechwytywanie kliknięć
        consoleArea.setFocusTraversable(!isDesign); // nie łapie focusa
        consoleArea.setEditable(!isDesign);         // nie można edytować
    }
}
