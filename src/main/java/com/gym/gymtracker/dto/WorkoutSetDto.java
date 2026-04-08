package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Подход упражнения")
public class WorkoutSetDto {

    @Schema(description = "ID подхода", example = "1")
    private Long id;

    @NotNull(message = "Weight must not be null")
    @Positive(message = "Weight must be positive")
    @Schema(description = "Вес в кг", example = "80.5")
    private Double weight;

    @NotNull(message = "Reps must not be null")
    @Min(value = 1, message = "Reps must be at least 1")
    @Schema(description = "Количество повторений", example = "10")
    private Integer reps;

    @NotNull(message = "Workout id must not be null")
    @Schema(description = "ID тренировки", example = "3")
    private Long workoutId;

    @NotNull(message = "Exercise id must not be null")
    @Schema(description = "ID упражнения", example = "5")
    private Long exerciseId;
}
