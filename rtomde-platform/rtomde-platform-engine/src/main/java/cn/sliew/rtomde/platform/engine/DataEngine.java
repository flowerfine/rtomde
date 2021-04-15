package cn.sliew.rtomde.platform.engine;

/**
 * todo 如何启动一个 DataEngine?
 * 引擎的实现是不同的，系统中要允许一个平台下多种引擎的实现，每个引擎下面的应用和数据资源都是同一类的实现。
 * 在引擎之外在增加执行编排器以串联多个引擎之间的实现，从而实现平台的图化。
 */
public interface DataEngine {

    EngineDescriptor getEngine();

    /**
     * DataEngine将管理应用的工作代理给ApplicationManager
     */
    ApplicationManager getApplicationManager();

    /**
     * 注册，查找，配置，删除，发布
     * 监听器。监听注册，查找，配置，删除，发布情况
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

    void export();

}
