package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Элемент bulk-запроса на создание подходов")
public class BulkWorkoutSetItemDto {

    @NotNull(message = "Exercise id must not be null")
    @Schema(description = "ID упражнения", example = "5")
    private Long exerciseId;

    @NotNull(message = "Weight must not be null")
    @DecimalMin(value = "0.0", message = "Weight must be zero or positive")
    @Schema(description = "Вес в кг", example = "80.0")
    private Double weight;

    @NotNull(message = "Reps must not be null")
    @Min(value = 1, message = "Reps must be at least 1")
    @Schema(description = "Количество повторений", example = "10")
    private Integer reps;
}
