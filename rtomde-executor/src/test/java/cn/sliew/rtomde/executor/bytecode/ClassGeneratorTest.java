package cn.sliew.rtomde.executor.bytecode;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassGeneratorTest {

    /**
     * 实现builder接口，并实现
     */
    @Test
    public void testSimpleInterface() throws Exception {
        User user = new User();
        Field fname = null;
        Field fs[] = User.class.getDeclaredFields();
        for (Field f : fs) {
            f.setAccessible(true);
            if (f.getName().equals("userName"))
                fname = f;
        }

        try (ClassGenerator cg = ClassGenerator.newInstance()) {
            cg.setClassName(User.class.getName() + "$UserBuilder");
            cg.addInterface(UserBuilder.class);

            cg.addMethod("public String getUserName(" + User.class.getName() + " user){ boolean[][][] bs = new boolean[0][][]; return (String)FNAME.get($1); }");
            cg.addMethod("public void setUserName(" + User.class.getName() + " user, String name){ FNAME.set($1, $2); }");

            cg.addField("public static java.lang.reflect.Field FNAME;");

            cg.addDefaultConstructor();
            Class<?> cl = cg.toClass();
            cl.getField("FNAME").set(null, fname);

            assertEquals("cn.sliew.rtomde.executor.bytecode.User$UserBuilder", cl.getName());
            UserBuilder builder = (UserBuilder) cl.newInstance();
            assertEquals("foo", user.getUserName());
            builder.setUserName(user, "bar");
            assertEquals("bar", user.getUserName());
        }
    }

}
