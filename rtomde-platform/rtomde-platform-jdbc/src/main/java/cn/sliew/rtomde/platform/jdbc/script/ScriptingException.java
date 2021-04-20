package cn.sliew.rtomde.platform.jdbc.script;

public class ScriptingException extends RuntimeException {

    public ScriptingException() {
    }

    public ScriptingException(String message) {
        super(message);
    }

    public ScriptingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptingException(Throwable cause) {
        super(cause);
    }

    protected ScriptingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
