package cn.rtomde.template.component;

import cn.sliew.milky.component.Component;

import javax.sql.DataSource;

public interface DataSourceComponent extends Component {

    DataSource getInstance();
}
