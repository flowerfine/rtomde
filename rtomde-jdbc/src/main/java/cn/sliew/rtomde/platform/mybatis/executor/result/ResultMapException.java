package cn.sliew.rtomde.platform.mybatis.executor.result;

import cn.sliew.rtomde.platform.mybatis.exceptions.PersistenceException;

public class ResultMapException extends PersistenceException {
    private static final long serialVersionUID = 3270932060569707623L;

    public ResultMapException() {
    }

    public ResultMapException(String message) {
        super(message);
    }

    public ResultMapException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultMapException(Throwable cause) {
        super(cause);
    }
}
