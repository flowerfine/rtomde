package cn.sliew.rtomde.platform.jdbc.type;

public class TypeException extends RuntimeException {

    private static final long serialVersionUID = 4866502322367673025L;

    public TypeException() {
        super();
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(Throwable cause) {
        super(cause);
    }

}
