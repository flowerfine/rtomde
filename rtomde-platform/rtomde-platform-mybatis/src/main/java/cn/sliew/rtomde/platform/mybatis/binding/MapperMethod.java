package cn.sliew.rtomde.platform.mybatis.binding;

import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.mapping.ParameterMapping;
import cn.sliew.rtomde.platform.mybatis.reflection.MetaObject;
import cn.sliew.rtomde.platform.mybatis.reflection.ParamNameResolver;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.RowBounds;
import cn.sliew.rtomde.platform.mybatis.session.SqlSession;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

public class MapperMethod {

    private final MethodSignature method;

    public MapperMethod(Configuration config, String id) {
        this.method = new MethodSignature(config, id);
    }

    public Object execute(SqlSession sqlSession, String id, Object[] args) {
        return executeForMany(sqlSession, id, args);
    }

    private <E> Object executeForMany(SqlSession sqlSession, String id, Object[] args) {
        List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectList(id, param, rowBounds);
        } else {
            result = sqlSession.selectList(id, param);
        }
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <E> Object convertToArray(List<E> list) {
        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[]) array);
        }
    }

    public static class MethodSignature {

        private final Class<?> returnType;
        private final Integer rowBoundsIndex;
        private final ParamNameResolver paramNameResolver;

        public MethodSignature(Configuration configuration, String id) {
            this.returnType = List.class;
            this.rowBoundsIndex = getUniqueParamIndex(configuration, id, RowBounds.class);
            this.paramNameResolver = new ParamNameResolver(configuration, id);
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            return paramNameResolver.getNamedParams(args);
        }

        public boolean hasRowBounds() {
            return rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        public Class<?> getReturnType() {
            return returnType;
        }

        private Integer getUniqueParamIndex(Configuration configuration, String id, Class<?> paramType) {
            Integer index = null;
            MappedStatement mappedStatement = configuration.getMappedStatement(id);
            List<ParameterMapping> parameterMappings = mappedStatement.getParameterMap().getParameterMappings();

            for (int i = 0; i < parameterMappings.size(); i++) {
                if (paramType.isAssignableFrom(parameterMappings.get(0).getJavaType())) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new RuntimeException(id + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }
    }

    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }
}