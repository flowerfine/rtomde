package cn.sliew.rtomde.common.exception;

public class RtomdeException extends RuntimeException {
    
    public RtomdeException() {
    }

    public RtomdeException(String message) {
        super(message);
    }

    public RtomdeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RtomdeException(Throwable cause) {
        super(cause);
    }

    public RtomdeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
