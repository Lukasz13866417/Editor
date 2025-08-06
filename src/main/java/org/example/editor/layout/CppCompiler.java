package org.example.editor.layout;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CppCompiler {
    private static String compilerPath;
    private static final long DEFAULT_TIMEOUT_SEC = 10;
    private final java.util.function.Consumer<String> liveOutput;



    public CppCompiler(String compilerPath, java.util.function.Consumer<String> liveOutput) {
        CppCompiler.compilerPath = compilerPath;
        this.liveOutput = liveOutput;
    }

    public CompilationResult compile(
            List<String> sourceFiles,
            String outputBinary,
            List<String> extraFlags,
            long timeoutSeconds)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {

        if (sourceFiles == null || sourceFiles.isEmpty()) {
            throw new IllegalArgumentException("No source files provided");
        }
        if (outputBinary == null || outputBinary.isEmpty()) {
            throw new IllegalArgumentException("Output binary path must not be null or empty");
        }

        //no compiler path
        if (compilerPath == null || compilerPath.isEmpty()) {

        }


        List<String> cmd = new ArrayList<>();
        cmd.add(compilerPath);
        cmd.addAll(sourceFiles);
        cmd.add("-o");
        cmd.add(outputBinary);
        if (extraFlags != null && !extraFlags.isEmpty()) {
            cmd.addAll(extraFlags);
        }

        System.out.println("Running compiler command: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        // Start wątki do czytania outputu i błędów na żywo
        StringBuilder stdoutBuf = new StringBuilder();
        StringBuilder stderrBuf = new StringBuilder();

        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdoutBuf.append(line).append(System.lineSeparator());
                    if (liveOutput != null) liveOutput.accept(line);
                }
            } catch (IOException ignored) {}
        });

        Thread stderrThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stderrBuf.append(line).append(System.lineSeparator());
                    if (liveOutput != null) liveOutput.accept("err: " + line);
                }
            } catch (IOException ignored) {}
        });

        stdoutThread.setDaemon(true);
        stderrThread.setDaemon(true);
        stdoutThread.start();
        stderrThread.start();

// Czekaj na zakończenie kompilacji
        boolean finished = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            stdoutThread.join(200);
            stderrThread.join(200);
            return new CompilationResult(
                    -1,
                    stdoutBuf.toString(),
                    stderrBuf.toString(),
                    "Timed out after " + timeoutSeconds + " seconds"
            );
        }

        int exitCode = proc.exitValue();
        stdoutThread.join(200);
        stderrThread.join(200);

        if (exitCode != 0) {
            return new CompilationResult(
                    exitCode,
                    stdoutBuf.toString(),
                    stderrBuf.toString(),
                    "Compilation failed with exit code " + exitCode
            );
        }

        return new CompilationResult(
                0,
                stdoutBuf.toString(),
                stderrBuf.toString(),
                ""
        );

    }

    public CompilationResult compile(
            List<String> sourceFiles,
            String outputBinary,
            List<String> extraFlags)
            throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return compile(sourceFiles, outputBinary, extraFlags, DEFAULT_TIMEOUT_SEC);
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public static class CompilationResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;
        public final String message;

        public CompilationResult(int exitCode, String stdout, String stderr) {
            this(exitCode, stdout, stderr, "");
        }

        public CompilationResult(int exitCode, String stdout, String stderr, String message) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.message = message;
        }

        public boolean success() {
            return exitCode == 0;
        }
    }

    public static void setCompilerPath(String compilerPath1) {
        compilerPath = compilerPath1;
    }
}
