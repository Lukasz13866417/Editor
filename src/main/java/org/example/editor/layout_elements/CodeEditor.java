    package org.example.editor.layout_elements;

    import javafx.scene.control.TextArea;
    import javafx.scene.layout.Pane;
    import org.example.editor.layout_api.Component;

    /**
     * Komponent do edytowania kodu – oparty o JavaFX TextArea
     */
    public class CodeEditor extends Component {

        private final TextArea textArea;

        public CodeEditor(String id) {
            super(new Pane(), id);  // Base region is a Pane
            textArea = new TextArea();
            textArea.setPromptText("Start typing your code here...");
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");

            Pane pane = (Pane) this.region;
            pane.getChildren().add(textArea);

// Dopasowanie rozmiaru TextArea do regionu
            pane.widthProperty().addListener((obs, oldVal, newVal) -> textArea.setPrefWidth(newVal.doubleValue()));
            pane.heightProperty().addListener((obs, oldVal, newVal) -> textArea.setPrefHeight(newVal.doubleValue()));
        }


        /**
         * Pobiera tekst z edytora kodu
         */
        public String getCode() {
            return textArea.getText();
        }

        /**
         * Ustawia cały kod w edytorze
         */
        public void setCode(String code) {
            textArea.setText(code);
        }

        /**
         * Dodaje tekst na końcu edytora
         */
        public void appendCode(String code) {
            textArea.appendText(code);
        }

        /**
         * Zwraca wewnętrzne TextArea – przydatne do stylowania lub wiązania rozmiaru
         */
        public TextArea getTextArea() {
            return textArea;
        }

        public void setDesignMode(boolean isDesign) {
            textArea.setMouseTransparent(isDesign); // wyłącza przechwytywanie kliknięć
            textArea.setFocusTraversable(!isDesign); // nie łapie focusa
            textArea.setEditable(!isDesign);         // nie można edytować
        }
    }
