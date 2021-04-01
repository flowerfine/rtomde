package cn.sliew.rtomde.config;

import java.time.Duration;
import java.util.Map;

public class MonitorOptions extends AbstractOptions {

    private static final long serialVersionUID = -657911593662439537L;

    /**
     * The protocol of the monitor, if the value is registry, it will search the monitor address from the registry center,
     * otherwise, it will directly connect to the monitor center
     */
    private String protocol;

    /**
     * The monitor address
     */
    private String address;

    /**
     * The monitor user name
     */
    private String username;

    /**
     * The password
     */
    private String password;

    /**
     * The monitor group
     */
    private String group;

    /**
     * The monitor interval
     */
    private Duration interval;

    /**
     * customized parameters
     */
    private Map<String, String> parameters;
}
