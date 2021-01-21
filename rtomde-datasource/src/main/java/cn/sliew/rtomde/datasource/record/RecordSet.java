package cn.sliew.rtomde.datasource.record;

import cn.sliew.rtomde.datasource.type.Type;

import java.util.List;

public interface RecordSet {

    List<Type> getColumnTypes();

    RecordCursor cursor();
}

