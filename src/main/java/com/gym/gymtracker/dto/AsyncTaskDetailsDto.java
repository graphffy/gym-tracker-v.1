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
@Schema(description = "Asynchronous task details for summary response")
public class AsyncTaskDetailsDto {

    @Schema(description = "Task id", example = "2f4a6172-4e3f-4f51-82df-6e104ba4d94d")
    private String taskId;

    @Schema(description = "Task status", example = "COMPLETED")
    private AsyncTaskState status;

    @Schema(description = "Total work units", example = "100")
    private int totalWorkUnits;

    @Schema(description = "Processed work units", example = "100")
    private int processedWorkUnits;

    @Schema(description = "Task creation time")
    private Instant createdAt;

    @Schema(description = "Task start time")
    private Instant startedAt;

    @Schema(description = "Task finish time")
    private Instant finishedAt;

    @Schema(description = "Error message when task failed")
    private String errorMessage;

    @Schema(description = "Task result")
    private String result;
}
