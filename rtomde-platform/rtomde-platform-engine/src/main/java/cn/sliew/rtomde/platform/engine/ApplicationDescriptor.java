package cn.sliew.rtomde.platform.engine;

/**
 * Resource资源描述，告诉应用可以到哪里获取这个信息。
 * 具体的实现信息可以参考spring的Resource类。
 */
public interface ApplicationDescriptor {


    String getId();

    String getVersion();

    String getName();


}
