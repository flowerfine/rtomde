package cn.sliew.rtomde.config.metadata;

import java.io.Serializable;
import java.util.Objects;

public final class FieldMetadata implements Comparable<FieldMetadata>, Serializable {

    /**
     * Index which indicates result position.
     */
    private final int index;

    /**
     * Result name
     */
    private final String name;

    /**
     * Result type name
     */
    private final String typeName;

    public FieldMetadata(int index, String name, String typeName) {
        this.index = index;
        this.name = name;
        this.typeName = typeName;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public int compareTo(FieldMetadata o) {
        return index - o.index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldMetadata that = (FieldMetadata) o;
        return index == that.index &&
                Objects.equals(name, that.name) &&
                Objects.equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, name, typeName);
    }

    @Override
    public String toString() {
        return String.format("<field index=\"%s\" name=\"%s\" type=\"%s\"/>", index, name, typeName);
    }

}
