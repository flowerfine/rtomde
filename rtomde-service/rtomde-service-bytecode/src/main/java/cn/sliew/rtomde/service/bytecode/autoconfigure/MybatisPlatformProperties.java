package cn.sliew.rtomde.service.bytecode.autoconfigure;

import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

@ConfigurationProperties(prefix = MybatisPlatformProperties.PLATFORM_MYBATIS_PREFIX)
public class MybatisPlatformProperties {

    public static final String PLATFORM_MYBATIS_PREFIX = "platform.mybatis";

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /**
     * Location of MyBatis Platform metadata xml config file.
     */
    private String metadataLocation;

    /**
     * Indicates whether perform presence check of the MyBatis Platform metadata xml config file.
     */
    private boolean checkMetadataLocation = false;

    /**
     * Environment.
     */
    private String environment;

    /**
     * Locations of MyBatis Platform application xml config files.
     */
    private String[] applicationLocations;

    /**
     * Packages to search type aliases. (Package delimiters are ",; \t\n")
     */
    private String typeAliasesPackage;

    /**
     * Packages to search for type handlers. (Package delimiters are ",; \t\n")
     */
    private String typeHandlersPackage;

    /**
     * The default scripting language driver class. (Available when use together with mybatis-spring 2.0.2+)
     */
    private Class<? extends LanguageDriver> defaultScriptingLanguageDriver;

    /**
     * Externalized properties for MyBatis Platform metadata.
     */
    private Properties platformProperties;

    /**
     * A MybatisPlatformOptions object for customize default settings. If {@link #platform} is specified, this property is
     * not used.
     */
    @NestedConfigurationProperty
    private MybatisPlatformOptions platform;

    public String getMetadataLocation() {
        return metadataLocation;
    }

    public void setMetadataLocation(String metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public boolean isCheckMetadataLocation() {
        return checkMetadataLocation;
    }

    public void setCheckMetadataLocation(boolean checkMetadataLocation) {
        this.checkMetadataLocation = checkMetadataLocation;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String[] getApplicationLocations() {
        return applicationLocations;
    }

    public void setApplicationLocations(String[] applicationLocations) {
        this.applicationLocations = applicationLocations;
    }

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    public String getTypeHandlersPackage() {
        return typeHandlersPackage;
    }

    public void setTypeHandlersPackage(String typeHandlersPackage) {
        this.typeHandlersPackage = typeHandlersPackage;
    }

    public Class<? extends LanguageDriver> getDefaultScriptingLanguageDriver() {
        return defaultScriptingLanguageDriver;
    }

    public void setDefaultScriptingLanguageDriver(Class<? extends LanguageDriver> defaultScriptingLanguageDriver) {
        this.defaultScriptingLanguageDriver = defaultScriptingLanguageDriver;
    }

    public Properties getPlatformProperties() {
        return platformProperties;
    }

    public void setPlatformProperties(Properties platformProperties) {
        this.platformProperties = platformProperties;
    }

    public MybatisPlatformOptions getPlatform() {
        return platform;
    }

    public void setPlatform(MybatisPlatformOptions platform) {
        this.platform = platform;
    }

    public Resource[] resolveApplicationLocations() {
        return Stream.of(Optional.ofNullable(this.applicationLocations).orElse(new String[0]))
                .flatMap(location -> Stream.of(getResources(location))).toArray(Resource[]::new);
    }

    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}
