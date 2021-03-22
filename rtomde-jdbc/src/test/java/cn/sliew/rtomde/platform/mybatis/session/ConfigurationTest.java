package cn.sliew.rtomde.platform.mybatis.session;

import cn.sliew.rtomde.platform.mybatis.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

public class ConfigurationTest {

    @Test
    public void test() throws Exception {
        String resource = "org/apache/ibatis/session/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                List<Object> objects = sqlSession.selectList("cn.sliew.rtomde.dao.SysUserMapper.selectByPrimaryKey", 1);
                System.out.println(objects);
            }
        }
    }
}
