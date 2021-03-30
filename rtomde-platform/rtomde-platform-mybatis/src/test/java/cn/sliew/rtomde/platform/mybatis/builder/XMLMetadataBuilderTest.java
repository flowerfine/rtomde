package cn.sliew.rtomde.platform.mybatis.builder;

import cn.sliew.milky.test.MilkyTestCase;
import cn.sliew.rtomde.platform.mybatis.builder.xml.XMLMetadataBuilder;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.io.Resources;
import cn.sliew.rtomde.platform.mybatis.scripting.xmltags.XMLLanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingBehavior;
import cn.sliew.rtomde.platform.mybatis.session.AutoMappingUnknownColumnBehavior;
import cn.sliew.rtomde.platform.mybatis.type.EnumTypeHandler;
import cn.sliew.rtomde.platform.mybatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XMLMetadataBuilderTest extends MilkyTestCase {

    @Test
    void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
        String resource = "org/apache/ibatis/builder/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLMetadataBuilder builder = new XMLMetadataBuilder(inputStream);
            MybatisPlatformOptions platform = builder.parse();
            assertNotNull(platform);

            assertThat(platform.isSafeRowBoundsEnabled()).isFalse();
            assertThat(platform.isSafeResultHandlerEnabled()).isTrue();
            assertThat(platform.isAggressiveLazyLoading()).isFalse();
            assertThat(platform.isMultipleResultSetsEnabled()).isTrue();
            assertThat(platform.isUseColumnLabel()).isTrue();
            assertThat(platform.isCallSettersOnNulls()).isFalse();
            assertThat(platform.isUseActualParamName()).isTrue();
            assertThat(platform.isReturnInstanceForEmptyRow()).isFalse();
            assertThat(platform.isShrinkWhitespacesInSql()).isFalse();

            assertThat(platform.getJdbcTypeForNull()).isEqualTo(JdbcType.OTHER);
            assertThat(platform.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));

            assertThat(platform.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.PARTIAL);
            assertThat(platform.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.NONE);

//            assertNull(platform.getDefaultStatementTimeout());
            assertThat(platform.getDefaultScriptingLanguageInstance()).isInstanceOf(XMLLanguageDriver.class);
            assertThat(platform.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumTypeHandler.class);
        }
    }

}
