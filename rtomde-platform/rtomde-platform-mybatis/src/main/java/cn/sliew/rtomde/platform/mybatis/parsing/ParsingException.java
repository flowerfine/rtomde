package cn.sliew.rtomde.platform.mybatis.parsing;

import cn.sliew.rtomde.platform.mybatis.exceptions.PersistenceException;

/**
 * @author Clinton Begin
 */
public class ParsingException extends PersistenceException {
    private static final long serialVersionUID = -176685891441325943L;

    public ParsingException() {
        super();
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
