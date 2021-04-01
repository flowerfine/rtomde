package cn.sliew.rtomde.platform.mybatis.exceptions;

import cn.sliew.rtomde.platform.mybatis.executor.ErrorContext;

public final class ExceptionFactory {

    private ExceptionFactory() {
        // Prevent Instantiation
    }

    public static RuntimeException wrapException(String message, Exception e) {
        return new PersistenceException(ErrorContext.instance().message(message).cause(e).toString(), e);
    }

}
