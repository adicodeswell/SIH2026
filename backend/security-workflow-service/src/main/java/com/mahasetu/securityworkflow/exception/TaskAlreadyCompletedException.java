package com.mahasetu.securityworkflow.exception;

public class TaskAlreadyCompletedException extends RuntimeException {

    private final String taskId;

    public TaskAlreadyCompletedException(String taskId) {
        super("Task has already been completed: " + taskId);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
}
