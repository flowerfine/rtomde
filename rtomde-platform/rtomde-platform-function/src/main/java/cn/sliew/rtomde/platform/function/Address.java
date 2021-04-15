package cn.sliew.rtomde.platform.function;

import java.io.Serializable;
import java.util.Objects;

public final class Address implements Serializable {

    private static final long serialVersionUID = 539531386824395899L;

    private final FunctionType type;
    private final String id;

    public Address(FunctionType type, String id) {
        this.type = Objects.requireNonNull(type);
        this.id = Objects.requireNonNull(id);
    }

    public FunctionType type() {
        return type;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(type, address.type) &&
                Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    @Override
    public String toString() {
        return String.format("Address(%s, %s, %s)", type.namespace(), type.name(), id);
    }
}
