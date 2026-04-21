package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Asynchronous tasks summary")
public class AsyncTaskSummaryDto {

    @Schema(description = "Total task count", example = "7")
    private int totalTasks;

    @Schema(description = "Pending task count", example = "0")
    private int pendingTasks;

    @Schema(description = "Running task count", example = "0")
    private int runningTasks;

    @Schema(description = "Completed task count", example = "7")
    private int completedTasks;

    @Schema(description = "Failed task count", example = "0")
    private int failedTasks;

    @Schema(description = "Task details")
    private List<AsyncTaskDetailsDto> tasks;
}
