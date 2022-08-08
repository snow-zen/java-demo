package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 滑动时间窗口模型。
 *
 * 参考 <a href="https://github.com/alibaba/Sentinel/blob/master/sentinel-core/src/main/java/com/alibaba/csp/sentinel/slots/statistic/base/LeapArray.java">Sentinel LeapArray</a>
 *
 * @author snow-zen
 */
public class SlidingTimeWindow<T> {

    /**
     * 间隔大小
     */
    @Getter
    private final int interval;

    /**
     * 最大样本大小
     */
    @Getter
    private final int maxSampleCount;

    /**
     * 窗口大小
     */
    @Getter
    private final int windowLength;

    /**
     * 实现数组
     */
    private final AtomicReferenceArray<Window<T>> array;

    /**
     * 初始化窗口值函数
     */
    private final Supplier<T> initFunction;

    /**
     * 重置窗口值函数
     */
    private final Consumer<Window<T>> resetFunction;

    /**
     * 更新锁仅在窗口弃用需要更新时使用
     */
    private final ReentrantLock updateLock = new ReentrantLock();

    public SlidingTimeWindow(int interval, int maxSampleCount,
                             Supplier<T> initFunction,
                             Consumer<Window<T>> resetFunction) {
        this.interval = interval;
        this.maxSampleCount = maxSampleCount;
        this.windowLength = interval / maxSampleCount;
        this.initFunction = initFunction;
        this.resetFunction = resetFunction;
        this.array = new AtomicReferenceArray<>(maxSampleCount);
    }

    /**
     * @return 获取当前时间所在的窗口
     */
    public Window<T> currentWindow() {
        return currentWindow(System.currentTimeMillis());
    }

    private Window<T> currentWindow(long timeMillis) {
        if (timeMillis < 0) {
            return null;
        }

        int idx = calculateWindowIdx(timeMillis);
        long windowStart = calculateWindowStart(timeMillis);

        while (true) {
            Window<T> window = array.get(idx);

            if (window == null) {
                // 当前时间的窗口还未创建
                Window<T> newWindows = new Window<>(windowStart, initFunction.get());
                if (array.compareAndSet(idx, null, newWindows)) {
                    return newWindows;
                }
            } else if (windowStart == window.getWindowStart()) {
                // 窗口的开始时刻等于期望的开始时刻
                return window;
            } else if (windowStart > window.getWindowStart()) {
                // 窗口的开始时刻小于期望的开始时刻，则表示该窗口已过期
                // 过期窗口需要重新创建
                if (updateLock.tryLock()) {
                    try {
                        resetWindow(window, windowStart);
                        return window;
                    } finally {
                        updateLock.unlock();
                    }
                }
            } else if (windowStart < window.getWindowStart()) {
                // 窗口的开始时刻大于期望的开始时刻，则表示指定的时间已过去且超过了间隔时间
                return new Window<>(windowStart, initFunction.get());
            }
        }
    }

    /**
     * @return 获取所有当前未被弃用的窗口的值
     */
    public List<T> values() {
        int length = array.length();
        List<T> result = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            Window<T> window = array.get(i);
            if (window == null || isWindowDeprecated(System.currentTimeMillis(), window)) {
                continue;
            }
            result.add(window.getValue());
        }
        return result;
    }

    /**
     * @return 获取所有当前未被弃用的窗口
     */
    public List<Window<T>> windows() {
        int length = array.length();
        List<Window<T>> result = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            Window<T> window = array.get(i);
            if (window == null || isWindowDeprecated(System.currentTimeMillis(), window)) {
                continue;
            }
            result.add(window);
        }
        return result;
    }

    /**
     * 判断窗口是否已弃用
     */
    private boolean isWindowDeprecated(long timeMillis, Window<T> window) {
        return timeMillis - window.getWindowStart() > interval;
    }

    /**
     * 重置窗口
     */
    private void resetWindow(Window<T> window, long windowStart) {
        resetFunction.accept(window);
        window.setWindowStart(windowStart);
    }

    /**
     * 计算当前时间所在窗口的开始时刻
     */
    private long calculateWindowStart(long timeMillis) {
        return timeMillis - timeMillis % windowLength;
    }

    /**
     * 计算当前时间所在窗口的索引
     */
    private int calculateWindowIdx(long timeMillis) {
        long timeId = timeMillis / windowLength;
        return (int) (timeId % array.length());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Window<T> {
        private long windowStart;
        private T value;
    }
}
