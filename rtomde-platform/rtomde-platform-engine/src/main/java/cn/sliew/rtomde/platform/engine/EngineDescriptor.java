package cn.sliew.rtomde.platform.engine;

public interface EngineDescriptor {

    String getId();

    String getVersion();

    String getName();

    void export();
}
