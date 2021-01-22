package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.common.xml.ParseException;

public class IncompleteElementException extends ParseException {

    public IncompleteElementException() {
        super();
    }

    public IncompleteElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompleteElementException(String message) {
        super(message);
    }

    public IncompleteElementException(Throwable cause) {
        super(cause);
    }

    public IncompleteElementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
