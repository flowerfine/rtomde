package cn.sliew.rtomde.platform.engine;

public interface DataResource {

    ResourceDescriptor getResource();

    Invoker toInvoker();

    void export();

}
