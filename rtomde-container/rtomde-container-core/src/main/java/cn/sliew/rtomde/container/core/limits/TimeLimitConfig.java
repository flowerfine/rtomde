package cn.sliew.rtomde.container.core.limits;

import java.time.Duration;

public class TimeLimitConfig {

    private final Duration min;

    private final Duration max;

    private final Duration std;

    public TimeLimitConfig(Duration min, Duration max, Duration std) {
        this.min = min;
        this.max = max;
        this.std = std;
    }

    public Duration getMin() {
        return min;
    }

    public Duration getMax() {
        return max;
    }

    public Duration getStd() {
        return std;
    }
}
