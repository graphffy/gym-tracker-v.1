package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bulk-запрос на создание нескольких подходов одной тренировки")
public class BulkWorkoutSetRequestDto {

    @NotNull(message = "Workout id must not be null")
    @Schema(description = "ID тренировки", example = "3")
    private Long workoutId;

    @Valid
    @NotEmpty(message = "Sets list must not be empty")
    @Schema(description = "Список подходов для создания")
    private List<BulkWorkoutSetItemDto> sets;
}
