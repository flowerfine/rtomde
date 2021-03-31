package cn.sliew.rtomde.platform.mybatis.binding;

import cn.sliew.milky.test.MilkyTestCase;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class PlainMapperInvokerTest extends MilkyTestCase {

    private MybatisPlatformOptions platform;
    private MybatisApplicationOptions application;

    @BeforeEach
    private void beforeEach() throws Exception {
        String metadata = "cn/sliew/rtomde/platform/mybatis/builder/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(metadata)) {
            XMLMetadataBuilder builder = new XMLMetadataBuilder(inputStream);
            platform = builder.parse();
        }
        String applicationConfig = "cn/sliew/rtomde/platform/mybatis/builder/ApplicationConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(applicationConfig)) {
            XMLApplicationBuilder builder = new XMLApplicationBuilder(inputStream, platform);
            application = builder.parse();
        }
    }

    @Test
    void invoke() {
        MapperMethod mapperMethod = new MapperMethod(application, "cn.sliew.rtomde.platform.mybatis.builder.SysUserMapper.selectByPrimaryKey");
        PlainMapperInvoker invoker = new PlainMapperInvoker(mapperMethod);

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(application);
        Object invoke = invoker.invoke(sqlSessionFactory.openSession(), "cn.sliew.rtomde.platform.mybatis.builder.SysUserMapper.selectByPrimaryKey", new Object[]{1L});
    }
}
