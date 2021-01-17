package cn.sliew.rtomde.executor.bytecode;

import java.util.List;

public final class BeanDescriptor {

    private final String name;
    private final Class<?> type;
    private final List<PropertyDescriptor> properties;

    public BeanDescriptor(String name, Class<?> type, List<PropertyDescriptor> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public List<PropertyDescriptor> getProperties() {
        return properties;
    }
}
