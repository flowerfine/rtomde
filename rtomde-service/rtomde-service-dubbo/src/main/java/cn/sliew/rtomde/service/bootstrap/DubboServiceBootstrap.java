package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.ClassPool;
import org.apache.dubbo.common.bytecode.ClassGenerator;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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




        ApplicationConfig application = new ApplicationConfig();
        application.setName("rtomde-service-dubbo");
        application.setOwner("wangqi");
        application.setArchitecture("data-center");
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("unicast", "false");
        application.setParameters(parameters);

        RegistryConfig zookeeper = new RegistryConfig();
        zookeeper.setProtocol("zookeeper");
        zookeeper.setAddress("127.0.0.1:2181");
        RegistryConfig multicast = new RegistryConfig();
        multicast.setProtocol("multicast");
        multicast.setAddress("224.5.6.7:1234");

        ProtocolConfig dubbo = new ProtocolConfig();
        dubbo.setName("dubbo");
        dubbo.setPort(20880);
        dubbo.setThreads(20);
        ProtocolConfig rest = new ProtocolConfig();
        rest.setName("rest");
        rest.setPort(8080);
        rest.setContextpath("hello");
        rest.setServer("netty");
        rest.setThreads(20);

        ServiceConfig service = new ServiceConfig();
        service.setApplication(application);
        service.setRegistries(Arrays.asList(zookeeper, multicast));
        service.setProtocols(Arrays.asList(dubbo, rest));
//        service.setInterface(HelloService.class);
//        service.setRef(helloService);
        service.export();

    }
}
