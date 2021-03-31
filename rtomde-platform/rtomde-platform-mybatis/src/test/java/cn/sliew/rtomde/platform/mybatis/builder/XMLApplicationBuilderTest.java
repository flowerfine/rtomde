package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.milky.test.MilkyTestCase;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLApplicationBuilder;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.domain.blog.Author;
import cn.sliew.rtomde.platform.mybatis.domain.blog.Blog;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XMLApplicationBuilderTest extends MilkyTestCase {

    private MybatisPlatformOptions platform;

    @BeforeEach
    private void beforeEach() throws Exception {
        String resource = "cn/sliew/rtomde/platform/mybatis/builder/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMetadataBuilder builder = new XMLMetadataBuilder(inputStream);
            platform = builder.parse();
        }
    }

    @Test
    public void shouldSuccessfullyLoadXMLApplicationFile() throws Exception {
        String resource = "cn/sliew/rtomde/platform/mybatis/builder/ApplicationConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLApplicationBuilder builder = new XMLApplicationBuilder(inputStream, platform);
            MybatisApplicationOptions application = builder.parse();

            assertNotNull(application);
            assertThat(application.getProps()).containsEntry("prop1", "bbbb");

            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("integer")).isEqualTo(Integer.class);
            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("long")).isEqualTo(Long.class);
            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("hashmap")).isEqualTo(HashMap.class);
            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("linkedhashmap")).isEqualTo(LinkedHashMap.class);
            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("arraylist")).isEqualTo(ArrayList.class);
            assertThat(application.getTypeAliasRegistry().getTypeAliases().get("linkedlist")).isEqualTo(LinkedList.class);

            assertNotNull(application.getDataSource("data_service"));

        }
    }


}
