package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.datasource.type.Type;

public class ParameterMapping {

    private String property;
    private Class<?> javaType = Object.class;
    private Type jdbcType;
    private TypeHandler<?> typeHandler;

    private ParameterMapping() {
    }
}
