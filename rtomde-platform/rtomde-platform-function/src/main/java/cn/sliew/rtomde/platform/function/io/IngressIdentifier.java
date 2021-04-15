package cn.sliew.rtomde.platform.function.io;

import java.io.Serializable;
import java.util.Objects;

public final class IngressIdentifier<T> implements Serializable {

    private static final long serialVersionUID = -5186922059735448246L;

    private final String namespace;
    private final String name;
    private final Class<T> producedType;

    public IngressIdentifier(Class<T> producedType, String namespace, String name) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.producedType = Objects.requireNonNull(producedType);
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    public Class<T> producedType() {
        return producedType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngressIdentifier<?> that = (IngressIdentifier<?>) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(name, that.name) &&
                Objects.equals(producedType, that.producedType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, producedType);
    }

    @Override
    public String toString() {
        return String.format("IngressIdentifier(%s, %s, %s)", namespace, name, producedType);
    }
}