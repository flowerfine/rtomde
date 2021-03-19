package org.apache.ibatis.cache;

public enum CacheType {
    LETTUCE(0, "https://github.com/lettuce-io/lettuce-core"),
    OHC(1, "https://github.com/snazy/ohc"),
    CAFFEINE(2, "https://github.com/ben-manes/caffeine"),
    ;

    private final int code;
    private final String desc;

    CacheType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
