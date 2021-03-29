package cn.sliew.rtomde.platform.mybatis.scripting.xmltags;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;

import java.util.Collections;
import java.util.List;

public class SetSqlNode extends TrimSqlNode {

    private static final List<String> COMMA = Collections.singletonList(",");

    public SetSqlNode(MybatisApplicationOptions application, SqlNode contents) {
        super(application, contents, "SET", COMMA, null, COMMA);
    }

}
