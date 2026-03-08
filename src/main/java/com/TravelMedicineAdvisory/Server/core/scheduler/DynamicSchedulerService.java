package com.TravelMedicineAdvisory.Server.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSchedulerService.class);

    private final TaskScheduler taskScheduler;
    private final Map<String, TaskDefinition> tasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSchedulerService() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("Dynamic-Scheduler-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    public void registerTask(TaskDefinition taskDefinition) {
        tasks.put(taskDefinition.getName(), taskDefinition);
        logger.info("Registered task: {}", taskDefinition.getName());
        
        if (taskDefinition.isEnabled()) {
            scheduleTask(taskDefinition.getName());
        }
    }

    public void unregisterTask(String name) {
        disableTask(name);
        tasks.remove(name);
        logger.info("Unregistered task: {}", name);
    }

    public void enableTask(String name) {
        TaskDefinition task = tasks.get(name);
        if (task != null && !task.isEnabled()) {
            task.setEnabled(true);
            scheduleTask(name);
            logger.info("Enabled scheduled task: {}", name);
        }
    }

    public void disableTask(String name) {
        TaskDefinition task = tasks.get(name);
        if (task != null && task.isEnabled()) {
            task.setEnabled(false);
            cancelTask(name);
            logger.info("Disabled scheduled task: {}", name);
        }
    }

    public void runTaskNow(String name) {
        TaskDefinition task = tasks.get(name);
        if (task != null && task.isEnabled()) {
            logger.info("Running task manually: {}", name);
            executeTask(task);
        }
    }

    public TaskDefinition getTask(String name) {
        return tasks.get(name);
    }

    public Map<String, TaskDefinition> getAllTasks() {
        return new ConcurrentHashMap<>(tasks);
    }

    private void scheduleTask(String name) {
        TaskDefinition task = tasks.get(name);
        if (task != null) {
            ScheduledFuture<?> scheduledTask = taskScheduler.schedule(() -> executeTask(task), new CronTrigger(task.getCronExpression()));
            scheduledTasks.put(name, scheduledTask);
        }
    }

    private void cancelTask(String name) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(name);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTasks.remove(name);
        }
    }

    private void executeTask(TaskDefinition task) {
        try {
            logger.info("Executing scheduled task: {}", task.getName());
            task.getAction().run();
            task.setRunCount(task.getRunCount() + 1);
            task.setLastRun(LocalDateTime.now());
            logger.info("Scheduled task completed successfully: {}", task.getName());
        } catch (Exception e) {
            task.setErrorCount(task.getErrorCount() + 1);
            logger.error("Scheduled task failed: {}", task.getName(), e);
        }
    }
}
