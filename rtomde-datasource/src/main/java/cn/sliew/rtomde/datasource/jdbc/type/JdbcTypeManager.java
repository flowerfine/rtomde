package cn.sliew.rtomde.datasource.jdbc.type;

import cn.sliew.rtomde.datasource.type.Type;
import cn.sliew.rtomde.datasource.type.TypeHandler;
import cn.sliew.rtomde.datasource.type.TypeManager;
import cn.sliew.rtomde.datasource.type.TypeSignature;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JdbcTypeManager implements TypeManager {

    private static final ConcurrentMap<TypeSignature, Type> typeSignatureCaches = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class, Type> clazzCaches = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Type, TypeHandler> typeHandlerCaches = new ConcurrentHashMap<>();

    static {
        typeSignatureCaches.put(ArrayJdbcType.INSTANCE.getTypeSignature(), ArrayJdbcType.INSTANCE);
        clazzCaches.put(ArrayJdbcType.INSTANCE.getJavaType(), ArrayJdbcType.INSTANCE);
        typeHandlerCaches.put(ArrayJdbcType.INSTANCE, new ArrayTypeHandler());
    }


    @Override
    public Type getType(TypeSignature signature) {
        Type type = typeSignatureCaches.get(signature);
        if (type == null) {
            throw new IllegalStateException("unknown type for " + signature.toString());
        }
        return type;
    }

    @Override
    public Type getType(Class<?> clazz) {
        Type type = clazzCaches.get(clazz);
        if (type == null) {
            throw new IllegalStateException("unknown type for " + clazz.getName());
        }
        return type;
    }

    @Override
    public TypeHandler getTypeHandler(Type type) {
        TypeHandler typeHandler = typeHandlerCaches.get(type);
        if (typeHandler == null) {
            throw new IllegalStateException("unknown type for " + typeHandler);
        }
        return typeHandler;
    }
}
