package cn.sliew.rtomde.platform.engine;

/**
 * 应用包含资源，并将资源的管理代理给 ResourceManager
 *
 * 开发调试功能
 */
public interface DataApplication {

    ApplicationDescriptor getApplication();

    ResourceManager getResourceManager();

    default DataResource registerResource(ResourceRegistryRequest request) {
        return getResourceManager().registerResource(request);
    }

    default DataResource discoverResource(ResourceDiscoveryRequest request) {
        return getResourceManager().discoverResource(request);
    }

    default DataResource configureResource(ResourceConfigurationRequest request) {
        return getResourceManager().configureResource(request);
    }

    default DataResource deleteResource(ResourceDeleteRequest request) {
        return getResourceManager().deleteResource(request);
    }

    default DataResource publishResource(ResourcePublishRequest request) {
        return getResourceManager().publishResource(request);
    }

    void export();

}
