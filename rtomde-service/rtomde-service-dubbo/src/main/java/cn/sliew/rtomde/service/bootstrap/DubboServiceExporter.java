package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.milky.log.Logger;
import cn.sliew.milky.log.LoggerFactory;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.service.bytecode.dispatcher.ServiceExporter;
import javassist.CannotCompileException;
import javassist.CtClass;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

import static cn.sliew.milky.common.check.Ensures.*;

public class DubboServiceExporter implements ServiceExporter {

    private static final Logger log = LoggerFactory.getLogger(DubboServiceExporter.class);

    private final String namespace;
    private final CtClass interfaceClazz;
    private final CtClass interfaceImplClazz;
    private final GenericApplicationContext ac;

    public DubboServiceExporter(String namespace, CtClass interfaceClazz, CtClass interfaceImplClazz, GenericApplicationContext ac) {
        this.namespace = notBlank(namespace, "empty namespace");
        this.interfaceClazz = checkNotNull(interfaceClazz, "null interfaceClazz");
        this.interfaceImplClazz = checkNotNull(interfaceImplClazz, "null interfaceImplClazz");
        this.ac = checkNotNull(ac, "null ac");
    }

    @Override
    public void export() {
        ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
        Class serviceInterface = toClass(classLoader, interfaceClazz);
        Class serviceInterfaceImpl = toClass(classLoader, interfaceImplClazz);
        ac.registerBean(namespace, serviceInterfaceImpl);
        Object instance = ac.getBean(namespace, serviceInterface);

        exportDubboMapper(DubboUtil.getApplication(), DubboUtil.getRegistries(), DubboUtil.getProtocols(), serviceInterface, instance);
    }

    private Class<?> toClass(ClassLoader classLoader, CtClass ctClass) {
        try {
            Class clazz = ctClass.toClass(classLoader, getClass().getProtectionDomain());
            ctClass.defrost();
            return clazz;
        } catch (CannotCompileException e) {
            log.error("create Service Class:[{}] failed", ctClass.getName(), e);
            throw new RuntimeException("create Service Class:" + ctClass.getName() + " failed.", e);
        }
    }

    private void exportDubboMapper(ApplicationConfig application, List<RegistryConfig> registries, List<ProtocolConfig> protocols, Class clazz, Object bean) {
        ServiceConfig service = new ServiceConfig();
        service.setApplication(application);
        service.setRegistries(registries);
        service.setProtocols(protocols);
        service.setInterface(clazz);
        service.setRef(bean);
        service.export();
    }

}
