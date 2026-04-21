package com.gym.gymtracker.task;

import com.gym.gymtracker.repository.WorkoutSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class WorkoutSetStatisticsTaskWorker {

    private final AsyncTaskStore asyncTaskStore;
    private final WorkoutSetRepository workoutSetRepository;

    @Async
    public CompletableFuture<Void> calculateStatistics(String taskId, long delayMillis) {
        try {
            asyncTaskStore.markRunning(taskId);
            asyncTaskStore.updateProgress(taskId, 40, "Reading workout set data");
            long workoutSetCount = workoutSetRepository.count();
            asyncTaskStore.updateProgress(taskId, 80, "Calculated workout set count: " + workoutSetCount);
            sleepIfNeeded(delayMillis);
            asyncTaskStore.markCompleted(taskId, "Workout set statistics recalculated. Total sets: " + workoutSetCount);
        } catch (RuntimeException ex) {
            asyncTaskStore.markFailed(taskId, ex.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    private void sleepIfNeeded(long delayMillis) {
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Workout set statistics task was interrupted", ex);
        }
    }
}
