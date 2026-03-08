package com.TravelMedicineAdvisory.Server.core.scheduler;

import java.time.LocalDateTime;

public class TaskDefinition {

    private String name;
    private String description;
    private String cronExpression;
    private Runnable action;
    private boolean enabled;
    private LocalDateTime lastRun;
    private LocalDateTime nextRun;
    private long runCount;
    private long errorCount;

    public TaskDefinition() {}

    public TaskDefinition(String name, String description, String cronExpression, Runnable action, boolean enabled) {
        this.name = name;
        this.description = description;
        this.cronExpression = cronExpression;
        this.action = action;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public LocalDateTime getNextRun() {
        return nextRun;
    }

    public void setNextRun(LocalDateTime nextRun) {
        this.nextRun = nextRun;
    }

    public long getRunCount() {
        return runCount;
    }

    public void setRunCount(long runCount) {
        this.runCount = runCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }
}
