package cn.sliew.rtomde.datasource;

import cn.sliew.rtomde.config.AbstractOptions;

/**
 * DataSource options
 * todo does it need to assosiate application ???
 */
public class DataSourceOptions extends AbstractOptions {

    private static final long serialVersionUID = 6763103014759242454L;

    private String driverClassName;

    private String url;

    private String username;

    private String password;


}
