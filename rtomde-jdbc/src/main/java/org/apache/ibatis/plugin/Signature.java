package org.apache.ibatis.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that indicate the method signature.
 *
 * @see Intercepts
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Signature {
    /**
     * Returns the java type.
     *
     * @return the java type
     */
    Class<?> type();

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    String method();

    /**
     * Returns java types for method argument.
     *
     * @return java types for method argument
     */
    Class<?>[] args();
}
