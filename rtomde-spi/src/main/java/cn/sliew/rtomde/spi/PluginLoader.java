package cn.sliew.rtomde.spi;

import java.util.List;

public interface PluginLoader<T> {

    List<LoadingStrategy> loadingStrategys();


}
