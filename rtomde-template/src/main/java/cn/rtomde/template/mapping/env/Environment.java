package cn.rtomde.template.mapping.env;

import cn.rtomde.template.mapping.ResultSetType;
import cn.rtomde.template.type.TypeAliasRegistry;
import cn.rtomde.template.type.TypeHandlerRegistry;

public class Environment {

    private Integer timeout;
    private Integer etchSize = 1000;
    private ResultSetType resultSetType = ResultSetType.FORWARD_ONLY;

    private final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getEtchSize() {
        return etchSize;
    }

    public ResultSetType getResultSetType() {
        return resultSetType;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }
}
