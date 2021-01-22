package cn.sliew.rtomde.datasource.jdbc.type;

import cn.sliew.rtomde.datasource.type.Type;
import cn.sliew.rtomde.datasource.type.TypeSignature;

import java.sql.Array;

public class ArrayJdbcType implements Type {

    public static final ArrayJdbcType INSTANCE = new ArrayJdbcType();

    @Override
    public TypeSignature getTypeSignature() {
        return new TypeSignature("array");
    }

    @Override
    public Class<?> getJavaType() {
        return Array.class;
    }
}
