package org.example.editor;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RunButton extends Button {
    private final Stage stage;
    private final Component codeEditor;
    private final Component consoleView;
    private final CppCompiler compiler;

    public RunButton(Stage stage, Component codeEditor, Component consoleView, String compilerPath) {
        super("Compile & Run");
        this.stage = stage;
        this.codeEditor = codeEditor;
        this.consoleView = consoleView;
        this.compiler = new CppCompiler(compilerPath, line ->
                Platform.runLater(() ->
                        consoleView.appendCode(line + System.lineSeparator())
                )
        );

        setOnAction(evt -> onRun());
    }

    private void onRun() {
        File src = HelloApplication.getCurrentFile();
        consoleView.setCode("");

        if (src == null) {
            consoleView.setCode("No file open to compile." + System.lineSeparator());
            return;
        }

        String parentDir = src.getParent();
        String baseName = src.getName().replaceFirst("\\.cpp$", "");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String exeName = baseName + (isWindows ? ".exe" : "");
        String outputBin = parentDir + File.separator + exeName;

        consoleView.appendCode("Compiling '" + src.getName() + "' to '" + exeName + "'..." + System.lineSeparator());

        // KOMILACJA W OSOBNYM WĄTKU
        new Thread(() -> {
            try {
                CppCompiler.CompilationResult result = compiler.compile(
                        List.of(src.getAbsolutePath()),
                        outputBin,
                        List.of("-std=c++17", "-fno-diagnostics-color", "-pipe")
                );

                Platform.runLater(() -> {
                    if (!result.message.isEmpty()) {
                        consoleView.appendCode(result.message + System.lineSeparator());
                    }
                    if (!result.stdout.isEmpty()) {
                        consoleView.appendCode(result.stdout);
                    }
                    if (!result.stderr.isEmpty()) {
                        consoleView.appendCode("Errors:" + System.lineSeparator() + result.stderr);
                    }

                    if (!result.success()) {
                        consoleView.appendCode(System.lineSeparator() + "Compilation failed (exit code "
                                + result.exitCode + ")." + System.lineSeparator());
                        return;
                    }

                    File exeFile = new File(outputBin);
                    if (!exeFile.exists()) {
                        consoleView.appendCode("Executable not found at: "
                                + exeFile.getAbsolutePath() + System.lineSeparator());
                        return;
                    }

                    consoleView.appendCode(System.lineSeparator() + "Running '" + exeName + "'..." + System.lineSeparator());
                    runExecutable(exeFile, 50);
                });

            } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                Platform.runLater(() -> {
                    consoleView.appendCode("Build error: " + e.getMessage() + System.lineSeparator());
                });
            }
        }).start();
    }

    private void runExecutable(File exe, long timeoutSec) {
        ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath());
        pb.directory(exe.getParentFile());
        pb.redirectErrorStream(true);

        try {
            Process proc = pb.start();

            Thread outputThread = new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(proc.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        Platform.runLater(() ->
                                consoleView.appendCode(finalLine + System.lineSeparator())
                        );
                    }
                } catch (IOException ignored) {}
            });
            outputThread.setDaemon(true);
            outputThread.start();

            if (!proc.waitFor(timeoutSec, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                Platform.runLater(() ->
                        consoleView.appendCode("Process timed out after " + timeoutSec + "s" + System.lineSeparator())
                );
            }
            outputThread.join(100);
        } catch (IOException | InterruptedException e) {
            Platform.runLater(() ->
                    consoleView.appendCode("Run error: " + e.getMessage() + System.lineSeparator())
            );
            Thread.currentThread().interrupt();
        }
    }
}
