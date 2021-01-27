package org.apache.ibatis.session;

import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class ConfigurationTest {

    @Test
    public void test() throws Exception {
        String resource = "org/apache/ibatis/session/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                UserParam param = new UserParam();
                param.setId(1L);
                List<Object> objects = sqlSession.selectList("selectByPrimaryKey", param);
                System.out.println(objects);
            }

        }
    }
}
