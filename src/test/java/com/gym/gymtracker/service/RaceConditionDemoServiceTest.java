package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.RaceConditionResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RaceConditionDemoServiceTest {

    private final RaceConditionDemoService raceConditionDemoService = new RaceConditionDemoService();

    @Test
    void demonstrateShowsUnsafeCounterAndCorrectSolutions() {
        RaceConditionResultDto result = raceConditionDemoService.demonstrate(64, 2000);

        assertEquals(64, result.getThreads());
        assertEquals(2000, result.getIncrementsPerThread());
        assertEquals(128000, result.getExpectedValue());
        assertEquals(128000, result.getAtomicCounterValue());
        assertEquals(128000, result.getSynchronizedCounterValue());
        assertTrue(result.getUnsafeCounterValue() <= result.getExpectedValue());
        assertTrue(result.getUnsafeLostUpdates() >= 0);
        assertTrue(result.isSolutionCorrect());
    }

    @Test
    void demonstrateRejectsThreadCountBelowFifty() {
        Executable action = () -> raceConditionDemoService.demonstrate(49, 1);

        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void demonstrateRejectsNonPositiveIncrements() {
        Executable action = () -> raceConditionDemoService.demonstrate(50, 0);

        assertThrows(IllegalArgumentException.class, action);
    }
}
