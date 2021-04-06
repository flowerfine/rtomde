package cn.sliew.rtomde.platform.engine;

/**
 * todo 如何启动一个 DataEngine?
 *
 *
 */
public interface DataEngine {

    EngineDescriptor getEngine();

    /**
     * DataEngine将管理应用的工作代理给ApplicationManager
     */
    ApplicationManager getApplicationManager();

    /**
     * 注册，查找，配置，删除，发布
     */
    default DataApplication registerApplication(ApplicationRegistryRequest request) {
        return getApplicationManager().registerApplication(request);
    }

    default DataApplication discoverApplication(ApplicationDiscoveryRequest request) {
        return getApplicationManager().discoverApplication(request);
    }

    default DataApplication configureApplication(ApplicationConfigurationRequest request) {
        return getApplicationManager().configureApplication(request);
    }

    default DataApplication deleteApplication(ApplicationDeleteRequest request) {
        return getApplicationManager().deleteApplication(request);
    }

    default DataApplication publishApplication(ApplicationPublishRequest request) {
        return getApplicationManager().publishApplication(request);
    }

}
