package cn.sliew.rtomde.platform.engine;

public interface ApplicationManager {

    /**
     * 注册，查找，配置，删除，发布
     */
    DataApplication registerApplication(ApplicationRegistryRequest request);

    DataApplication discoverApplication(ApplicationDiscoveryRequest request);

    DataApplication configureApplication(ApplicationConfigurationRequest request);

    DataApplication deleteApplication(ApplicationDeleteRequest request);

    DataApplication publishApplication(ApplicationPublishRequest request);

}
