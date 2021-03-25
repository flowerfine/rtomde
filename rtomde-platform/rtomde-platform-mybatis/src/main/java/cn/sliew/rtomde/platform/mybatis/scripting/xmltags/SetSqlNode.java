package cn.sliew.rtomde.platform.mybatis.scripting.xmltags;

import cn.sliew.rtomde.platform.mybatis.session.Configuration;

import java.util.Collections;
import java.util.List;

public class SetSqlNode extends TrimSqlNode {

    private static final List<String> COMMA = Collections.singletonList(",");

    public SetSqlNode(Configuration configuration, SqlNode contents) {
        super(configuration, contents, "SET", COMMA, null, COMMA);
    }

}
