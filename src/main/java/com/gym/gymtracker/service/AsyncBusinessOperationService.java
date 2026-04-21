package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.AsyncTaskSummaryDto;
import com.gym.gymtracker.dto.AsyncTaskStatusDto;
import com.gym.gymtracker.dto.AsyncWorkoutSetStatisticsRequestDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.task.AsyncTaskStore;
import com.gym.gymtracker.task.WorkoutSetStatisticsTaskWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncBusinessOperationService {

    private final AsyncTaskStore asyncTaskStore;
    private final WorkoutSetStatisticsTaskWorker workoutSetStatisticsTaskWorker;

    public String startWorkoutSetStatisticsTask(AsyncWorkoutSetStatisticsRequestDto request) {
        String taskId = asyncTaskStore.createTask();
        workoutSetStatisticsTaskWorker.calculateStatistics(taskId, resolveDelayMillis(request));
        return taskId;
    }

    public AsyncTaskStatusDto getTaskStatus(String taskId) {
        return asyncTaskStore.findTask(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Async task not found"));
    }

    public int getCompletedTaskCount() {
        return asyncTaskStore.getCompletedTaskCount();
    }

    public AsyncTaskSummaryDto getTaskSummary() {
        return asyncTaskStore.getSummary();
    }

    private long resolveDelayMillis(AsyncWorkoutSetStatisticsRequestDto request) {
        if (request == null || request.getDelayMillis() == null) {
            return 0L;
        }
        return request.getDelayMillis();
    }
}
