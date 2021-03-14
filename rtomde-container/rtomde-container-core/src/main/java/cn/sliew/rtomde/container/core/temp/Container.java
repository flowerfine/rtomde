package cn.sliew.rtomde.container.core.temp;

public interface Container {

    void prepare();

    void start();

    void destroy();

    /**
     * 传入参数，返回结果、异常、日志、trace、explain。。。
     *
     * 装饰器或者责任链，增强熔断，重试，限流等功能。
     *
     * 两种执行方式。一种是带着discovery的执行，用于开发和调试
     * 一种是指定某个任务执行，用于生产环境
     */
    void execute();

    /**
     * 注册监听器，执行监听器和discovery监听器
     */
    void registerListener();

    /**
     * 主动触发服务发现，discovery功能。注册一个接口
     */
    void discovery();
}
