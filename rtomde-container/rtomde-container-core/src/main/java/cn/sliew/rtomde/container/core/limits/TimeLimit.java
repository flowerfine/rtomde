package cn.sliew.rtomde.container.core.limits;

import java.time.Duration;

public class TimeLimit {

    private final Duration duration;

    public TimeLimit(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
