package com.gym.gymtracker.repository;

import com.gym.gymtracker.model.Workout;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Workout> findAll();

    List<Workout> findByUserId(Long userId);
}
