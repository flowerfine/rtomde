package cn.sliew.rtomde.platform.mybatis.session;

/**
 * Specifies if and how MyBatis should automatically map columns to fields/properties.
 *
 * @author Eduardo Macarron
 */
public enum AutoMappingBehavior {

    /**
     * Disables auto-mapping.
     */
    NONE,

    /**
     * Will only auto-map results with no nested result mappings defined inside.
     */
    PARTIAL,

    /**
     * Will auto-map result mappings of any complexity (containing nested or otherwise).
     */
    FULL
}
