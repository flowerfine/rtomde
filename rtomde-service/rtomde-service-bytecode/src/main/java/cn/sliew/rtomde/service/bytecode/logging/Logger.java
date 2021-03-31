package cn.sliew.rtomde.service.bytecode.logging;

import java.util.function.Supplier;

/**
 * Wrapper of {@link cn.sliew.milky.log.Logger}, allow log with lambda expressions.
 */
public class Logger {

    private final cn.sliew.milky.log.Logger log;

    Logger(cn.sliew.milky.log.Logger log) {
        this.log = log;
    }

    public void error(Supplier<String> s, Throwable e) {
        log.error(s.get(), e);
    }

    public void error(Supplier<String> s) {
        log.error(s.get());
    }

    public void warn(Supplier<String> s) {
        log.warn(s.get());
    }

    public void debug(Supplier<String> s) {
        if (log.isDebugEnabled()) {
            log.debug(s.get());
        }
    }

    public void trace(Supplier<String> s) {
        if (log.isTraceEnabled()) {
            log.trace(s.get());
        }
    }

}
