package cn.sliew.rtomde.platform.mybatis.type;

import cn.sliew.rtomde.platform.mybatis.exceptions.PersistenceException;

/**
 * @author Clinton Begin
 */
public class TypeException extends PersistenceException {

    private static final long serialVersionUID = 8614420898975117130L;

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
