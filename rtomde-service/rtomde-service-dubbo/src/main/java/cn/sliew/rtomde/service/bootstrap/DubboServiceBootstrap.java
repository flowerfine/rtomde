package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
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
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Order(DubboServiceBootstrap.ORDER)
//@Component
public class DubboServiceBootstrap implements ApplicationRunner {

    static final int ORDER = Integer.MAX_VALUE - 10000;

    private String application;

    private ClassLoader classLoader = ClassUtils.getClassLoader(DubboServiceBootstrap.class);
    private ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private MapperDispatcher dispatcher;
    @Autowired
    private AnnotationConfigApplicationContext ac;

    {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        this.application = configuration.getApplication();

        CtClass serviceCt = makeDispatcherService();
        CtClass serviceImplCt = makeDispatcherServiceImpl(serviceCt);

        Class serviceInterface = serviceCt.toClass(classLoader, getClass().getProtectionDomain());
        Class serviceInterfaceImpl = serviceImplCt.toClass(classLoader, getClass().getProtectionDomain());
        serviceCt.detach();
        serviceImplCt.detach();
        ac.registerBean("dubbo.MapperService", serviceInterfaceImpl);
        Object instance = ac.getBean("dubbo.MapperService");

        ClassPool pool = ClassPool.getDefault();

        CtClass ctClass = pool.get(serviceInterface.getName());


//        ApplicationConfig application = new ApplicationConfig();
//        application.setName(this.application);
//        application.setOwner("wangqi");
//        application.setArchitecture("data-center");
//        Map<String, String> parameters = new HashMap<>(2);
//        parameters.put("unicast", "false");
//        application.setParameters(parameters);
//
//        RegistryConfig zookeeper = new RegistryConfig();
//        zookeeper.setProtocol("zookeeper");
//        zookeeper.setAddress("127.0.0.1:2181");
//        RegistryConfig multicast = new RegistryConfig();
//        multicast.setProtocol("multicast");
//        multicast.setAddress("224.5.6.7:1234");
//
//        ProtocolConfig dubbo = new ProtocolConfig();
//        dubbo.setName("dubbo");
//        dubbo.setPort(20880);
//        dubbo.setThreads(20);
//
//        ServiceConfig service = new ServiceConfig();
//        service.setApplication(application);
//        service.setRegistries(Arrays.asList(zookeeper, multicast));
//        service.setProtocols(Arrays.asList(dubbo));
//        service.setInterface(serviceInterface);
//        service.setRef(instance);
//        service.export();
    }

    private CtClass makeDispatcherService() {
        CtClass serviceClass = classPool.makeInterface("cn.sliew.rtomde.executor.MapperService");
        try {
            CtMethod[] methods = makeServiceMethod(serviceClass);
            for (CtMethod m : methods) {
                serviceClass.addMethod(m);
            }
            return serviceClass;
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", serviceClass.getName(), e);
            throw new RuntimeException("create Service:" + serviceClass.getName() + " failed.", e);
        }
    }

    private CtMethod[] makeServiceMethod(CtClass declaring) {
        Set<String> invokers = dispatcher.getMapperInvokers().keySet();
        List<CtMethod> methods = new ArrayList<>(invokers.size());
        for (String id : invokers) {
            CtMethod method = makeServiceMethod(declaring, id);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeServiceMethod(CtClass declaring, String id) {
        try {
            MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Class<?> resultType = mappedStatement.getResultMap().getType();
            return new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtClass makeDispatcherServiceImpl(CtClass anInterface) {
        CtClass serviceImplClass = classPool.makeClass("cn.sliew.rtomde.executor.MapperServiceImpl");
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

            CtMethod[] methods = makeServiceMethodImpl(serviceImplClass);
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

    private CtMethod[] makeServiceMethodImpl(CtClass declaring) {
        Set<String> invokers = dispatcher.getMapperInvokers().keySet();
        List<CtMethod> methods = new ArrayList<>(invokers.size());
        for (String id : invokers) {
            CtMethod method = makeServiceMethodImpl(declaring, id);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeServiceMethodImpl(CtClass declaring, String id) {
        try {
            MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Class<?> resultType = mappedStatement.getResultMap().getType();
            return generateMapperMethod(resultType, paramType, id, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethod(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return mapperDispatcher.execute(\"" + id + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

}
