package cn.sliew.rtomde.platform.engine;

import java.util.List;

/**
 * 应用包含资源
 */
public interface DataApplication {

    String getId();

    String getVersion();

    String getName();

    List<DataResource> getResources();

}
