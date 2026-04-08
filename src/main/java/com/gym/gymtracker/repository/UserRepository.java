package com.gym.gymtracker.repository;

import com.gym.gymtracker.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @EntityGraph(attributePaths = {"workouts"})
    List<User> findAll();

    User findByUsername(String username);
}
