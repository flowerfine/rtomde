package cn.sliew.rtomde.executor.bytecode;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeanGeneratorTest {

    @Test
    public void testMakeBean() throws Exception {
        try (BeanGenerator bg = BeanGenerator.newInstance()) {
            bg.className("cn.sliew.rtomde.executor.SysUser");
            bg.setgetter("id", Long.class);
            bg.setgetter("deleted", Boolean.class);
            bg.setgetter("isRed", Boolean.class);
            bg.setgetter("isBlack", Integer.class);
            Class<?> clazz = bg.toClass();
            assertEquals("cn.sliew.rtomde.executor.SysUser", clazz.getName());

            Method getIdMethod = clazz.getMethod("getId");
            Method setIdMethod = clazz.getMethod("setId", Long.class);
            Method isDeletedMethod = clazz.getMethod("isDeleted");
            Method setDeletedMethod = clazz.getMethod("setDeleted", Boolean.class);
            Object user = clazz.newInstance();
            setIdMethod.invoke(user, 100L);
            setDeletedMethod.invoke(user, false);
            assertEquals(100L, getIdMethod.invoke(user));
            assertEquals(false, isDeletedMethod.invoke(user));
        }

    }
}
