package cn.sliew.rtomde.platform.function;

import java.io.Serializable;
import java.util.Objects;

public final class IngressType implements Serializable {

    private static final long serialVersionUID = -489271732770071843L;

    private final String namespace;

    private final String type;

    public IngressType(String namespace, String type) {
        this.namespace = namespace;
        this.type = type;
    }

    public String namespace() {
        return namespace;
    }

    public String type() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngressType that = (IngressType) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, type);
    }

    @Override
    public String toString() {
        return String.format("IngressType(%s, %s)", namespace, type);
    }
}
