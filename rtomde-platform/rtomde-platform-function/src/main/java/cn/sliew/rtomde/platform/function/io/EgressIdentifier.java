package cn.sliew.rtomde.platform.function.io;

import java.io.Serializable;
import java.util.Objects;

public class EgressIdentifier<T> implements Serializable {

    private static final long serialVersionUID = 1840561877597465647L;

    private final String namespace;
    private final String name;
    private final Class<T> consumedType;

    public EgressIdentifier(String namespace, String name, Class<T> consumedType) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.consumedType = Objects.requireNonNull(consumedType);
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    public Class<T> consumedType() {
        return consumedType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EgressIdentifier<?> that = (EgressIdentifier<?>) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(name, that.name) &&
                Objects.equals(consumedType, that.consumedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, consumedType);
    }

    @Override
    public String toString() {
        return String.format("EgressIdentifier(%s, %s, %s)", namespace, name, consumedType);
    }
}
