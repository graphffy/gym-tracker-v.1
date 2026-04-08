package com.gym.gymtracker.mapper;

import com.gym.gymtracker.dto.CategoryDto;
import com.gym.gymtracker.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
            .id(category.getId())
            .name(category.getName())
            .build();
    }

    public List<CategoryDto> toDtoList(List<Category> categories) {
        return categories.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

}
