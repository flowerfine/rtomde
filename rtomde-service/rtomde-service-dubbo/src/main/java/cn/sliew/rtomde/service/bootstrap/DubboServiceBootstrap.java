package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.bytecode.ClassGenerator;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Order(DubboServiceBootstrap.ORDER)
@Component
public class DubboServiceBootstrap implements ApplicationRunner {

    static final int ORDER = Integer.MAX_VALUE - 10000;

    private String application;

    private static ClassPool classPool = ClassGenerator.getClassPool(DubboServiceBootstrap.class.getClassLoader());

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private MapperDispatcher dispatcher;

    static {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        this.application = configuration.getApplication();

        makeDispatcherInterface();

//        ApplicationConfig application = new ApplicationConfig();
//        application.setName("rtomde-service-dubbo");
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
////        service.setInterface(HelloService.class);
////        service.setRef(helloService);
//        service.export();

    }

    private Class makeDispatcherInterface() {
        CtClass anInterface = classPool.makeInterface("cn.sliew.rtomde.executor.MapperService");
        ClassFile ccFile = anInterface.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        try {
            CtMethod[] methods = makeServiceMethod(anInterface);
            for (CtMethod m : methods) {
                anInterface.addMethod(m);
            }
            anInterface.writeFile();
            return anInterface.toClass(ClassUtils.getClassLoader(DubboServiceBootstrap.class), getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
        } catch (IOException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
        } catch (NotFoundException e) {
            log.error("create Service:[{}] failed", anInterface.getName(), e);
            throw new RuntimeException("create Service:" + anInterface.getName() + " failed.", e);
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

    private Class makeDispatcherInterfaceImpl() {
        return null;
    }
}
