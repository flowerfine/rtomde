package cn.sliew.rtomde.common.reflect;

import cn.sliew.rtomde.common.exception.RtomdeException;

public class ReflectionException extends RtomdeException {

    private static final long serialVersionUID = 7642570221267566591L;

    public ReflectionException() {
        super();
    }

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(Throwable cause) {
        super(cause);
    }

    public ReflectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
