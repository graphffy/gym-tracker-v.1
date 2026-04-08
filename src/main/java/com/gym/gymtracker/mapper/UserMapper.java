package com.gym.gymtracker.mapper;

import com.gym.gymtracker.dto.UserDto;
import com.gym.gymtracker.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final WorkoutMapper workoutMapper;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .workouts(user.getWorkouts() != null ?
                user.getWorkouts().stream()
                    .map(workoutMapper::toDto)
                    .collect(Collectors.toList()) : null)
            .build();
    }

    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        return User.builder()
            .id(dto.getId())
            .username(dto.getUsername())
            .email(dto.getEmail())
            .workouts(new ArrayList<>())
            .build();
    }
}
