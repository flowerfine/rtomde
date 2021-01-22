package cn.sliew.rtomde.bind;

/**
 * 参数的映射
 */
public class ParameterMapping {

    private final String property;
    private final String javaType;
    private final String columnType;
    /**
     * 将每个jdbcType和javaType进行转换
     */
    private final String typeHandler;

    private ParameterMapping(String property, String javaType, String columnType, String typeHandler) {
        this.property = property;
        this.javaType = javaType;
        this.columnType = columnType;
        this.typeHandler = typeHandler;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String property;
        private String javaType;
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

        public Builder columnType(String columnType) {
            this.columnType = columnType;
            return this;
        }

        public Builder typeHandler(String typeHandler) {
            this.typeHandler = typeHandler;
            return this;
        }

        public ParameterMapping build() {
            return new ParameterMapping(property, javaType, columnType, typeHandler);
        }
    }
}
