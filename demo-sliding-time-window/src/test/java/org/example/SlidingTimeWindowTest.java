package org.example;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author snow-zen
 */
class SlidingTimeWindowTest {

    @Test
    void testFunc() throws InterruptedException {
        SlidingTimeWindow<LongAdder> stw = new SlidingTimeWindow<>(1000, 1,
                LongAdder::new, la -> la.getValue().reset());

        SlidingTimeWindow.Window<LongAdder> window = stw.currentWindow();
        window.getValue().increment();

        int beforeFreq = stw.values().stream()
                .mapToInt(LongAdder::intValue)
                .sum();

        assertEquals(1, beforeFreq);

        TimeUnit.SECONDS.sleep(1);

        int afterFreq = stw.values().stream()
                .mapToInt(LongAdder::intValue)
                .sum();
        assertEquals(0, afterFreq);
    }
}