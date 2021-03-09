package cn.sliew.rtomde.container.core.limits;

public class ActionLimits {

    private final MemoryLimit memory;

    private final ConcurrencyLimit concurrency;

    private final TimeLimit timeout;

    public ActionLimits(MemoryLimit memory, ConcurrencyLimit concurrency, TimeLimit timeout) {
        this.memory = memory;
        this.concurrency = concurrency;
        this.timeout = timeout;
    }

    public MemoryLimit getMemory() {
        return memory;
    }

    public ConcurrencyLimit getConcurrency() {
        return concurrency;
    }

    public TimeLimit getTimeout() {
        return timeout;
    }
}
