package com.bozntouran.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Res4jHelperMethod {

    public static void someThingBadHappened(int delay, int faultPercent) {
        // Simulate a fault based on percentage
        int randomValue = ThreadLocalRandom.current().nextInt(100); // 0-99
        if (randomValue < faultPercent) {
            throw new RuntimeException("Simulated failure (faultPercent=" + faultPercent + "%)");
        }

        // Simulate delay
        if (delay > 0) {
            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted during delay", e);
            }
        }
    }
}
