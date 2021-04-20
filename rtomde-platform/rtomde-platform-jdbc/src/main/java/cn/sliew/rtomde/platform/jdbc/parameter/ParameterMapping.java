package cn.sliew.rtomde.platform.jdbc.parameter;

import cn.sliew.rtomde.platform.jdbc.type.JdbcType;
import cn.sliew.rtomde.platform.jdbc.type.TypeHandler;

public class ParameterMapping {

    private final String property;
    private final Class<?> javaType = Object.class;
    private final JdbcType jdbcType;
    private final TypeHandler<?> typeHandler;
    private final String expression;

    public ParameterMapping(String property, JdbcType jdbcType, TypeHandler<?> typeHandler, String expression) {
        this.property = property;
        this.jdbcType = jdbcType;
        this.typeHandler = typeHandler;
        this.expression = expression;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public String getExpression() {
        return expression;
    }

}
