package org.example.editor;

import javafx.scene.Cursor;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.event.EventHandler;

public class Component extends BorderPane {
    private final TextArea textArea;
    private boolean designMode = false;

    // Move offsets
    private double dragOffsetX;
    private double dragOffsetY;

    // Top-resize state
    private boolean resizingTop = false;
    private double origLayoutY;
    private double origHeight;
    private double pressSceneY;

    // Left-resize state
    private boolean resizingLeft = false;
    private double origLayoutX;
    private double origWidth;
    private double pressSceneX;

    private static final double RESIZE_MARGIN = 5;

    // ─── Mouse Handlers ───────────────────────────────────────────────────────

    private final EventHandler<MouseEvent> onPress = e -> {
        if (!designMode) return;

        double localX = e.getX();
        double localY = e.getY();

        // Top-edge resize (primary button)
        if (e.getButton() == MouseButton.PRIMARY && localY <= RESIZE_MARGIN) {
            resizingTop = true;
            origLayoutY = getLayoutY();
            origHeight = getPrefHeight();
            pressSceneY = e.getSceneY();
            setCursor(Cursor.N_RESIZE);
            e.consume();
            return;
        }
        // Left-edge resize (primary button)
        if (e.getButton() == MouseButton.PRIMARY && localX <= RESIZE_MARGIN) {
            resizingLeft = true;
            origLayoutX = getLayoutX();
            origWidth = getPrefWidth();
            pressSceneX = e.getSceneX();
            setCursor(Cursor.W_RESIZE);
            e.consume();
            return;
        }

        // Right-click move
        if (e.getButton() == MouseButton.SECONDARY) {
            dragOffsetX = e.getSceneX() - getLayoutX();
            dragOffsetY = e.getSceneY() - getLayoutY();
            setCursor(Cursor.MOVE);
            e.consume();
        }
    };

    private final EventHandler<MouseEvent> onDrag = e -> {
        if (!designMode) return;

        // Top-edge resize
        if (resizingTop && e.isPrimaryButtonDown()) {
            double deltaY = e.getSceneY() - pressSceneY;
            // Prevent inverted size
            deltaY = Math.min(deltaY, origHeight - 50);
            double newY = origLayoutY + deltaY;
            double newH = Math.max(origHeight - deltaY, 50);
            setLayoutY(newY);
            setPrefHeight(newH);
            e.consume();
            return;
        }

        // Left-edge resize
        if (resizingLeft && e.isPrimaryButtonDown()) {
            double deltaX = e.getSceneX() - pressSceneX;
            // Prevent inverted size
            deltaX = Math.min(deltaX, origWidth - 50);
            double newX = origLayoutX + deltaX;
            double newW = Math.max(origWidth - deltaX, 50);
            setLayoutX(newX);
            setPrefWidth(newW);
            e.consume();
            return;
        }

        // Right-button move
        if (e.isSecondaryButtonDown()) {
            setLayoutX(e.getSceneX() - dragOffsetX);
            setLayoutY(e.getSceneY() - dragOffsetY);
            e.consume();
        }
    };

    private final EventHandler<MouseEvent> onMove = e -> {
        if (!designMode) return;

        double localX = e.getX();
        double localY = e.getY();

        if (localY <= RESIZE_MARGIN) {
            setCursor(Cursor.N_RESIZE);
        } else if (localX <= RESIZE_MARGIN) {
            setCursor(Cursor.W_RESIZE);
        } else {
            setCursor(Cursor.DEFAULT);
        }
        e.consume();
    };

    private final EventHandler<MouseEvent> onRelease = e -> {
        if (!designMode) return;
        resizingTop = false;
        resizingLeft = false;
        setCursor(Cursor.DEFAULT);
        e.consume();
    };

    // ─── Constructor ─────────────────────────────────────────────────────────

    public Component() {
        textArea = new TextArea();
        textArea.setPromptText("Start typing your code here...");
        textArea.setWrapText(false);
        setCenter(textArea);

        // Styling
        setStyle("-fx-background-color:#1e1e1e; -fx-border-color:#ff6600; -fx-border-width:2;");
        textArea.setStyle(
                "-fx-font-family:'Consolas';" +
                        "-fx-font-size:12pt;" +
                        "-fx-text-fill:#ff0000;"
        );

        // Register handlers
        addEventFilter(MouseEvent.MOUSE_PRESSED,   onPress);
        addEventFilter(MouseEvent.MOUSE_DRAGGED,   onDrag);
        addEventFilter(MouseEvent.MOUSE_MOVED,     onMove);
        addEventFilter(MouseEvent.MOUSE_RELEASED,  onRelease);
    }

    // ─── Design-Mode API ──────────────────────────────────────────────────────

    public void setDesignMode(boolean enabled) {
        this.designMode = enabled;
        textArea.setMouseTransparent(enabled);
        textArea.setEditable(!enabled);
        if (!enabled) setCursor(Cursor.DEFAULT);
    }

    public boolean isDesignMode() {
        return designMode;
    }

    // ─── Text API ──────────────────────────────────────────────────────────────

    public String getCode() {
        return textArea.getText();
    }

    public void setCode(String code) {
        textArea.setText(code);
    }

    public void appendCode(String code) {
        textArea.setText(textArea.getText() + code);
    }
}
