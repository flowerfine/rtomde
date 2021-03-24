package org.apache.ibatis.type;

import java.lang.annotation.*;

/**
 * The annotation that specify alias name.
 *
 * <p>
 * <b>How to use:</b>
 * <pre>
 * &#064;Alias("Email")
 * public class UserEmail {
 *   // ...
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {
    /**
     * Return the alias name.
     *
     * @return the alias name
     */
    String value();
}
