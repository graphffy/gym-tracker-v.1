package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.CategoryDto;
import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.mapper.CategoryMapper;
import com.gym.gymtracker.model.Category;
import com.gym.gymtracker.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAllReturnsMappedCategories() {
        List<Category> categories = List.of(Category.builder().id(1L).name("Chest").build());
        List<CategoryDto> dtos = List.of(CategoryDto.builder().id(1L).name("Chest").build());

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDtoList(categories)).thenReturn(dtos);

        assertSame(dtos, categoryService.findAll());
    }

    @Test
    void findByIdReturnsMappedCategory() {
        Category category = Category.builder().id(1L).name("Chest").build();
        CategoryDto dto = CategoryDto.builder().id(1L).name("Chest").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(dto);

        assertSame(dto, categoryService.findById(1L));
    }

    @Test
    void findByIdThrowsWhenCategoryMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Executable action = () -> categoryService.findById(1L);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void createSavesAndMapsCategory() {
        CategoryDto request = CategoryDto.builder().name("Back").build();
        Category saved = Category.builder().id(2L).name("Back").build();
        CategoryDto response = CategoryDto.builder().id(2L).name("Back").build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toDto(saved)).thenReturn(response);

        assertSame(response, categoryService.create(request));
    }

    @Test
    void updateChangesNameAndReturnsMappedCategory() {
        Category existing = Category.builder().id(3L).name("Old").build();
        CategoryDto request = CategoryDto.builder().name("New").build();
        CategoryDto response = CategoryDto.builder().id(3L).name("New").build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);
        when(categoryMapper.toDto(existing)).thenReturn(response);

        assertSame(response, categoryService.update(3L, request));
        assertEquals("New", existing.getName());
    }

    @Test
    void updateThrowsWhenCategoryMissing() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        CategoryDto request = CategoryDto.builder().build();
        Executable action = () -> categoryService.update(3L, request);

        assertThrows(ResourceNotFoundException.class, action);
    }

    @Test
    void deleteDelegatesToRepository() {
        categoryService.delete(5L);

        verify(categoryRepository).deleteById(5L);
    }
}
