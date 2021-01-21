package cn.sliew.rtomde.parse;

import cn.sliew.rtomde.common.exception.RtomdeException;

public class ParseException extends RtomdeException {

    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    public ParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
