package cn.sliew.rtomde.datasource;

import java.sql.Connection;
import java.sql.Driver;

/**
 * hikaricp 使用 {@link HikariDataSource} 作为一个连接池，将获取 {@link Connection} 的功能
 * 代理给了内部的 {@link DriverDataSource}，而 {@link DriverDataSource} 内部使用 {@code SPI}
 * 加载 {@link Driver} 实现，如果能够和 {@code driver-class-name} 匹配即使用配置的驱动类，然后将
 * 获取 {@link Connection} 的功能进一步代理给 加载的驱动。
 *
 * @deprecated hikaricp的一个思想简洁，驱动能够实现的功能不需要连接池在实现一遍。当使用mysql的驱动时
 * 能够获取较好的体验，但是切换到trino驱动时就难以适应。
 */
@Deprecated
public class HikaricpOptions extends DataSourceOptions {

    private static final long serialVersionUID = 8686800521850158697L;

}
