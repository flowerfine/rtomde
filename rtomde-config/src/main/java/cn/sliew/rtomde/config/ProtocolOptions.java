package cn.sliew.rtomde.config;

import java.util.Map;

public class ProtocolOptions extends AbstractOptions {

    private static final long serialVersionUID = -433557551154797407L;

    /**
     * Protocol name
     */
    private String name;

    /**
     * Service ip address (when there are multiple network cards available)
     */
    private String host;

    /**
     * Service port
     */
    private Integer port;

    /**
     * Context path
     */
    private String contextpath;

    /**
     * Thread pool
     */
    private String threadpool;

    /**
     * Access log
     */
    private String accesslog;

    /**
     * Whether to register
     */
    private Boolean register;

    /**
     * The customized parameters
     */
    private Map<String, String> parameters;

}
