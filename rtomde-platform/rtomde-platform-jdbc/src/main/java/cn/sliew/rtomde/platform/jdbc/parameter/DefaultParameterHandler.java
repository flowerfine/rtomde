//package cn.sliew.rtomde.platform.jdbc.parameter;
//
//import cn.sliew.rtomde.platform.jdbc.script.ScriptBoundResult;
//import cn.sliew.rtomde.platform.jdbc.type.JdbcType;
//import cn.sliew.rtomde.platform.jdbc.type.TypeException;
//import cn.sliew.rtomde.platform.jdbc.type.TypeHandler;
//
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.List;
//
//public class DefaultParameterHandler implements ParameterHandler {
//
//    @Override
//    public void bindParameter(ScriptBoundResult boundScript, PreparedStatement ps) throws SQLException {
//        List<ParameterMapping> parameterMappings = boundScript.getParameterMappings();
//        if (parameterMappings != null) {
//            for (int i = 0; i < parameterMappings.size(); i++) {
//                ParameterMapping parameterMapping = parameterMappings.get(i);
//                    Object value;
//                    String propertyName = parameterMapping.getProperty();
//                Object parameterObject = boundScript.getParameterObject();
//                if (parameterObject == null) {
//                        value = null;
//                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
//                        value = parameterObject;
//                    } else {
//                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
//                        value = metaObject.getValue(propertyName);
//                    }
//                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
//                    JdbcType jdbcType = parameterMapping.getJdbcType();
//                    if (value == null && jdbcType == null) {
//                        jdbcType = configuration.getJdbcTypeForNull();
//                    }
//                    try {
//                        typeHandler.setParameter(ps, i + 1, value, jdbcType);
//                    } catch (TypeException | SQLException e) {
//                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
//                    }
//            }
//        }
//    }
//
//}
