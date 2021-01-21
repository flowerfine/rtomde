package cn.sliew.rtomde.bind;

public class ParameterMapping {

    private String property;
    private Class<?> javaType = Object.class;
    private ColumnType jdbcType;
    private TypeHandler<?> typeHandler;

    private ParameterMapping() {
    }
}
