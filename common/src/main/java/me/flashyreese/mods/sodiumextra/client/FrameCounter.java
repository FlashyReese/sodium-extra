package me.flashyreese.mods.sodiumextra.client;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

public class FrameCounter {
    private static final FrameCounter INSTANCE = new FrameCounter();
    private final Deque<FrameSample> samples = new ArrayDeque<>();
    private final long smoothWindow = 500_000_000L; // 0.5 seconds in nanoseconds
    private final long windowNanos = 5_000_000_000L;    // 5 seconds in nanoseconds
    private final long updateIntervalNanos = 500_000_000L; // 0.5 seconds in nanoseconds
    private long lastFrameTime = -1;
    private long lastUpdateTime = 0;
    private double cachedSmoothFps = 0;
    private double cachedAverageFps = 0;
    private double cachedOnePercentLowFps = 0;
    private double cachedPointOnePercentLowFps = 0;

    public static FrameCounter getInstance() {
        return FrameCounter.INSTANCE;
    }

    public synchronized void onFrame() {
        long now = System.nanoTime();

        // Record new frame delta
        if (this.lastFrameTime != -1) {
            long delta = now - this.lastFrameTime;
            this.samples.addLast(new FrameSample(now, delta));
        }
        this.lastFrameTime = now;

        // Trim old samples
        while (!this.samples.isEmpty() && now - this.samples.peekFirst().timestamp > this.windowNanos) {
            this.samples.removeFirst();
        }

        // Throttle stat computation
        if (now - this.lastUpdateTime >= this.updateIntervalNanos) {
            this.lastUpdateTime = now;
            if (!this.samples.isEmpty()) {
                long totalNanos = this.samples.stream().mapToLong(s -> s.deltaNanos).sum();
                this.cachedAverageFps = (double) (this.samples.size() * 1_000_000_000L) / totalNanos;
                this.cachedOnePercentLowFps = this.computePercentileLow(1.0);
                this.cachedPointOnePercentLowFps = this.computePercentileLow(0.1);
                this.cachedSmoothFps = this.computeSmoothFpsFromRecentFrames(now);
            } else {
                this.cachedAverageFps = this.cachedOnePercentLowFps = this.cachedPointOnePercentLowFps = this.cachedSmoothFps = 0;
            }
        }
    }

    private double computeSmoothFpsFromRecentFrames(long now) {
        List<Long> recent = this.samples.stream()
                .filter(s -> now - s.timestamp <= this.smoothWindow)
                .map(s -> s.deltaNanos)
                .toList();

        if (recent.isEmpty()) return 0;

        double avgDelta = recent.stream().mapToLong(Long::longValue).average().orElse(0);
        return 1_000_000_000.0 / avgDelta;
    }

    public synchronized int getSmoothFps() {
        return (int) Math.round(this.cachedSmoothFps);
    }

    public synchronized int getAverageFps() {
        return (int) Math.round(this.cachedAverageFps);
    }

    public synchronized int getOnePercentLowFps() {
        return (int) Math.round(this.cachedOnePercentLowFps);
    }

    public synchronized int getPointOnePercentLowFps() {
        return (int) Math.round(this.cachedPointOnePercentLowFps);
    }

    private double computePercentileLow(double percent) {
        if (this.samples.isEmpty()) return 0;
        List<Long> deltas = this.samples.stream()
                .map(s -> s.deltaNanos)
                .sorted(Comparator.reverseOrder())  // slowest frames first
                .toList();

        int count = Math.max(1, (int) Math.ceil(deltas.size() * (percent / 100.0)));
        long sum = 0;
        for (int i = 0; i < count; i++) sum += deltas.get(i);
        double avgDelta = sum / (double) count;
        return 1_000_000_000.0 / avgDelta;
    }

    private record FrameSample(long timestamp, long deltaNanos) {
    }
}
