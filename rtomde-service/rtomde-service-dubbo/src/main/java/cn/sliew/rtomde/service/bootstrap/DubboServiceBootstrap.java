package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.annotation.Annotation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Order(DubboServiceBootstrap.ORDER)
@Component
public class DubboServiceBootstrap implements ApplicationRunner {

    static final int ORDER = Integer.MAX_VALUE - 10000;

    private ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private AnnotationConfigApplicationContext ac;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        platform.getAllApplicationOptions().forEach(this::makeDispatcherService);
    }

    private void makeDispatcherService(MybatisApplicationOptions applicationOptions) {
        dubboInternalClassPoolCascade();

        Map<String, List<MappedStatement>> namespaces = applicationOptions.getMappedStatements().stream().collect(Collectors.groupingBy(ms ->
                ms.getId().lastIndexOf(".") != -1 ?
                        ms.getId().substring(0, ms.getId().lastIndexOf(".")) :
                        ms.getId()));

        ApplicationConfig applicationConfig = applicationConfig(sqlSessionFactory.getPlatform());
        List<RegistryConfig> registryConfigs = registryConfigs();
        ProtocolConfig protocolConfig = protocolConfig();
        for (Map.Entry<String, List<MappedStatement>> entry : namespaces.entrySet()) {
            CtClass serviceCt = doMakeDispatcherService(entry.getKey(), entry.getValue());
            CtClass serviceImplCt = doMakeDispatcherServiceImpl(applicationOptions, serviceCt, entry.getKey(), entry.getValue());
            Class serviceInterface = null;
            Class serviceInterfaceImpl = null;
            try {
                serviceCt.writeFile();
                serviceImplCt.writeFile();
                serviceInterface = serviceCt.toClass(classLoader, getClass().getProtectionDomain());
                serviceInterfaceImpl = serviceImplCt.toClass(classLoader, getClass().getProtectionDomain());
            } catch (CannotCompileException e) {
                log.error("create Service:[{}] failed", serviceCt.getName(), e);
                throw new RuntimeException("create Service:" + serviceCt.getName() + " failed.", e);
            } catch (IOException e) {
                log.error("create Service:[{}] failed", serviceCt.getName(), e);
                throw new RuntimeException("create Service:" + serviceCt.getName() + " failed.", e);
            } catch (NotFoundException e) {
                log.error("create Service:[{}] failed", serviceCt.getName(), e);
                throw new RuntimeException("create Service:" + serviceCt.getName() + " failed.", e);
            }
            ac.registerBean(entry.getKey(), serviceInterfaceImpl);
            Object instance = ac.getBean(entry.getKey());

            serviceCt.defrost();
            serviceImplCt.defrost();

            exportDubboMapper(applicationConfig, registryConfigs, protocolConfig, serviceInterface, instance);
        }

    }

    private CtClass doMakeDispatcherService(String namespace, List<MappedStatement> mappedStatements) {
        CtClass serviceClass = classPool.makeInterface(namespace);
        try {
            CtMethod[] methods = makeServiceMethod(serviceClass, mappedStatements);
            for (CtMethod m : methods) {
                serviceClass.addMethod(m);
            }
            return serviceClass;
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", serviceClass.getName(), e);
            throw new RuntimeException("create Service:" + serviceClass.getName() + " failed.", e);
        }
    }

    private CtMethod[] makeServiceMethod(CtClass declaring, List<MappedStatement> mappedStatements) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = makeServiceMethod(declaring, ms);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeServiceMethod(CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return new CtMethod(classPool.get(resultType.getName()), ms.getId().replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtClass doMakeDispatcherServiceImpl(MybatisApplicationOptions applicationOptions, CtClass anInterface, String namespace, List<MappedStatement> mappedStatements) {
        CtClass serviceImplClass = classPool.makeClass(namespace + "Impl");
        serviceImplClass.addInterface(anInterface);
        ClassFile ccFile = serviceImplClass.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        // @Service
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation service = new Annotation("org.springframework.stereotype.Service", constpool);
        classAttr.addAnnotation(service);

        ccFile.addAttribute(classAttr);
        try {
            serviceImplClass.addField(makeAutowiredField(serviceImplClass, constpool));

            CtMethod[] methods = makeServiceMethodImpl(applicationOptions, serviceImplClass, mappedStatements);
            for (CtMethod m : methods) {
                serviceImplClass.addMethod(m);
            }
            return serviceImplClass;
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", serviceImplClass.getName(), e);
            throw new RuntimeException("create Service:" + serviceImplClass.getName() + " failed.", e);
        }
    }

    private CtField makeAutowiredField(CtClass declaring, ConstPool constpool) {
        try {
            CtField ctField = new CtField(classPool.get(MapperDispatcher.class.getName()), "mapperDispatcher", declaring);
            ctField.setModifiers(Modifier.PRIVATE);
            FieldInfo fieldInfo = ctField.getFieldInfo();
            // @Autowired
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
            fieldAttr.addAnnotation(autowired);
            fieldInfo.addAttribute(fieldAttr);
            return ctField;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("make dispatcher field failed.", e);
            throw new RuntimeException("make @Autowired field failed.", e);
        }
    }

    private CtMethod[] makeServiceMethodImpl(MybatisApplicationOptions applicationOptions, CtClass declaring, List<MappedStatement> mappedStatements) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = makeServiceMethodImpl(applicationOptions, declaring, ms);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeServiceMethodImpl(MybatisApplicationOptions applicationOptions, CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return generateMapperMethod(applicationOptions, resultType, paramType, ms.getId(), declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethod(MybatisApplicationOptions applicationOptions, Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return mapperDispatcher.execute(\"" + id + "\", \"" + applicationOptions.getId() + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

    private void dubboInternalClassPoolCascade() {
        try {
            ClassPool pool = org.apache.dubbo.common.bytecode.ClassGenerator.getClassPool(classLoader);
            Field parent = ClassPool.class.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(pool, classPool);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private ApplicationConfig applicationConfig(MybatisPlatformOptions platform) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName(platform.getName());
        application.setOwner("wangqi");
        application.setArchitecture("data-center");
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("unicast", "false");
        application.setParameters(parameters);
        return application;
    }

    private List<RegistryConfig> registryConfigs() {
        RegistryConfig zookeeper = new RegistryConfig();
        zookeeper.setProtocol("zookeeper");
        zookeeper.setAddress("127.0.0.1:2181");
        RegistryConfig multicast = new RegistryConfig();
        multicast.setProtocol("multicast");
        multicast.setAddress("224.5.6.7:1234");
        return Arrays.asList(zookeeper, multicast);
    }

    private ProtocolConfig protocolConfig() {
        ProtocolConfig dubbo = new ProtocolConfig();
        dubbo.setName("dubbo");
        dubbo.setPort(20880);
        dubbo.setThreads(20);
        return dubbo;
    }

    private void exportDubboMapper(ApplicationConfig application,List<RegistryConfig> registries,ProtocolConfig protocol,  Class clazz, Object bean) {
        ServiceConfig service = new ServiceConfig();
        service.setApplication(application);
        service.setRegistries(registries);
        service.setProtocols(Arrays.asList(protocol));
        service.setInterface(clazz);
        service.setRef(bean);
        service.export();
    }

}
