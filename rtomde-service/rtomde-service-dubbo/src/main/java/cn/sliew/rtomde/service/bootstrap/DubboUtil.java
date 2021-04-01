package cn.sliew.rtomde.service.bootstrap;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DubboUtil {

    private static volatile ApplicationConfig application;
    private static volatile List<RegistryConfig> registries;
    private static volatile List<ProtocolConfig> protocols;

    public static ApplicationConfig getApplication() {
        if (application == null) {
            ApplicationConfig application = new ApplicationConfig();
            application.setName("mybatis");
            application.setOwner("wangqi");
            application.setArchitecture("data-center");
            Map<String, String> parameters = new HashMap<>(2);
            parameters.put("unicast", "false");
            application.setParameters(parameters);
            DubboUtil.application = application;
        }
        return application;
    }

    public static List<RegistryConfig> getRegistries() {
        if (registries == null || registries.isEmpty()) {
            RegistryConfig zookeeper = new RegistryConfig();
            zookeeper.setProtocol("zookeeper");
            zookeeper.setAddress("127.0.0.1:2181");
            RegistryConfig multicast = new RegistryConfig();
            multicast.setProtocol("multicast");
            multicast.setAddress("224.5.6.7:1234");
            DubboUtil.registries = Arrays.asList(zookeeper, multicast);
        }
        return registries;
    }

    public static List<ProtocolConfig> getProtocols() {
        if (protocols == null || protocols.isEmpty()) {
            ProtocolConfig dubbo = new ProtocolConfig();
            dubbo.setName("dubbo");
            dubbo.setPort(20880);
            dubbo.setThreads(20);
            DubboUtil.protocols = Arrays.asList(dubbo);
        }
        return protocols;
    }
}
