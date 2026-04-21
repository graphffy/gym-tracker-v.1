package com.gym.gymtracker.controller;

import com.gym.gymtracker.dto.RaceConditionResultDto;
import com.gym.gymtracker.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/race-condition")
@RequiredArgsConstructor
@Validated
@Tag(name = "Race Condition Demo", description = "Race condition demonstration and solution")
public class RaceConditionController {

    private final RaceConditionDemoService raceConditionDemoService;

    @Operation(summary = "Run race condition demo with unsafe, atomic, and synchronized counters")
    @GetMapping("/demo")
    public RaceConditionResultDto demonstrate(
        @RequestParam(defaultValue = "64") @Min(50) int threads,
        @RequestParam(defaultValue = "10000") @Min(1) int incrementsPerThread
    ) {
        return raceConditionDemoService.demonstrate(threads, incrementsPerThread);
    }
}
