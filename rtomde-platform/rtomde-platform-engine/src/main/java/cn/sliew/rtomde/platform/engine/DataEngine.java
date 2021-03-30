package cn.sliew.rtomde.platform.engine;

public interface DataEngine {

    String getId();

    String getVersion();

    DataApplication registerApplication(ApplicationDiscoveryRequest request);

    DataApplication discoverApplication();

    DataResource registerResource();

    DataResource discoverResource();

    void execute();

}
