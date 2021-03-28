package cn.sliew.rtomde.config;

import java.util.Map;

/**
 * 1. owner, organization, architecture
 * 2. environment
 * 3. qos
 * 4. platform
 * 5. config
 */
public class ApplicationOptions extends AbstractOptions {

    private static final long serialVersionUID = 2443366122854267151L;

    /**
     * Application name
     */
    private String name;

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
    protected PlatformOptions platform;

    /**
     * The config options
     */
    private ConfigOptions config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getDumpDirectory() {
        return dumpDirectory;
    }

    public void setDumpDirectory(String dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    public Boolean getQosEnable() {
        return qosEnable;
    }

    public void setQosEnable(Boolean qosEnable) {
        this.qosEnable = qosEnable;
    }

    public String getQosHost() {
        return qosHost;
    }

    public void setQosHost(String qosHost) {
        this.qosHost = qosHost;
    }

    public Integer getQosPort() {
        return qosPort;
    }

    public void setQosPort(Integer qosPort) {
        this.qosPort = qosPort;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    public PlatformOptions getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformOptions platform) {
        this.platform = platform;
    }

    public ConfigOptions getConfig() {
        return config;
    }

    public void setConfig(ConfigOptions config) {
        this.config = config;
    }
}
