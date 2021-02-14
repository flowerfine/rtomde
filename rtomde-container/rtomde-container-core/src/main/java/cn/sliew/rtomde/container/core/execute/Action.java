package cn.sliew.rtomde.container.core.execute;

import cn.sliew.rtomde.container.core.limits.ActionLimits;
import cn.sliew.rtomde.container.core.parameter.Parameters;

public class Action {

    private String application;

    private String namespace;

    private String name;

    private Parameters parameters;

    private ActionLimits limits;

    // sem version
    private String version;

    /**
     * already be published ?
     * default false means not publish.
     */
    private Boolean publish = false;
}
