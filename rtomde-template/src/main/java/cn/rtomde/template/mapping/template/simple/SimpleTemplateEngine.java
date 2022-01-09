package cn.rtomde.template.mapping.template.simple;

import cn.rtomde.template.mapping.ParameterMap;
import cn.rtomde.template.mapping.ParameterMapping;
import cn.rtomde.template.mapping.SqlTemplate;
import cn.rtomde.template.mapping.template.TemplateContext;
import cn.rtomde.template.mapping.template.TemplateEngine;
import cn.sliew.milky.common.parse.placeholder.PropertyPlaceholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleTemplateEngine implements TemplateEngine {

    @Override
    public SqlTemplate create(TemplateContext context, String template) {
        PropertyPlaceholder propertyPlaceholder = new PropertyPlaceholder(context.getLogger(), "#{", "}");
        ParameterMappingResolver parameterMappingResolver = new ParameterMappingResolver(context.getParameterMap());
        String rawSql = propertyPlaceholder.replacePlaceholders(template, parameterMappingResolver);
        return new SimpleSqlTemplate(context.getLogger(), rawSql, parameterMappingResolver.parameterMappings, context.getParameterMap());
    }

    private class ParameterMappingResolver implements PropertyPlaceholder.PlaceholderResolver {

        private final ParameterMap parameterMap;
        private final List<ParameterMapping> parameterMappings = new ArrayList<>();

        private ParameterMappingResolver(ParameterMap parameterMap) {
            this.parameterMap = parameterMap;
        }

        @Override
        public Optional<String> resolvePlaceholder(String placeholderName) {
            parameterMappings.add(buildParameterMapping(placeholderName));
            return Optional.of("?");
        }

        @Override
        public boolean shouldIgnoreMissing(String placeholderName) {
            return false;
        }

        @Override
        public boolean shouldRemoveMissingPlaceholder(String placeholderName) {
            return true;
        }

        private ParameterMapping buildParameterMapping(String content) {
            Map<String, String> propertiesMap = parseParameterMapping(content);
            String property = propertiesMap.get("property");
            if (parameterMap.containsParameter(property)) {
                return parameterMap.getParameterMapping(property);
            }
            throw new RuntimeException("not found property for " + content);
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
            }
        }
    }
}
