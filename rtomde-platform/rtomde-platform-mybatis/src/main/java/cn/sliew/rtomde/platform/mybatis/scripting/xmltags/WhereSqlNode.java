package cn.sliew.rtomde.platform.mybatis.scripting.xmltags;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;

import java.util.Arrays;
import java.util.List;

public class WhereSqlNode extends TrimSqlNode {

    private static List<String> prefixList = Arrays.asList("AND ", "OR ", "AND\n", "OR\n", "AND\r", "OR\r", "AND\t", "OR\t");

    public WhereSqlNode(MybatisApplicationOptions application, SqlNode contents) {
        super(application, contents, "WHERE", prefixList, null, null);
    }

}
