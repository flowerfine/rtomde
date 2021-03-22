package cn.sliew.rtomde.platform.mybatis.exceptions;

import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class PersistenceException extends IbatisException {

    private static final long serialVersionUID = -7537395265357977271L;

    private MappedStatement mappedStatement;

    public PersistenceException() {
        super();
    }

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public Optional<MappedStatement> getMappedStatement() {
        return Optional.ofNullable(mappedStatement);
    }

    public void setMappedStatement(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }
}
