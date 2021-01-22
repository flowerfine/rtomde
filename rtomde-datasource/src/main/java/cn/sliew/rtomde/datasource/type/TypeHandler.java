package cn.sliew.rtomde.datasource.type;

/**
 * 无非是实现写入和读取的功能，写入为将javaType转化为columnType，读取为将columnType转化为javaType
 *
 */
public interface TypeHandler {

    Type getType();

    ReadFunction read();

    WriteFunction write();
}
