package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.RaceConditionResultDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionDemoService {

    private int unsafeCounter;
    private int synchronizedCounter;
    private final AtomicInteger atomicCounter = new AtomicInteger();

    public RaceConditionResultDto demonstrate(int threads, int incrementsPerThread) {
        validateInput(threads, incrementsPerThread);
        resetCounters();

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> incrementCounters(incrementsPerThread, startSignal, doneSignal));
        }

        startSignal.countDown();
        awaitCompletion(doneSignal);
        executorService.shutdown();

        int expectedValue = threads * incrementsPerThread;
        return RaceConditionResultDto.builder()
            .threads(threads)
            .incrementsPerThread(incrementsPerThread)
            .expectedValue(expectedValue)
            .unsafeCounterValue(unsafeCounter)
            .atomicCounterValue(atomicCounter.get())
            .synchronizedCounterValue(synchronizedCounter)
            .unsafeLostUpdates(expectedValue - unsafeCounter)
            .solutionCorrect(atomicCounter.get() == expectedValue && synchronizedCounter == expectedValue)
            .build();
    }

    private void validateInput(int threads, int incrementsPerThread) {
        if (threads < 50) {
            throw new IllegalArgumentException("Threads must be at least 50 to demonstrate a race condition");
        }
        if (incrementsPerThread < 1) {
            throw new IllegalArgumentException("Increments per thread must be positive");
        }
    }

    private void resetCounters() {
        unsafeCounter = 0;
        synchronizedCounter = 0;
        atomicCounter.set(0);
    }

    private void incrementCounters(int incrementsPerThread, CountDownLatch startSignal, CountDownLatch doneSignal) {
        try {
            startSignal.await();
            for (int i = 0; i < incrementsPerThread; i++) {
                incrementUnsafe();
                atomicCounter.incrementAndGet();
                incrementSynchronized();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        } finally {
            doneSignal.countDown();
        }
    }

    private void incrementUnsafe() {
        int currentValue = unsafeCounter;
        if ((currentValue & 255) == 0) {
            Thread.yield();
        }
        unsafeCounter = currentValue + 1;
    }

    private synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    private void awaitCompletion(CountDownLatch doneSignal) {
        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        }
    }
}
