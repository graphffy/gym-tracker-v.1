package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Тренировка")
public class WorkoutDto {

    @Schema(description = "ID тренировки", example = "1")
    private Long id;

    @NotBlank(message = "Workout name must not be blank")
    @Size(max = 150, message = "Workout name must be less than or equal to 150 characters")
    @Schema(description = "Название тренировки", example = "Leg Day")
    private String name;

    @Schema(description = "Дата и время тренировки", example = "2026-03-24T18:30:00")
    private LocalDateTime workoutDate;

    @NotNull(message = "User id must not be null")
    @Schema(description = "ID владельца тренировки", example = "1")
    private Long userId;
}
