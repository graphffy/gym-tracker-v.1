package com.gym.gymtracker.service;

import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.dto.CategoryDto;
import com.gym.gymtracker.mapper.CategoryMapper;
import com.gym.gymtracker.model.Category;
import com.gym.gymtracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        Category category = Category.builder()
            .name(dto.getName())
            .build();
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        existingCategory.setName(dto.getName());
        return categoryMapper.toDto(categoryRepository.save(existingCategory));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
