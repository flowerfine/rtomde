package cn.sliew.rtomde.service.springmvc;

import cn.sliew.rtomde.executor.bytecode.ClassGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SpringBeanFactoryTest {

    @Autowired
    private GenericWebApplicationContext ac;

    /**
     * todo 如何执行方法，可以通过一个统一的execute方法，然后内部路由到不同的方法中去
     */
    @Test
    public void testRegisterBean() throws Exception {

        try (ClassGenerator cg = ClassGenerator.newInstance()) {
            cg.setClassName("cn.sliew.rtomde.executor.mapper.SysUserMapper");
            cg.addMethod("public String selectByPrimaryKey(Long id){ return $1.toString(); }");

            Class<?> cl = cg.toClass();

            ac.registerBean("cn.sliew.rtomde.executor.mapper.SysUserMapper", cl);
            Object bean = ac.getBean("cn.sliew.rtomde.executor.mapper.SysUserMapper");
            Method selectByPrimaryKey = cl.getMethod("selectByPrimaryKey", Long.class);
            String result = (String) selectByPrimaryKey.invoke(bean, 100L);
            assertEquals("100", result);
        }
    }
}
