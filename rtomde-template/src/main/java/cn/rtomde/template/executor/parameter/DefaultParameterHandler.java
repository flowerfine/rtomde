package cn.rtomde.template.executor.parameter;

import cn.rtomde.template.executor.ErrorContext;
import cn.rtomde.template.mapping.MappedStatement;
import cn.rtomde.template.mapping.ParameterMapping;
import cn.rtomde.template.mapping.expression.AttributeExpression;
import cn.rtomde.template.type.JdbcType;
import cn.rtomde.template.type.TypeException;
import cn.rtomde.template.type.TypeHandler;
import io.cloudevents.CloudEvent;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DefaultParameterHandler implements ParameterHandler {

    private JdbcType jdbcTypeForNull = JdbcType.OTHER;

    private final MappedStatement ms;

    public DefaultParameterHandler(MappedStatement ms) {
        this.ms = ms;
    }

    @Override
    public void setParameters(PreparedStatement ps, List<ParameterMapping> parameterMappings, CloudEvent event) throws SQLException {
        ErrorContext.instance().activity("setting parameters").object(ms.getId());
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                AttributeExpression expression = parameterMapping.getExpression();
                Object value = event != null ? expression.evaluate(event) : expression.evaluate();
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                JdbcType jdbcType = parameterMapping.getJdbcType();
                if (value == null && jdbcType == null) {
                    jdbcType = jdbcTypeForNull;
                }
                try {
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                } catch (TypeException | SQLException e) {
                    throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ".", e);
                }
            }
        }
    }
}
