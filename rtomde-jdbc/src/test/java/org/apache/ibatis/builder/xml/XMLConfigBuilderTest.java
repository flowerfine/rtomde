package org.apache.ibatis.builder.xml;

import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.*;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XMLConfigBuilderTest {


    @Test
    public void shouldSuccessfullyLoadMinimalXMLConfigFile() throws Exception {
        String resource = "org/apache/ibatis/builder/xml/MinimalMapperConfig.xml";
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
            Configuration config = builder.parse();
            assertNotNull(config);
            assertThat(config.getApplication()).isEqualTo("minimal");
            assertThat(config.getAutoMappingBehavior()).isEqualTo(AutoMappingBehavior.PARTIAL);
            assertThat(config.getAutoMappingUnknownColumnBehavior()).isEqualTo(AutoMappingUnknownColumnBehavior.NONE);
            assertThat(config.getProxyFactory()).isInstanceOf(JavassistProxyFactory.class);
            assertThat(config.isLazyLoadingEnabled()).isFalse();
            assertThat(config.isAggressiveLazyLoading()).isFalse();
            assertThat(config.isMultipleResultSetsEnabled()).isTrue();
            assertThat(config.isUseColumnLabel()).isTrue();
            assertThat(config.getDefaultExecutorType()).isEqualTo(ExecutorType.SIMPLE);
            assertNull(config.getDefaultStatementTimeout());
            assertThat(config.isSafeRowBoundsEnabled()).isFalse();
            assertThat(config.getLocalCacheScope()).isEqualTo(LocalCacheScope.SESSION);
            assertThat(config.getJdbcTypeForNull()).isEqualTo(JdbcType.OTHER);
            assertThat(config.getLazyLoadTriggerMethods()).isEqualTo(new HashSet<>(Arrays.asList("equals", "clone", "hashCode", "toString")));
            assertThat(config.isSafeResultHandlerEnabled()).isTrue();
            assertThat(config.getDefaultScriptingLanuageInstance()).isInstanceOf(XMLLanguageDriver.class);
            assertThat(config.isCallSettersOnNulls()).isFalse();
            assertNull(config.getLogPrefix());
            assertThat(config.getLogImpl()).isEqualTo(Slf4jImpl.class);
            assertNull(config.getConfigurationFactory());
            assertThat(config.getTypeHandlerRegistry().getTypeHandler(RoundingMode.class)).isInstanceOf(EnumTypeHandler.class);
        }
    }
}
