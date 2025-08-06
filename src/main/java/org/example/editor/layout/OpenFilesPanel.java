package org.example.editor.layout;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

public class OpenFilesPanel extends VBox {
    private final ListView<Object> fileListView = new ListView<>();
    private final File recentFilesStore = new File("recent.txt");
    private final Consumer<File> onFileSelected;

    public OpenFilesPanel(Consumer<File> onFileSelected) {
        this.onFileSelected = onFileSelected;

        // Display folders and files differently
        fileListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setContextMenu(null);
                } else if (item instanceof File file) {
                    setText(file.getName());
                    setStyle("");
                    ContextMenu menu = new ContextMenu();
                    MenuItem removeItem = new MenuItem("Remove");
                    removeItem.setOnAction(e -> {
                        fileListView.getItems().remove(file);
                        saveRecentFiles();
                    });
                    menu.getItems().add(removeItem);
                    setContextMenu(menu);

                }else if (item instanceof String folderPath) {
                    File folder = new File(folderPath);
                    setText("[ " + folder.getName() + " ]");
                    setStyle("-fx-font-weight: bold; -fx-text-fill: lightgray;");

                    ContextMenu menu = new ContextMenu();
                    MenuItem removeItem = new MenuItem("Remove Folder");
                    removeItem.setOnAction(e -> {
                        removeFolderAndFiles(folderPath);
                    });
                    menu.getItems().add(removeItem);
                    setContextMenu(menu);
                }

            }
        });



        // Handle double-click to open file
        fileListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Object selected = fileListView.getSelectionModel().getSelectedItem();
                if (selected instanceof File file && file.exists()) {
                    onFileSelected.accept(file);
                }
            }
        });

        this.getChildren().add(fileListView);
        this.setPrefWidth(160);
        this.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 6;");

        loadRecentFiles();
    }

    public void addFile(File file) {
        if (!fileListView.getItems().contains(file)) {
            fileListView.getItems().add(file);
            saveRecentFiles();
        }
    }

    public void addFolder(File folder, List<File> files) {
        String folderLabel = folder.getAbsolutePath();

        if (!fileListView.getItems().contains(folderLabel)) {
            fileListView.getItems().add(folderLabel);
        }

        for (File file : files) {
            if (!fileListView.getItems().contains(file)) {
                fileListView.getItems().add(file);
            }
        }

        saveRecentFiles();
    }


    public void removeFile(File file) {
        fileListView.getItems().remove(file);
        saveRecentFiles();
    }

    private void saveRecentFiles() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recentFilesStore))) {
            for (Object item : fileListView.getItems()) {
                if (item instanceof File file) {
                    writer.write("FILE:" + file.getAbsolutePath());
                    writer.newLine();
                } else if (item instanceof String folderPath) {
                    writer.write("FOLDER:" + folderPath);
                    writer.newLine();
                }
            }
        } catch (IOException ignored) {}
    }



    private void loadRecentFiles() {
        if (!recentFilesStore.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(recentFilesStore))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("FOLDER:")) {
                    String folderPath = line.substring(7);
                    fileListView.getItems().add(folderPath);
                } else if (line.startsWith("FILE:")) {
                    String filePath = line.substring(5);
                    File file = new File(filePath);
                    if (file.exists()) {
                        fileListView.getItems().add(file);
                    }
                }
            }
        } catch (IOException ignored) {}
    }

    private void removeFolderAndFiles(String folderPath) {
        List<Object> items = fileListView.getItems();
        int startIndex = items.indexOf(folderPath);

        if (startIndex == -1) return;

        int endIndex = startIndex + 1;
        while (endIndex < items.size()) {
            Object next = items.get(endIndex);
            if (next instanceof String) break; // next folder starts
            endIndex++;
        }

        items.subList(startIndex, endIndex).clear();
        saveRecentFiles();
    }

    public void addFileToFolder(File file) {
        String folderPath = file.getParentFile().getAbsolutePath();
        List<Object> items = fileListView.getItems();

        // Step 1: Find the folder header
        int folderIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof String s && s.equals(folderPath)) {
                folderIndex = i;
                break;
            }
        }

        if (folderIndex == -1) {
            // Folder not listed yet — create it
            addFolder(file.getParentFile(), List.of(file));
            return;
        }

        // Step 2: If file already exists, skip
        if (items.contains(file)) return;

        // Step 3: Find alphabetical insert index within this folder
        int insertIndex = folderIndex + 1;
        while (insertIndex < items.size()) {
            Object o = items.get(insertIndex);
            if (o instanceof File otherFile) {
                if (!otherFile.getParentFile().getAbsolutePath().equals(folderPath)) break;

                // Insert before the first file that comes after alphabetically
                if (file.getName().compareToIgnoreCase(otherFile.getName()) < 0) {
                    break;
                }
            } else {
                // Reached next folder header
                break;
            }
            insertIndex++;
        }

        items.add(insertIndex, file);
        saveRecentFiles();
    }




}
