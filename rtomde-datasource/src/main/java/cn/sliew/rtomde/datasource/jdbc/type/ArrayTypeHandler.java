package cn.sliew.rtomde.datasource.jdbc.type;

import cn.sliew.rtomde.datasource.type.ReadFunction;
import cn.sliew.rtomde.datasource.type.Type;
import cn.sliew.rtomde.datasource.type.TypeHandler;
import cn.sliew.rtomde.datasource.type.WriteFunction;

public class ArrayTypeHandler implements TypeHandler {

    @Override
    public Type getType() {
        return ArrayJdbcType.INSTANCE;
    }

    @Override
    public ReadFunction read() {
        return null;
    }

    @Override
    public WriteFunction write() {
        return null;
    }
}
