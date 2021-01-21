package cn.sliew.rtomde.datasource.record;

import cn.sliew.rtomde.datasource.type.Type;

import java.io.Closeable;

public interface RecordCursor extends Closeable {

    Type getType(int field);

    boolean getBoolean(int field);

    long getLong(int field);

    double getDouble(int field);

    Object getObject(int field);

    boolean isNull(int field);

    @Override
    void close();
}
