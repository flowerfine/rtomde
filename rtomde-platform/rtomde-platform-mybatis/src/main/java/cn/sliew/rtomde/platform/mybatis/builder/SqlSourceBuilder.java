package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMapping;
import cn.sliew.rtomde.platform.mybatis.mapping.SqlSource;
import cn.sliew.rtomde.platform.mybatis.parsing.GenericTokenParser;
import cn.sliew.rtomde.platform.mybatis.parsing.TokenHandler;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaClass;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class SqlSourceBuilder extends BaseBuilder {

    private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(MybatisApplicationOptions application) {
        super(application);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(application, parameterType, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql;
        MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
        if (platform.isShrinkWhitespacesInSql()) {
            sql = parser.parse(removeExtraWhitespaces(originalSql));
        } else {
            sql = parser.parse(originalSql);
        }
        return new StaticSqlSource(application, sql, handler.getParameterMappings());
    }

    public static String removeExtraWhitespaces(String original) {
        StringTokenizer tokenizer = new StringTokenizer(original);
        StringBuilder builder = new StringBuilder();
        boolean hasMoreTokens = tokenizer.hasMoreTokens();
        while (hasMoreTokens) {
            builder.append(tokenizer.nextToken());
            hasMoreTokens = tokenizer.hasMoreTokens();
            if (hasMoreTokens) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(MybatisApplicationOptions application, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(application);
            this.parameterType = parameterType;
            MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
            this.metaParameters = platform.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        private ParameterMapping buildParameterMapping(String content) {
            Map<String, String> propertiesMap = parseParameterMapping(content);
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
                propertyType = metaParameters.getGetterType(property);
            } else if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
                propertyType = java.sql.ResultSet.class;
            } else if (property == null || Map.class.isAssignableFrom(parameterType)) {
                propertyType = Object.class;
            } else {
                MybatisPlatformOptions platform = (MybatisPlatformOptions) application.getPlatform();
                MetaClass metaClass = MetaClass.forClass(parameterType, platform.getReflectorFactory());
                if (metaClass.hasGetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            }
            ParameterMapping.Builder builder = ParameterMapping.builder(application).property(property).javaType(propertyType);
            Class<?> javaType = propertyType;
            String typeHandlerAlias = null;
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if ("javaType".equals(name)) {
                    javaType = resolveClass(value);
                    builder.javaType(javaType);
                } else if ("jdbcType".equals(name)) {
                    builder.jdbcType(resolveJdbcType(value));
                } else if ("typeHandler".equals(name)) {
                    typeHandlerAlias = value;
                } else if ("jdbcTypeName".equals(name)) {
                    builder.jdbcTypeName(value);
                } else if ("property".equals(name)) {
                    // Do Nothing
                } else if ("expression".equals(name)) {
                    throw new BuilderException("Expression based parameters are not supported yet");
                } else {
                    throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + PARAMETER_PROPERTIES);
                }
            }
            if (typeHandlerAlias != null) {
                builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
            }
            return builder.build();
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (BuilderException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new BuilderException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
            }
        }
    }

}
