package cn.sliew.rtomde.config;

import java.util.List;
import java.util.Map;

/**
 * 1. TimeLimiter
 * 2. RateLimiter
 * 3. Retry
 * 4. Circuitbreaker
 * 5. Cache
 * 6. id, interface, arguments, results, source
 * 7. owner, layer, tag
 * 8. auth
 * 9. registry( protocol )
 * 10. log
 */
public class MethodOptions extends AbstractOptions {

    private static final long serialVersionUID = 5520793832466722444L;

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
     * The timeout for remote invocation
     */
    private TimelimiterOptions timelimiter;

    /**
     * The retry
     */
    private RetryOptions retry;

    /**
     * max concurrent threads
     */
    private BulkheadOptions bulkhead;

    /**
     * The rate limiter
     */
    private RateLimiterOptions rateLimiter;

    /**
     * cache id. Cache the return result with the call parameter as key.
     */
    private CacheOptions cache;

    /**
     * The customized parameters
     */
    private Map<String, String> parameters;

    /**
     * The auth
     */
    private AuthOptions auth;

    /**
     * The metrics
     */
    private MetricsOptions metrics;

    /**
     * The interfaces
     */
    private InterfaceOptions interfaces;

    /**
     * The arguments
     */
    private List<ArgumentOptions> arguments;

    /**
     * The results.
     */
    private List<ResultOptions> results;

    /**
     * The registry.
     */
    private RegistryOptions registry;

    /**
     * The log.
     */
    private LogOptions log;

}
