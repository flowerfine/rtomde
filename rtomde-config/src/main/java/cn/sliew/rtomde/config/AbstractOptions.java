package cn.sliew.rtomde.config;

import cn.sliew.milky.common.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * todo attributemap
 */
public abstract class AbstractOptions implements Serializable {

    private static final long serialVersionUID = 2959235272146318124L;

    /**
     * The config id.
     */
    protected String id;

    /**
     * The application version
     *
     * @see cn.sliew.milky.common.version.SemVersion
     */
    private String version;

    /**
     * The config util config.
     */
    protected String prefix;

    protected final AtomicBoolean refreshed = new AtomicBoolean(false);

    public static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        return StringUtils.camelToSplitName(tag, "-");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void updateIdIfAbsent(String value) {
        if (StringUtils.isNotBlank(value) && StringUtils.isBlank(id)) {
            this.id = value;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrefix() {
        return StringUtils.isNotBlank(prefix) ? prefix : (CommonConstants.RTOMDE + "." + getTagName(this.getClass()));
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * for config refresh.
     */
    public void refresh() {
        refreshed.set(true);
    }

    public boolean isRefreshed() {
        return refreshed.get();
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractOptions that = (AbstractOptions) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(prefix, that.prefix) &&
                Objects.equals(refreshed, that.refreshed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, prefix, refreshed);
    }

    @Override
    public String toString() {
        return "AbstractConfig{" +
                "id='" + id + '\'' +
                ", prefix='" + prefix + '\'' +
                ", refreshed=" + refreshed +
                '}';
    }
}
