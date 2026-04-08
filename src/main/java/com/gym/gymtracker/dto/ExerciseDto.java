package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Упражнение")
public class ExerciseDto {

    @Schema(description = "ID упражнения", example = "10")
    private Long id;

    @NotBlank(message = "Exercise name must not be blank")
    @Size(max = 100, message = "Exercise name must be less than or equal to 100 characters")
    @Schema(description = "Название упражнения", example = "Bench Press")
    private String name;

    @Size(max = 500, message = "Exercise description must be less than or equal to 500 characters")
    @Schema(description = "Описание упражнения", example = "Flat barbell bench press")
    private String description;

    @NotEmpty(message = "At least one categoryId is required")
    @Schema(description = "Список ID категорий", example = "[1,2]")
    private Set<Long> categoryIds;
}
