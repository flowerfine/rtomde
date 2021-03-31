package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.milky.test.MilkyTestCase;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMapperBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.LettuceOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisCacheOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @Test
    void cacheWithOptions() throws Exception {
        String resource = "cn/sliew/rtomde/platform/mybatis/builder/SysUserMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, application, resource, application.getSqlFragments());
            builder.parse();

            MybatisCacheOptions cacheOptions = application.getCacheOptions("cn.sliew.rtomde.platform.mybatis.builder.SysUserMapper.boss_board_redis");
            LettuceOptions lettuce = cacheOptions.getLettuce();
            assertNotNull(lettuce);
            assertThat(lettuce.getId()).isEqualTo("boss_board");
            assertThat(lettuce.getRedisURI()).isEqualTo("redis://123@localhost:6379/0?timeout=1s");
            assertThat(cacheOptions.getExpire().toMillis()).isEqualTo(30000L);
            assertThat(cacheOptions.getSize()).isEqualTo(500L);
        }
    }

    @Test
    void mappedStatementWithOptions() throws Exception {
        String resource = "cn/sliew/rtomde/platform/mybatis/builder/SysUserMapper.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, application, resource, application.getSqlFragments());
            builder.parse();

            MappedStatement mappedStatement = application.getMappedStatement("cn.sliew.rtomde.platform.mybatis.builder.SysUserMapper.selectByPrimaryKey");
            assertThat(mappedStatement.getTimeout()).isEqualTo(1);
            assertThat(mappedStatement.getDataSourceId()).isEqualTo("data_service");
            assertThat(mappedStatement.getCacheRefId()).isEqualTo("boss_board_redis");
        }
    }


}
