package com.gym.gymtracker.repository;

import com.gym.gymtracker.model.WorkoutSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {

    @Override
    @EntityGraph(attributePaths = {"workout", "exercise"})
    List<WorkoutSet> findAll();

    List<WorkoutSet> findByWorkoutId(Long workoutId);

    @Query("""
        SELECT ws FROM WorkoutSet ws
        JOIN ws.workout w
        JOIN w.user u
        JOIN ws.exercise e
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', COALESCE(:username, ''), '%'))
          AND LOWER(e.name) LIKE LOWER(CONCAT('%', COALESCE(:exerciseName, ''), '%'))
        """)
    Page<WorkoutSet> searchByUserAndExerciseJpql(
        @Param("username") String username,
        @Param("exerciseName") String exerciseName,
        Pageable pageable
    );

    @Query(value = """
        SELECT ws.*
        FROM workout_sets ws
        JOIN workouts w ON ws.workout_id = w.id
        JOIN users u ON w.user_id = u.id
        JOIN exercises e ON ws.exercise_id = e.id
        WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:exerciseName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :exerciseName, '%')))
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM workout_sets ws
            JOIN workouts w ON ws.workout_id = w.id
            JOIN users u ON w.user_id = u.id
            JOIN exercises e ON ws.exercise_id = e.id
            WHERE (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))
              AND (:exerciseName IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :exerciseName, '%')))
            """,
        nativeQuery = true)
    Page<WorkoutSet> searchByUserAndExerciseNative(
        @Param("username") String username,
        @Param("exerciseName") String exerciseName,
        Pageable pageable
    );
}
