package cn.sliew.rtomde.bind;

import cn.sliew.rtomde.datasource.DataSource;

import static java.util.Objects.requireNonNull;

public class Environment {

    private final String application;
    private final String id;
    private final DataSource dataSource;

    private Environment(String application, String id, DataSource dataSource) {
        this.application = application;
        this.id = requireNonNull(id, "Parameter 'id' must not be null");
        this.dataSource = requireNonNull(dataSource, "Parameter 'dataSource' must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String application;
        private String id;
        private DataSource dataSource;

        private Builder() {
        }

        public Builder application(String application) {
            this.application = application;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Environment build() {
            return new Environment(application, id, dataSource);
        }
    }
}
