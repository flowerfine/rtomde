package cn.sliew.rtomde.bind;

public class ResultMapping {

    private final String property;
    private final String javaType;
    private final String column;
    private final String columnType;
    private final String typeHandler;

    private ResultMapping(String property, String javaType, String column, String columnType, String typeHandler) {
        this.property = property;
        this.javaType = javaType;
        this.column = column;
        this.columnType = columnType;
        this.typeHandler = typeHandler;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String property;
        private String javaType;
        private String column;
        private String columnType;
        private String typeHandler;

        private Builder() {
        }

        public Builder property(String property) {
            this.property = property;
            return this;
        }

        public Builder javaType(String javaType) {
            this.javaType = javaType;
            return this;
        }

        public Builder column(String column) {
            this.column = column;
            return this;
        }

        public Builder columnType(String columnType) {
            this.columnType = columnType;
            return this;
        }

        public Builder typeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public ResultMapping build() {
            return new ResultMapping(property, javaType, column, columnType, typeHandler);
        }
    }

    public String getProperty() {
        return property;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getColumn() {
        return column;
    }

    public String getColumnType() {
        return columnType;
    }

    public String getTypeHandler() {
        return typeHandler;
    }
}
