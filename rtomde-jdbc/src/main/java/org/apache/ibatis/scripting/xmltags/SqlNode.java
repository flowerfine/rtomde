package org.apache.ibatis.scripting.xmltags;

public interface SqlNode {
    boolean apply(DynamicContext context);
}
