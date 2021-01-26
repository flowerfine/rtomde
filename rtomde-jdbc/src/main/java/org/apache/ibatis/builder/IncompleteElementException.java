package org.apache.ibatis.builder;

public class IncompleteElementException extends BuilderException {
    private static final long serialVersionUID = -3697292286890900315L;

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

}
