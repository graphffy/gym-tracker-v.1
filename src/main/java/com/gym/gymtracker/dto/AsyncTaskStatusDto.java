package com.gym.gymtracker.dto;

import com.gym.gymtracker.model.AsyncTaskState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Asynchronous task status")
public class AsyncTaskStatusDto {

    @Schema(description = "Task id", example = "2f4a6172-4e3f-4f51-82df-6e104ba4d94d")
    private String taskId;

    @Schema(description = "Current task state", example = "RUNNING")
    private AsyncTaskState state;

    @Schema(description = "Progress from 0 to 100", example = "75")
    private int progress;

    @Schema(description = "Human readable task result")
    private String result;

    @Schema(description = "Error message when task failed")
    private String error;

    @Schema(description = "Task creation time")
    private Instant createdAt;

    @Schema(description = "Last task status update time")
    private Instant updatedAt;

    @Schema(description = "Task start time")
    private Instant startedAt;

    @Schema(description = "Task finish time")
    private Instant finishedAt;
}
