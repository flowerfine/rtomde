package cn.sliew.rtomde.config.metadata;

import java.io.Serializable;
import java.util.Objects;

public class MethodMetadata implements Serializable {

    private static final long serialVersionUID = -9011256907702123313L;

    /**
     * Method id.
     */
    private final String name;

    /**
     * Method version.
     */
    private final String version;

    /**
     * Method paramter, nullable.
     */
    private final RecordMetadata parameter;

    /**
     * Method result.
     */
    private final RecordMetadata result;

    public MethodMetadata(String name, String version, RecordMetadata parameter, RecordMetadata result) {
        this.name = name;
        this.version = version;
        this.parameter = parameter;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public RecordMetadata getParameter() {
        return parameter;
    }

    public RecordMetadata getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodMetadata that = (MethodMetadata) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(parameter, that.parameter) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, parameter, result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<method id=\"");
        sb.append(name);
        sb.append("\" version=\"");
        sb.append(version);
        if (parameter != null) {
            sb.append("\" parameter=\"");
            sb.append(parameter.getName());
        }
        sb.append("\" result=\"");
        sb.append(result.getName());
        sb.append("\"/>");
        return sb.toString();
    }
}
