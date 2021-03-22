package cn.sliew.rtomde.config;

import java.util.List;

public class InterfaceOptions extends AbstractOptions {

    private static final long serialVersionUID = 5401153761554010708L;

    /**
     * The owner of the service providers
     */
    private String owner;

    /**
     * The layer of service providers
     */
    private String layer;

    /**
     * The tag.
     * if there are more than one, you can use commas to separate them
     */
    private String tag;

    /**
     * The application info
     */
    private ApplicationOptions application;

    /**
     * The module info.
     */
    private ModuleOptions module;

    /**
     * The method included.
     */
    private List<MethodOptions> methods;


}
