package cn.rtomde.template.mapping.template;

import cn.rtomde.template.exceptions.PersistenceException;

public class TemplateException extends PersistenceException {

    public TemplateException() {
    }

    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(Throwable cause) {
        super(cause);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
