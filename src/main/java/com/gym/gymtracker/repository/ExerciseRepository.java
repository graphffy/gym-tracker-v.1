package com.gym.gymtracker.repository;

import com.gym.gymtracker.model.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Override
    @EntityGraph(attributePaths = {"categories"})
    List<Exercise> findAll();

    List<Exercise> findByNameContainingIgnoreCase(String name);
}
