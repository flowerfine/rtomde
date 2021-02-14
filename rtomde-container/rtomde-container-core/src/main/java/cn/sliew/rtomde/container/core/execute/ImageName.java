package cn.sliew.rtomde.container.core.execute;

import java.util.Optional;

public class ImageName {

    private final Optional<String> prefix;

    private final String name;

    private final Optional<String> tag;

    private final Optional<String> registry;

    public ImageName(Optional<String> prefix, String name, Optional<String> tag, Optional<String> registry) {
        this.prefix = prefix;
        this.name = name;
        this.tag = tag;
        this.registry = registry;
    }

    /**
     * registry/prefix/name:tag
     */
    public String resolveImageName() {
        String reg = registry.orElse("");
        if (!reg.endsWith("/")) {
            reg = reg + "/";
        }
        String pre = prefix.orElse("");
        if (!pre.equals("")) {
            pre = pre + "/";
        }
        String t = tag.orElse("");
        if (!t.equals("")) {
            t = ":" + t;
        }
        return reg + pre + name + t;
    }

    public Optional<String> getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getTag() {
        return tag;
    }

    public Optional<String> getRegistry() {
        return registry;
    }
}
