package cn.sliew.rtomde.platform.engine;

public interface DataEngine {

    String getId();

    String getVersion();

    /**
     * 注册，查找，配置，下线和删除
     */
    DataApplication registerApplication(ApplicationRegistryRequest request);

    DataApplication discoverApplication(ApplicationDiscoveryRequest request);

    DataApplication configureApplication(ApplicationConfigurationRequest request);



    DataResource registerResource(ResourceRegistryRequest request);

    DataResource discoverResource(ResourceDiscoveryRequest request);

    DataResource configureResource(ResourceConfigurationRequest request);

    void execute();

}
