package cn.rtomde.template.mapping.template;

import cn.rtomde.template.mapping.ResultMap;
import cn.rtomde.template.mapping.ResultMapping;
import cn.rtomde.template.type.IntegerTypeHandler;
import cn.rtomde.template.type.StringTypeHandler;

public class ResultMapHelper {

    public static ResultMap userResultMap() {
        ResultMapping id = ResultMapping.builder()
                .column("id")
                .property("id")
                .javaType(Integer.TYPE)
                .typeHandler(new IntegerTypeHandler())
                .build();
        ResultMapping userName = ResultMapping.builder()
                .column("username")
                .property("username")
                .javaType(String.class)
                .typeHandler(new StringTypeHandler())
                .build();

        ResultMapping password = ResultMapping.builder()
                .column("password")
                .property("password")
                .javaType(String.class)
                .typeHandler(new StringTypeHandler())
                .build();
        return ResultMap.builder()
                .id("UserResultMap")
                .resultMappings(id, userName, password)
                .build();
    }
}
