package cn.sliew.rtomde.container.core.limits;

public class MemoryLimit {

    private final Integer megabytes;

    public MemoryLimit(Integer megabytes) {
        this.megabytes = megabytes;
    }

    public Integer getMegabytes() {
        return megabytes;
    }
}
