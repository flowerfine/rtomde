package cn.sliew.rtomde.platform.engine;

public interface DataEngine {

    /**
     * Get the ID that uniquely identifies this data engine.
     *
     * <p>Each data engine must provide a unique ID.
     */
    String getId();

    /**
     * Get the Version that this data engine iterated.
     *
     * <p>Each data engine must provide its current version.
     */
    String getVersion();

    // application。创建一个应用
    // service。服务以什么形式对外提供服务，dubbo/http。
    // executor(mybatis)
    // limits（ratelimiter, bulkhead, circuitbreaker, cache, timelimiter, retry, log）这里是针对执行器的概念
    // observability（log, metrics, trace）这里也是针对执行器的概念。
    // 查询配置

//    /**
//     * Discover data application according to the supplied {@link EngineDiscoveryRequest}.
//     *
//     * <p>The supplied {@link UniqueId} must be used as the unique ID of the
//     * returned root {@link TestDescriptor}. In addition, the {@code UniqueId}
//     * must be used to create unique IDs for children of the root's descriptor
//     * by calling {@link UniqueId#append}.
//     *
//     * @param discoveryRequest the discovery request; never {@code null}
//     * @param uniqueId the unique ID to be used for this test engine's
//     * {@code TestDescriptor}; never {@code null}
//     * @return the root {@code TestDescriptor} of this engine, typically an
//     * instance of {@code EngineDescriptor}
//     * @see org.junit.platform.engine.support.descriptor.EngineDescriptor
//     */
//    TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId);
//
//    /**
//     * Execute tests according to the supplied {@link ExecutionRequest}.
//     *
//     * <p>The {@code request} passed to this method contains the root
//     * {@link TestDescriptor} that was previously returned by {@link #discover},
//     * the {@link EngineExecutionListener} to be notified of test execution
//     * events, and {@link ConfigurationParameters} that may influence test execution.
//     *
//     * @param request the request to execute tests for; never {@code null}
//     */
//    void execute(ExecutionRequest request);

}
