package cn.sliew.rtomde.platform.jdbc.result;

public class ResultMapException extends RuntimeException {

    private static final long serialVersionUID = -7786952411574090864L;

    public ResultMapException() {
        super();
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