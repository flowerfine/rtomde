package cn.sliew.rtomde.service.bytecode.autoconfigure;

import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandler;
import cn.sliew.rtomde.service.bytecode.spring.SqlSessionFactoryBean;
import cn.sliew.rtomde.service.bytecode.spring.SqlSessionTemplate;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@ConditionalOnClass(SqlSessionFactory.class)
@EnableConfigurationProperties(MybatisPlatformProperties.class)
public class MybatisAutoConfiguration implements InitializingBean {

    private final MybatisPlatformProperties properties;

    private final TypeHandler[] typeHandlers;

    private final LanguageDriver[] languageDrivers;

    private final ResourceLoader resourceLoader;

    private final List<MybatisPlatformCustomizer> mybatisPlatformCustomizers;

    public MybatisAutoConfiguration(MybatisPlatformProperties properties, ObjectProvider<TypeHandler[]> typeHandlersProvider,
                                    ObjectProvider<LanguageDriver[]> languageDriversProvider, ResourceLoader resourceLoader,
                                    ObjectProvider<List<MybatisPlatformCustomizer>> configurationCustomizersProvider) {
        this.properties = properties;
        this.typeHandlers = typeHandlersProvider.getIfAvailable();
        this.languageDrivers = languageDriversProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.mybatisPlatformCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Override
    public void afterPropertiesSet() {
        checkConfigFileExists();
    }

    private void checkConfigFileExists() {
        if (this.properties.isCheckMetadataLocation() && StringUtils.hasText(this.properties.getMetadataLocation())) {
            Resource resource = this.resourceLoader.getResource(this.properties.getMetadataLocation());
            Assert.state(resource.exists(),
                    "Cannot find Mybatis Platform metadata location: " + resource + " (please add metadata file or check your Mybatis Platform metadata config)");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(SqlSessionFactoryBean factory) throws Exception {
        if (StringUtils.hasText(this.properties.getMetadataLocation())) {
            factory.setMetadataLocation(this.resourceLoader.getResource(this.properties.getMetadataLocation()));
        }
        if (StringUtils.hasText(this.properties.getEnvironment())) {
            factory.setEnvironment(this.properties.getEnvironment());
        }
        if (this.properties.getPlatformProperties() != null) {
            factory.setPlatformProperties(this.properties.getPlatformProperties());
        }

        applyPlatform(factory);

        if (!ObjectUtils.isEmpty(this.properties.resolveApplicationLocations())) {
            factory.setApplicationLocations(this.properties.resolveApplicationLocations());
        }

        if (!ObjectUtils.isEmpty(this.typeHandlers)) {
            factory.setTypeHandlers(this.typeHandlers);
        }

        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }

        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }

        Set<String> factoryPropertyNames = Stream.of(new BeanWrapperImpl(SqlSessionFactoryBean.class)
                .getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());
        Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
        if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !ObjectUtils.isEmpty(this.languageDrivers)) {
            factory.setScriptingLanguageDrivers(this.languageDrivers);
            if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {
                defaultLanguageDriver = this.languageDrivers[0].getClass();
            }
        }
        if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        }
        factory.setVfs(SpringBootVFS.class);
        return factory.getObject();
    }

    private void applyPlatform(SqlSessionFactoryBean factory) {
        MybatisPlatformOptions platform = this.properties.getPlatform();

        if (platform == null && !StringUtils.hasText(this.properties.getMetadataLocation())) {
            platform = new MybatisPlatformOptions();
        }
        if (platform != null && !CollectionUtils.isEmpty(this.mybatisPlatformCustomizers)) {
            for (MybatisPlatformCustomizer customizer : this.mybatisPlatformCustomizers) {
                customizer.customize(platform);
            }
        }
        factory.setPlatform(platform);
    }

//    @Bean
//    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
