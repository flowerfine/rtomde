package cn.sliew.rtomde.datasource.type;

/**
 * 表示请求参数和返回结果的类型
 */
public interface Type {

    /**
     * Gets the name of this type which must be case insensitive globally unique.
     * The name of a user defined type must be a legal identifier in Trino.
     */
    TypeSignature getTypeSignature();

    /**
     * Returns the name of this type that should be displayed to end-users.
     */
    default String getName() {
        return getTypeSignature().getName();
    }

    /**
     * Gets the Java class type used to represent this value on the stack during
     * expression execution.
     * <p>
     * Currently, this must be boolean, long, double, Slice or Block.
     */
    Class<?> getJavaType();
}
