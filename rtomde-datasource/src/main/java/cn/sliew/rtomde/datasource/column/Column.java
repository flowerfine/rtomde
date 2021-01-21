package cn.sliew.rtomde.datasource.column;

import cn.sliew.rtomde.datasource.type.Type;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;

public class Column {

    private final String name;
    private final Type type;
    private final boolean nullable;
    private final String comment;
    private final String extraInfo;
    private final boolean hidden;
    private final Map<String, Object> properties;

    public Column(String name, Type type) {
        this(name, type, true, null, null, false, emptyMap());
    }

    private Column(String name, Type type, boolean nullable, String comment, String extraInfo, boolean hidden, Map<String, Object> properties) {
        checkArgument(name != null, "name is null");
        requireNonNull(type, "type is null");
        requireNonNull(properties, "properties is null");

        this.name = name.toLowerCase(ENGLISH);
        this.type = type;
        this.comment = comment;
        this.extraInfo = extraInfo;
        this.hidden = hidden;
        this.properties = properties.isEmpty() ? emptyMap() : unmodifiableMap(new LinkedHashMap<>(properties));
        this.nullable = nullable;
    }

    public static class Builder {
        private String name;
        private Type type;
        private boolean nullable = true;
        private Optional<String> comment = Optional.empty();
        private Optional<String> extraInfo = Optional.empty();
        private boolean hidden;
        private Map<String, Object> properties = emptyMap();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = requireNonNull(name, "name is null");
            return this;
        }

        public Builder type(Type type) {
            this.type = requireNonNull(type, "type is null");
            return this;
        }

        public Builder nullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder comment(Optional<String> comment) {
            this.comment = requireNonNull(comment, "comment is null");
            return this;
        }

        public Builder extraInfo(Optional<String> extraInfo) {
            this.extraInfo = requireNonNull(extraInfo, "extraInfo is null");
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = requireNonNull(properties, "properties is null");
            return this;
        }

        public Column build() {
            return new Column(
                    name,
                    type,
                    nullable,
                    comment.orElse(null),
                    extraInfo.orElse(null),
                    hidden,
                    properties);
        }
    }

}
