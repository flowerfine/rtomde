package cn.sliew.rtomde.platform.mybatis.reflection.property;

import cn.sliew.rtomde.platform.mybatis.reflection.Reflector;

import java.lang.reflect.Field;

/**
 * @author Clinton Begin
 */
public final class PropertyCopier {

    private PropertyCopier() {
        // Prevent Instantiation of Static Class
    }

    public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
        Class<?> parent = type;
        while (parent != null) {
            final Field[] fields = parent.getDeclaredFields();
            for (Field field : fields) {
                try {
                    try {
                        field.set(destinationBean, field.get(sourceBean));
                    } catch (IllegalAccessException e) {
                        if (Reflector.canControlMemberAccessible()) {
                            field.setAccessible(true);
                            field.set(destinationBean, field.get(sourceBean));
                        } else {
                            throw e;
                        }
                    }
                } catch (Exception e) {
                    // Nothing useful to do, will only fail on final fields, which will be ignored.
                }
            }
            parent = parent.getSuperclass();
        }
    }

}
