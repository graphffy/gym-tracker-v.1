package com.gym.gymtracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Race condition demonstration result")
public class RaceConditionResultDto {

    @Schema(description = "Number of executor threads", example = "64")
    private int threads;

    @Schema(description = "Increments executed by every thread", example = "10000")
    private int incrementsPerThread;

    @Schema(description = "Expected counter value", example = "640000")
    private int expectedValue;

    @Schema(description = "Counter value without synchronization")
    private int unsafeCounterValue;

    @Schema(description = "Counter value with AtomicInteger")
    private int atomicCounterValue;

    @Schema(description = "Counter value with synchronized increment")
    private int synchronizedCounterValue;

    @Schema(description = "Number of updates lost by the unsafe counter")
    private int unsafeLostUpdates;

    @Schema(description = "True when synchronized and atomic counters reached the expected value")
    private boolean solutionCorrect;
}
