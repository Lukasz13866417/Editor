package org.example.editor.task;

public abstract class TaskResult {
    public abstract String shortMessage();
    public abstract String longMessage();
    public abstract boolean isSuccess();
}
