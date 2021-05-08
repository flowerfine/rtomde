package cn.sliew.rtomde.config.metadata;

import java.io.Serializable;
import java.util.List;

public class ApplicationMetadata implements Serializable {

    private static final long serialVersionUID = 1754484080839091532L;

    private final String name;

    private final String version;

    private final List<MethodMetadata> methods;

    public ApplicationMetadata(String name, String version, List<MethodMetadata> methods) {
        this.name = name;
        this.version = version;
        this.methods = methods;
    }
}
