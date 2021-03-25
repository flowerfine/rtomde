package cn.sliew.rtomde.platform.mybatis.cache;

public enum CacheType {
    LETTUCE(0, "lettuce", "https://github.com/lettuce-io/lettuce-core"),
    OHC(1, "ohc", "https://github.com/snazy/ohc"),
    CAFFEINE(2, "caffeine", "https://github.com/ben-manes/caffeine"),
    ;

    private final int code;
    private final String name;
    private final String desc;

    CacheType(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static CacheType ofName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("cache type can't be empty!");
        }

        for (CacheType type : values()) {
            if (name.equals(type.name)) {
                return type;
            }
        }
        throw new IllegalStateException(String.format("unknown cache type: [%s]", name));
    }

    public String getName() {
        return name;
    }
}
