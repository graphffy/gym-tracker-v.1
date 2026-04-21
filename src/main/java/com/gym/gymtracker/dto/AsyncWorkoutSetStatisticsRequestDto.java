package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for asynchronous workout set statistics task")
public class AsyncWorkoutSetStatisticsRequestDto {

    @Min(value = 0, message = "Delay must not be negative")
    @Max(value = 60000, message = "Delay must not be greater than 60000 ms")
    @Schema(description = "Artificial task execution delay in milliseconds", example = "5000")
    private Long delayMillis;
}
