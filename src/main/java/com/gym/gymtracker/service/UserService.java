package com.gym.gymtracker.service;

import com.gym.gymtracker.exception.ResourceNotFoundException;
import com.gym.gymtracker.dto.UserDto;
import com.gym.gymtracker.mapper.UserMapper;
import com.gym.gymtracker.model.User;
import com.gym.gymtracker.model.Workout;
import com.gym.gymtracker.repository.UserRepository;
import com.gym.gymtracker.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final UserMapper userMapper;


    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
            .map(userMapper::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserDto create(UserDto dto) {
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto createWithDirtyTest(UserDto dto, boolean throwError) {
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        System.out.println(">>> Шаг 1: Пользователь сохранен в БД с ID: " + savedUser.getId());

        if (throwError) {
            System.out.println(">>> ШАГ 2: Имитация сбоя...");
            throw new RuntimeException("Сбой после сохранения юзера, но до сохранения тренировки!");
        }

        Workout workout = Workout.builder()
            .name("Приветственная тренировка")
            .user(savedUser)
            .build();
        workoutRepository.save(workout);

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto createWithFirstWorkout(UserDto dto, boolean makeError) {
        User user = userMapper.toEntity(dto);

        Workout firstWorkout = Workout.builder()
            .name("Первая тренировка")
            .workoutDate(LocalDateTime.now())
            .user(user)
            .build();

        user.getWorkouts().add(firstWorkout);

        User savedUser = userRepository.save(user);

        if (makeError) {
            throw new RuntimeException("Ошибка: транзакция откатывается, данные не будут сохранены в БД");
        }

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }
}
