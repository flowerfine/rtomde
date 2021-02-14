package cn.sliew.rtomde.container.core.limits;

public class ConcurrencyLimit {

    private final Integer maxConcurrent;

    public ConcurrencyLimit(Integer maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    public Integer getMaxConcurrent() {
        return maxConcurrent;
    }

}
