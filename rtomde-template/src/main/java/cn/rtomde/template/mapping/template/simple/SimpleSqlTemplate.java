package cn.rtomde.template.mapping.template.simple;

import cn.rtomde.template.mapping.BoundSql;
import cn.rtomde.template.mapping.ParameterMap;
import cn.rtomde.template.mapping.ParameterMapping;
import cn.rtomde.template.mapping.SqlTemplate;
import cn.rtomde.template.mapping.expression.AttributeExpression;
import cn.sliew.milky.common.parse.placeholder.PropertyPlaceholder;
import cn.sliew.milky.log.Logger;
import io.cloudevents.CloudEvent;

import java.util.List;
import java.util.Optional;

public class SimpleSqlTemplate implements SqlTemplate {

    private final Logger logger;
    private final String sql;
    private final List<ParameterMapping> parameterMappings;
    private final ParameterMap parameterMap;

    public SimpleSqlTemplate(Logger logger, String sql, List<ParameterMapping> parameterMappings, ParameterMap parameterMap) {
        this.logger = logger;
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterMap = parameterMap;
    }

    @Override
    public BoundSql bind(CloudEvent event) {
        PropertyPlaceholder propertyPlaceholder = new PropertyPlaceholder(logger, "${", "}");
        ParameterResolver parameterResolver = new ParameterResolver(event, parameterMap);
        String rawSql = propertyPlaceholder.replacePlaceholders(sql, parameterResolver);
        return new BoundSql(rawSql, parameterMappings, event);
    }

    private class ParameterResolver implements PropertyPlaceholder.PlaceholderResolver {

        private final CloudEvent event;
        private final ParameterMap parameterMap;

        public ParameterResolver(CloudEvent event, ParameterMap parameterMap) {
            this.event = event;
            this.parameterMap = parameterMap;
        }

        @Override
        public Optional<String> resolvePlaceholder(String placeholderName) {
            if (parameterMap.containsParameter(placeholderName)) {
                ParameterMapping parameterMapping = parameterMap.getParameterMapping(placeholderName);
                AttributeExpression expression = parameterMapping.getExpression();
                Object value = event != null ? expression.evaluate(event) : expression.evaluate();
                if (value == null) {
                    return Optional.empty();
                }
                return Optional.of(value.toString());
            }
            return Optional.empty();
        }

        @Override
        public boolean shouldIgnoreMissing(String placeholderName) {
            return false;
        }

        @Override
        public boolean shouldRemoveMissingPlaceholder(String placeholderName) {
            return false;
        }
    }
}
