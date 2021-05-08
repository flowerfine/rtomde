package cn.sliew.rtomde.config.metadata;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;

public class RecordMetadata implements Serializable {

    private static final long serialVersionUID = 2732104439700495476L;

    /**
     * Record name.
     */
    private final String name;

    /**
     * Fields organized by position.
     */
    private final FieldMetadata[] fieldByPosition;

    /**
     * Mapping from field names to positions.
     */
    private final LinkedHashMap<String, Integer> positionByName;

    public RecordMetadata(String name, FieldMetadata[] fieldByPosition) {
        this.name = name;
        this.fieldByPosition = fieldByPosition;
        this.positionByName = new LinkedHashMap<>();
        init(fieldByPosition);
    }

    private void init(FieldMetadata[] fields) {
        if (fields != null) {
            for (int i = 0; i < fields.length; i++) {
                FieldMetadata field = fields[i];
                this.positionByName.put(field.getName(), field.getIndex());
            }
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordMetadata that = (RecordMetadata) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(fieldByPosition, that.fieldByPosition) &&
                Objects.equals(positionByName, that.positionByName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, positionByName);
        result = 31 * result + Arrays.hashCode(fieldByPosition);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<record id=\"");
        sb.append(name);
        sb.append("\">");

        if (fieldByPosition != null) {
            sb.append("\n");
            for (FieldMetadata field : fieldByPosition) {
                sb.append("    ");
                sb.append(field.toString());
                sb.append("\n");
            }
        }

        sb.append("</record>");
        return sb.toString();
    }

}
