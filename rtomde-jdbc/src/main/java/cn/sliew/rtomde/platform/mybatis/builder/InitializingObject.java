package cn.sliew.rtomde.platform.mybatis.builder;

/**
 * Interface that indicate to provide an initialization method.
 */
public interface InitializingObject {

    /**
     * Initialize an instance.
     * <p>
     * This method will be invoked after it has set all properties.
     * </p>
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if initialization
     *                   fails
     */
    void initialize() throws Exception;

}
