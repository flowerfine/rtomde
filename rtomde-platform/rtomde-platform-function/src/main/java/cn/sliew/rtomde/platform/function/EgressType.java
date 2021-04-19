package cn.sliew.rtomde.platform.function;

import java.io.Serializable;
import java.util.Objects;

public final class EgressType implements Serializable {

    private static final long serialVersionUID = 6805341794237632355L;

    private final String namespace;
    private final String type;

    public EgressType(String namespace, String type) {
        this.namespace = Objects.requireNonNull(namespace);
        this.type = Objects.requireNonNull(type);
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
        EgressType that = (EgressType) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, type);
    }

    @Override
    public String toString() {
        return String.format("EgressType(%s, %s)", namespace, type);
    }
}
