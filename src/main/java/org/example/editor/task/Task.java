package org.example.editor.task;

public abstract class Task<ResType extends TaskResult>{
    public abstract ResType run();
}
