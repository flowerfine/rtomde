package org.apache.ibatis.cache;

import java.util.Objects;
import java.util.Properties;

public class LettuceWrapper {

    private final String id;
    private final String type;
    private final Properties props;

    private LettuceWrapper(String id, String type, Properties props) {
        this.id = id;
        this.type = type;
        this.props = props;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String id;
        private String type;
        private Properties props;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder props(Properties props) {
            this.props = props;
            return this;
        }

        public LettuceWrapper build() {
            return new LettuceWrapper(id, type, props);
        }

    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Properties getProps() {
        return props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LettuceWrapper that = (LettuceWrapper) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type) &&
                Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, props);
    }

    @Override
    public String toString() {
        return "LettuceWrapper{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", props=" + props +
                '}';
    }
}
