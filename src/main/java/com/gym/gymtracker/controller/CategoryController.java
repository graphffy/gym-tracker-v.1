package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.CategoryDto;
import com.gym.gymtracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Categories", description = "Category management")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping
    public List<CategoryDto> getAll() {
        return categoryService.findAll();
    }

    @Operation(summary = "Get category by id")
    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @Operation(summary = "Create category")
    @PostMapping
    public CategoryDto create(@Valid @RequestBody CategoryDto dto) {
        return categoryService.create(dto);
    }

    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public CategoryDto update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return categoryService.update(id, dto);
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
