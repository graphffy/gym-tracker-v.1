package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.AsyncTaskStartResponseDto;
import com.gym.gymtracker.dto.AsyncTaskStatusDto;
import com.gym.gymtracker.dto.AsyncTaskSummaryDto;
import com.gym.gymtracker.dto.AsyncWorkoutSetStatisticsRequestDto;
import com.gym.gymtracker.service.AsyncBusinessOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/async-tasks")
@RequiredArgsConstructor
@Tag(name = "Async Tasks", description = "Asynchronous business operations")
public class AsyncTaskController {

    private final AsyncBusinessOperationService asyncBusinessOperationService;

    @Operation(summary = "Start asynchronous workout set statistics recalculation")
    @PostMapping("/workout-set-statistics")
    public AsyncTaskStartResponseDto startWorkoutSetStatisticsTask(
        @Valid @RequestBody(required = false) AsyncWorkoutSetStatisticsRequestDto request
    ) {
        return AsyncTaskStartResponseDto.builder()
            .taskId(asyncBusinessOperationService.startWorkoutSetStatisticsTask(request))
            .build();
    }

    @Operation(summary = "Get asynchronous task status")
    @GetMapping("/{taskId}")
    public AsyncTaskStatusDto getTaskStatus(@PathVariable String taskId) {
        return asyncBusinessOperationService.getTaskStatus(taskId);
    }

    @Operation(summary = "Get completed asynchronous task count")
    @GetMapping("/completed-count")
    public AsyncTaskSummaryDto getCompletedTaskCount() {
        return asyncBusinessOperationService.getTaskSummary();
    }
}
