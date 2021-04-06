package cn.sliew.rtomde.platform.engine;

public interface ResourceManager {

    DataResource registerResource(ResourceRegistryRequest request);

    DataResource discoverResource(ResourceDiscoveryRequest request);

    DataResource configureResource(ResourceConfigurationRequest request);

    DataResource deleteResource(ResourceDeleteRequest request);

    DataResource publishResource(ResourcePublishRequest request);
}
