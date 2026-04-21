package com.gym.gymtracker.task;

import com.gym.gymtracker.dto.AsyncTaskDetailsDto;
import com.gym.gymtracker.dto.AsyncTaskSummaryDto;
import com.gym.gymtracker.dto.AsyncTaskStatusDto;
import com.gym.gymtracker.model.AsyncTaskState;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AsyncTaskStore {

    private final ConcurrentMap<String, AsyncTaskStatusDto> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger completedTaskCounter = new AtomicInteger();

    public String createTask() {
        String taskId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        tasks.put(taskId, AsyncTaskStatusDto.builder()
            .taskId(taskId)
            .state(AsyncTaskState.PENDING)
            .progress(0)
            .createdAt(now)
            .updatedAt(now)
            .build());
        return taskId;
    }

    public Optional<AsyncTaskStatusDto> findTask(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public void markRunning(String taskId) {
        updateTask(taskId, AsyncTaskState.RUNNING, 10, null, null, true, false);
    }

    public void updateProgress(String taskId, int progress, String result) {
        updateTask(taskId, AsyncTaskState.RUNNING, progress, result, null, false, false);
    }

    public void markCompleted(String taskId, String result) {
        updateTask(taskId, AsyncTaskState.COMPLETED, 100, result, null, false, true);
        completedTaskCounter.incrementAndGet();
    }

    public void markFailed(String taskId, String error) {
        updateTask(taskId, AsyncTaskState.FAILED, 100, null, error, false, true);
    }

    public int getCompletedTaskCount() {
        return completedTaskCounter.get();
    }

    public AsyncTaskSummaryDto getSummary() {
        List<AsyncTaskStatusDto> taskList = tasks.values()
            .stream()
            .sorted(Comparator.comparing(AsyncTaskStatusDto::getCreatedAt))
            .toList();

        return AsyncTaskSummaryDto.builder()
            .totalTasks(taskList.size())
            .pendingTasks(countByState(taskList, AsyncTaskState.PENDING))
            .runningTasks(countByState(taskList, AsyncTaskState.RUNNING))
            .completedTasks(getCompletedTaskCount())
            .failedTasks(countByState(taskList, AsyncTaskState.FAILED))
            .tasks(taskList.stream().map(this::toDetails).toList())
            .build();
    }

    private int countByState(List<AsyncTaskStatusDto> taskList, AsyncTaskState state) {
        return (int) taskList.stream()
            .filter(task -> task.getState() == state)
            .count();
    }

    private AsyncTaskDetailsDto toDetails(AsyncTaskStatusDto task) {
        return AsyncTaskDetailsDto.builder()
            .taskId(task.getTaskId())
            .status(task.getState())
            .totalWorkUnits(100)
            .processedWorkUnits(task.getProgress())
            .createdAt(task.getCreatedAt())
            .startedAt(task.getStartedAt())
            .finishedAt(task.getFinishedAt())
            .errorMessage(task.getError())
            .result(task.getResult())
            .build();
    }

    private void updateTask(String taskId, AsyncTaskState state, int progress, String result, String error,
                            boolean markStarted, boolean markFinished) {
        tasks.computeIfPresent(taskId, (id, current) -> AsyncTaskStatusDto.builder()
            .taskId(id)
            .state(state)
            .progress(progress)
            .result(result != null ? result : current.getResult())
            .error(error)
            .createdAt(current.getCreatedAt())
            .startedAt(resolveStartedAt(current, markStarted))
            .finishedAt(resolveFinishedAt(current, markFinished))
            .updatedAt(Instant.now())
            .build());
    }

    private Instant resolveStartedAt(AsyncTaskStatusDto current, boolean markStarted) {
        if (current.getStartedAt() != null || !markStarted) {
            return current.getStartedAt();
        }
        return Instant.now();
    }

    private Instant resolveFinishedAt(AsyncTaskStatusDto current, boolean markFinished) {
        if (current.getFinishedAt() != null || !markFinished) {
            return current.getFinishedAt();
        }
        return Instant.now();
    }
}
