package com.gym.gymtracker.service;

import com.gym.gymtracker.dto.RaceConditionResultDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionDemoService {

    public RaceConditionResultDto demonstrate(int threads, int incrementsPerThread) {
        validateInput(threads, incrementsPerThread);

        int unsafeValue = runScenario(threads, incrementsPerThread, new UnsafeCounter());
        int synchronizedValue = runScenario(threads, incrementsPerThread, new SynchronizedCounter());
        int atomicValue = runScenario(threads, incrementsPerThread, new AtomicCounter());

        int expectedValue = threads * incrementsPerThread;
        return RaceConditionResultDto.builder()
            .threads(threads)
            .incrementsPerThread(incrementsPerThread)
            .expectedValue(expectedValue)
            .unsafeCounterValue(unsafeValue)
            .atomicCounterValue(atomicValue)
            .synchronizedCounterValue(synchronizedValue)
            .unsafeLostUpdates(expectedValue - unsafeValue)
            .solutionCorrect(atomicValue == expectedValue && synchronizedValue == expectedValue)
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

    private int runScenario(int threadCount, int incrementsPerThread, IncrementCounter counter) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> incrementCounter(counter, incrementsPerThread, startSignal, doneSignal));
        }

        startSignal.countDown();
        awaitCompletion(doneSignal);
        executorService.shutdown();
        return counter.get();
    }

    private void incrementCounter(IncrementCounter counter, int incrementsPerThread,
                                  CountDownLatch startSignal, CountDownLatch doneSignal) {
        try {
            startSignal.await();
            for (int i = 0; i < incrementsPerThread; i++) {
                counter.increment();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        } finally {
            doneSignal.countDown();
        }
    }

    private void awaitCompletion(CountDownLatch doneSignal) {
        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Race condition demo was interrupted", ex);
        }
    }

    private interface IncrementCounter {

        void increment();

        int get();
    }

    private static class UnsafeCounter implements IncrementCounter {

        private int value;

        @Override
        public void increment() {
            value++;
        }

        @Override
        public int get() {
            return value;
        }
    }

    private static class SynchronizedCounter implements IncrementCounter {

        private int value;

        @Override
        public synchronized void increment() {
            value++;
        }

        @Override   
        public synchronized int get() {
            return value;
        }
    }

    private static class AtomicCounter implements IncrementCounter {

        private final AtomicInteger value = new AtomicInteger();

        @Override
        public void increment() {
            value.incrementAndGet();
        }

        @Override
        public int get() {
            return value.get();
        }
    }
}
