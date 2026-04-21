package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.AsyncTaskSummaryDto;
import com.gym.gymtracker.dto.AsyncTaskStatusDto;
import com.gym.gymtracker.dto.AsyncWorkoutSetStatisticsRequestDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.model.AsyncTaskState;
import com.gym.gymtracker.task.AsyncTaskStore;
import com.gym.gymtracker.task.WorkoutSetStatisticsTaskWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncBusinessOperationServiceTest {

    @Mock
    private AsyncTaskStore asyncTaskStore;

    @Mock
    private WorkoutSetStatisticsTaskWorker workoutSetStatisticsTaskWorker;

    @InjectMocks
    private AsyncBusinessOperationService asyncBusinessOperationService;

    @Test
    void startWorkoutSetStatisticsTaskCreatesTaskAndStartsWorker() {
        when(asyncTaskStore.createTask()).thenReturn("task-1");
        when(workoutSetStatisticsTaskWorker.calculateStatistics("task-1", 5000L))
            .thenReturn(CompletableFuture.completedFuture(null));
        AsyncWorkoutSetStatisticsRequestDto request = AsyncWorkoutSetStatisticsRequestDto.builder()
            .delayMillis(5000L)
            .build();

        String taskId = asyncBusinessOperationService.startWorkoutSetStatisticsTask(request);

        assertEquals("task-1", taskId);
        verify(workoutSetStatisticsTaskWorker).calculateStatistics("task-1", 5000L);
    }

    @Test
    void startWorkoutSetStatisticsTaskUsesZeroDelayWhenRequestIsNull() {
        when(asyncTaskStore.createTask()).thenReturn("task-1");
        when(workoutSetStatisticsTaskWorker.calculateStatistics("task-1", 0L))
            .thenReturn(CompletableFuture.completedFuture(null));

        String taskId = asyncBusinessOperationService.startWorkoutSetStatisticsTask(null);

        assertEquals("task-1", taskId);
        verify(workoutSetStatisticsTaskWorker).calculateStatistics("task-1", 0L);
    }

    @Test
    void getTaskStatusReturnsExistingTask() {
        AsyncTaskStatusDto status = AsyncTaskStatusDto.builder()
            .taskId("task-1")
            .state(AsyncTaskState.COMPLETED)
            .progress(100)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        when(asyncTaskStore.findTask("task-1")).thenReturn(Optional.of(status));

        assertSame(status, asyncBusinessOperationService.getTaskStatus("task-1"));
    }

    @Test
    void getTaskStatusThrowsWhenTaskMissing() {
        when(asyncTaskStore.findTask("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> asyncBusinessOperationService.getTaskStatus("missing"));
    }

    @Test
    void getCompletedTaskCountReturnsStoreCounter() {
        when(asyncTaskStore.getCompletedTaskCount()).thenReturn(3);

        assertEquals(3, asyncBusinessOperationService.getCompletedTaskCount());
    }

    @Test
    void getTaskSummaryReturnsStoreSummary() {
        AsyncTaskSummaryDto summary = AsyncTaskSummaryDto.builder()
            .totalTasks(3)
            .completedTasks(3)
            .build();
        when(asyncTaskStore.getSummary()).thenReturn(summary);

        assertSame(summary, asyncBusinessOperationService.getTaskSummary());
    }
}
