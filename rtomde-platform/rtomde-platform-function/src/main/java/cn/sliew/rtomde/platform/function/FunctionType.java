package cn.sliew.rtomde.platform.function;

import java.io.Serializable;
import java.util.Objects;

public final class FunctionType implements Serializable {

    private static final long serialVersionUID = 4897060349247934788L;

    private final String namespace;
    private final String type;

    public FunctionType(String namespace, String type) {
        this.namespace = Objects.requireNonNull(namespace);
        this.type = Objects.requireNonNull(type);
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionType that = (FunctionType) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, type);
    }

    @Override
    public String toString() {
        return String.format("FunctionType(%s, %s)", namespace, type);
    }
}
