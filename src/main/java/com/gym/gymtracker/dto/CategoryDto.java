package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Категория упражнений")
public class CategoryDto {

    @Schema(description = "ID категории", example = "1")
    private Long id;

    @NotBlank(message = "Category name must not be blank")
    @Size(max = 100, message = "Category name must be less than or equal to 100 characters")
    @Schema(description = "Название категории", example = "Chest")
    private String name;
}
