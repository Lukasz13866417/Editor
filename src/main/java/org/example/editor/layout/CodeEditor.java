    package org.example.editor.layout;

    import javafx.scene.control.TextArea;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.Region;

    /**
     * Komponent do edytowania kodu – oparty o JavaFX TextArea
     */
    public class CodeEditor extends Component {

        private final TextArea textArea;

        public CodeEditor(String id) {
            super(new Pane(), id);  // Tworzymy Pane jako bazowy Region
            textArea = new TextArea();
            textArea.setPromptText("Start typing your code here...");
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");

            // Ustaw TextArea jako jedyne dziecko w Pane
            ((Pane) super.region).getChildren().add(textArea);
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
    }
