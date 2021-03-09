package cn.sliew.rtomde.container.core.limits;

public class ConcurrencyLimitConfig {

    private final Integer min;

    private final Integer max;

    private final Integer std;

    public ConcurrencyLimitConfig(Integer min, Integer max, Integer std) {
        this.min = min;
        this.max = max;
        this.std = std;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getStd() {
        return std;
    }
}
