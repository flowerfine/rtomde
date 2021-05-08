package cn.sliew.rtomde.config.metadata;

import java.io.Serializable;

public class MethodMetadata implements Serializable {

    private static final long serialVersionUID = -9011256907702123313L;

    /**
     * Method id.
     */
    private final String id;

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

    public MethodMetadata(String id, String version, RecordMetadata parameter, RecordMetadata result) {
        this.id = id;
        this.version = version;
        this.parameter = parameter;
        this.result = result;
    }

    public String getId() {
        return id;
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
    public String toString() {
        return "MethodMetadata{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", parameter=" + parameter +
                ", result=" + result +
                '}';
    }
}
