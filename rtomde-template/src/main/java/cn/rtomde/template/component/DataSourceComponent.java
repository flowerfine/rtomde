package cn.rtomde.template.component;

import javax.sql.DataSource;

public interface DataSourceComponent {

    DataSource newInstance();
}
