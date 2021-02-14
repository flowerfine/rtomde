package cn.sliew.rtomde.container.core.temp;

public interface ContainerFactory {

    /**
     * 创建一个容器，对应着应用的创建
     */
    Container create();

}
