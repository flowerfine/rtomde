package cn.sliew.rtomde.platform.mybatis.config;

import cn.sliew.rtomde.config.AbstractOptions;

import java.util.Objects;

public class DatasourceOptions extends AbstractOptions {

    private static final long serialVersionUID = 2684647730680496376L;

    private String jdbcUrl;

    private String username;

    private String password;

    private String driverClassName;

    private Boolean profileSQL;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public Boolean getProfileSQL() {
        return profileSQL;
    }

    public void setProfileSQL(Boolean profileSQL) {
        this.profileSQL = profileSQL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DatasourceOptions that = (DatasourceOptions) o;
        return Objects.equals(jdbcUrl, that.jdbcUrl) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(driverClassName, that.driverClassName) &&
                Objects.equals(profileSQL, that.profileSQL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jdbcUrl, username, password, driverClassName, profileSQL);
    }
}
