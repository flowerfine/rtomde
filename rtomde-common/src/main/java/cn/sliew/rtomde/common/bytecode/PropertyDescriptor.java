package cn.sliew.rtomde.common.bytecode;

public final class PropertyDescriptor {

    private final String property;
    private final Class<?> javaType;

    public PropertyDescriptor(String property, Class<?> javaType) {
        this.property = property;
        this.javaType = javaType;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }
}
