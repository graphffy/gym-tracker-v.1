package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.UserDto;
import com.gym.gymtracker.service.UserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить список пользователей")
    @GetMapping
    public List<UserDto> getAll() {
        return userService.findAll();
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Создать пользователя")
    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto dto) {
        return userService.create(dto);
    }

    @Operation(summary = "Транзакционный тест создания пользователя")
    @PostMapping("/transaction-test")
    public UserDto transactionTest(@Valid @RequestBody UserDto dto, @RequestParam boolean error) {
        return userService.createWithDirtyTest(dto, error);
    }

    @Operation(summary = "Удалить пользователя")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @Operation(summary = "Обновить пользователя")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return userService.update(id, dto);
    }

    @Operation(summary = "Найти пользователя по username")
    @GetMapping("/search")
    public UserDto getByUsername(@RequestParam String username) {
        return userService.findByUsername(username);
    }

}
