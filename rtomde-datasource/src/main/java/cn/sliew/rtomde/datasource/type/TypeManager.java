package cn.sliew.rtomde.datasource.type;

public interface TypeManager {

    /**
     * Gets the type with the specified signature.
     */
    Type getType(TypeSignature signature);

    Type getType(Class<?> clazz);

    TypeHandler getTypeHandler(Type type);
}
