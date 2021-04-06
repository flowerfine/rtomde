package cn.sliew.rtomde.platform.engine;

/**
 * 应用包含资源，并将资源的管理代理给 ResourceManager
 */
public interface DataApplication {

    String getId();

    String getVersion();

    String getName();

    ResourceManager getResourceManager();

    DataResource registerResource(ResourceRegistryRequest request);

    DataResource discoverResource(ResourceDiscoveryRequest request);

    DataResource configureResource(ResourceConfigurationRequest request);

    void execute();

}
