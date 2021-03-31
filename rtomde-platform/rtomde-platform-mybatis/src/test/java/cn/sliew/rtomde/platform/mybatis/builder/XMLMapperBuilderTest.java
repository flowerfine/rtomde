package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.milky.test.MilkyTestCase;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMapperBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class XMLMapperBuilderTest extends MilkyTestCase {

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
    public void shouldSuccessfullyLoadXMLMapperFile() throws Exception {
        String resource = "cn/sliew/rtomde/platform/mybatis/builder/SysUserMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, application, resource, application.getSqlFragments());
            builder.parse();
        }
    }

}
