package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.rtomde.config.PlatformOptions;
import cn.sliew.rtomde.platform.mybatis.executor.loader.ProxyFactory;
import cn.sliew.rtomde.platform.mybatis.executor.loader.javassist.JavassistProxyFactory;
import cn.sliew.rtomde.platform.mybatis.io.VFS;
import cn.sliew.rtomde.platform.mybatis.plugin.Interceptor;
import cn.sliew.rtomde.platform.mybatis.plugin.InterceptorChain;
import cn.sliew.rtomde.platform.mybatis.reflection.ReflectorFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.DefaultObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.factory.ObjectFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.reflection.wrapper.ObjectWrapperFactory;
import cn.sliew.rtomde.platform.mybatis.scripting.LanguageDriverRegistry;
import cn.sliew.rtomde.platform.mybatis.type.TypeAliasRegistry;
import cn.sliew.rtomde.platform.mybatis.type.TypeHandlerRegistry;

import java.util.Map;
import java.util.Properties;

/**
 * todo 更换log实现
 */
public class MybatisPlatformOptions extends PlatformOptions {

    private static final long serialVersionUID = -2816717900936922789L;

    private final String environment;

    protected Properties variables = new Properties();

    protected Class<? extends VFS> vfsImpl;

    private Map<String, String> settings;

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final TypeAliasRegistry typeAliasRegistry;

    private final ObjectFactory objectFactory = new DefaultObjectFactory();

    private final ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    private final ReflectorFactory reflectorFactory;

    protected final InterceptorChain interceptorChain = new InterceptorChain();

    protected ProxyFactory proxyFactory = new JavassistProxyFactory();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();


    public MybatisPlatformOptions(TypeHandlerRegistry typeHandlerRegistry, TypeAliasRegistry typeAliasRegistry, ReflectorFactory reflectorFactory, Properties props) {
        this.environment = System.getenv("ENV");
        this.typeHandlerRegistry = typeHandlerRegistry;
        this.typeAliasRegistry = typeAliasRegistry;
        this.reflectorFactory = reflectorFactory;
        this.variables = props;
    }

    public Class<? extends VFS> getVfsImpl() {
        return this.vfsImpl;
    }

    public void setVfsImpl(Class<? extends VFS> vfsImpl) {
        if (vfsImpl != null) {
            this.vfsImpl = vfsImpl;
            VFS.addImplClass(this.vfsImpl);
        }
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public String getEnvironment() {
        return environment;
    }

    public void setVariables(Properties variables) {
        this.variables = variables;
    }

    public Properties getVariables() {
        return variables;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public InterceptorChain getInterceptorChain() {
        return interceptorChain;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public Configuration toMybatisConfiguration() {
        return null;
    }

}
