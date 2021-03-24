package org.mybatis.logging;

import cn.sliew.rtomde.platform.mybatis.logging.LogFactory;

/**
 * LoggerFactory is a wrapper around {@link LogFactory} to support {@link Logger}.
 */
public class LoggerFactory {

    private LoggerFactory() {
        // NOP
    }

    public static Logger getLogger(Class<?> aClass) {
        return new Logger(LogFactory.getLog(aClass));
    }

    public static Logger getLogger(String logger) {
        return new Logger(LogFactory.getLog(logger));
    }

}
