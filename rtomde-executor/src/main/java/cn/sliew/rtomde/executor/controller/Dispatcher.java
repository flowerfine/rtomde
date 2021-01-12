package cn.sliew.rtomde.executor.controller;

import javassist.ClassPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
public class Dispatcher {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;

    @PostConstruct
    public void register() {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MappedStatement mappedStatement = configuration.getMappedStatement(mappedStatementName, true);
            String resource = mappedStatement.getResource();

            System.out.println(mappedStatementName + "=" + mappedStatement.getId());
        }
    }

    private Method getHandlerMethod(MappedStatement mappedStatement) {
        ParameterMap parameterMap = mappedStatement.getParameterMap();
        List<ParameterMapping> parameterMappings = parameterMap.getParameterMappings();
        for (ParameterMapping parameterMapping : parameterMappings) {
            log.info("{}: {}", mappedStatement.getId(), parameterMapping.toString());
        }

        ClassPool pool = ClassPool.getDefault();
//        CtClass cc = pool.get("test.Rectangle");
        return null;
    }

}
