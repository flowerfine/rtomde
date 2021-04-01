package cn.sliew.rtomde.service.bytecode.logging;

/**
 * LoggerFactory is a wrapper around {@link cn.sliew.milky.log.LoggerFactory} to support {@link Logger}.
 */
public class LoggerFactory {

    private LoggerFactory() {
        // NOP
    }

    public static Logger getLogger(Class<?> aClass) {
        return new Logger(cn.sliew.milky.log.LoggerFactory.getLogger(aClass));
    }

    public static Logger getLogger(String logger) {
        return new Logger(cn.sliew.milky.log.LoggerFactory.getLogger(logger));
    }

}
