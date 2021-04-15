package cn.sliew.rtomde.datasource;

import cn.sliew.rtomde.common.exception.RtomdeException;

public class DataSourceException extends RtomdeException {

    private static final long serialVersionUID = 3915109251083585394L;

    public DataSourceException() {
        super();
    }

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

    public DataSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
