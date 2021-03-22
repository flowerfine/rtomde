package cn.sliew.rtomde.platform.mybatis.executor;

public class ErrorContext {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final ThreadLocal<ErrorContext> LOCAL = ThreadLocal.withInitial(ErrorContext::new);

    private ErrorContext stored;
    private String resource;
    private String activity;
    private String object;
    private String message;
    private String sql;
    private Throwable cause;

    private ErrorContext() {
    }

    public static ErrorContext instance() {
        return LOCAL.get();
    }

    public ErrorContext store() {
        ErrorContext newContext = new ErrorContext();
        newContext.stored = this;
        LOCAL.set(newContext);
        return LOCAL.get();
    }

    public ErrorContext recall() {
        if (stored != null) {
            LOCAL.set(stored);
            stored = null;
        }
        return LOCAL.get();
    }

    public ErrorContext resource(String resource) {
        this.resource = resource;
        return this;
    }

    public ErrorContext activity(String activity) {
        this.activity = activity;
        return this;
    }

    public ErrorContext object(String object) {
        this.object = object;
        return this;
    }

    public ErrorContext message(String message) {
        this.message = message;
        return this;
    }

    public ErrorContext sql(String sql) {
        this.sql = sql;
        return this;
    }

    public ErrorContext cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public ErrorContext reset() {
        resource = null;
        activity = null;
        object = null;
        message = null;
        sql = null;
        cause = null;
        LOCAL.remove();
        return this;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();

        // message
        if (this.message != null) {
            description.append(LINE_SEPARATOR);
            description.append("### ");
            description.append(this.message);
        }

        // resource
        if (resource != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may exist in ");
            description.append(resource);
        }

        // object
        if (object != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error may involve ");
            description.append(object);
        }

        // activity
        if (activity != null) {
            description.append(LINE_SEPARATOR);
            description.append("### The error occurred while ");
            description.append(activity);
        }

        // sql
        if (sql != null) {
            description.append(LINE_SEPARATOR);
            description.append("### SQL: ");
            description.append(sql.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').trim());
        }

        // cause
        if (cause != null) {
            description.append(LINE_SEPARATOR);
            description.append("### Cause: ");
            description.append(cause.toString());
        }

        return description.toString();
    }

}
