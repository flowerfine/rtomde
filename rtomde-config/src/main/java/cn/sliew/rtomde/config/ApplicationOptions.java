package cn.sliew.rtomde.config;

import java.util.Map;

public class ApplicationOptions extends AbstractOptions {

    private static final long serialVersionUID = 2443366122854267151L;

    /**
     * Application owner
     */
    private String owner;

    /**
     * Application's organization (BU)
     */
    private String organization;

    /**
     * Architecture layer
     */
    private String architecture;

    /**
     * Environment, e.g. dev, test or production
     */
    private String environment;

    /**
     * Directory for saving thread dump
     */
    private String dumpDirectory;

    /**
     * Whether to enable qos or not
     */
    private Boolean qosEnable;

    /**
     * The qos host to listen
     */
    private String qosHost;

    /**
     * The qos port to listen
     */
    private Integer qosPort;

    /**
     * Customized parameters
     */
    private Map<String, String> parameters;

    /**
     * Metadata type, local or remote, if choose remote, you need to further specify metadata center.
     */
    private String metadataType;

    /**
     * platform
     */
    private PlatformOptions platform;

    /**
     * The config options
     */
    private ConfigOptions config;

}
