package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response with asynchronous task id")
public class AsyncTaskStartResponseDto {

    @Schema(description = "Task id", example = "2f4a6172-4e3f-4f51-82df-6e104ba4d94d")
    private String taskId;
}
