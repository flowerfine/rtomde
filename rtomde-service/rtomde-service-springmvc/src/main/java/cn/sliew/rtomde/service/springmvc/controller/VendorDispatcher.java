package cn.sliew.rtomde.service.springmvc.controller;

import cn.sliew.rtomde.executor.bytecode.ClassGenerator;
import cn.sliew.rtomde.executor.mapper.MapperInvoker;
import cn.sliew.rtomde.executor.mapper.MapperMethod;
import cn.sliew.rtomde.executor.mapper.PlainMapperInvoker;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@org.springframework.context.annotation.Configuration
public class VendorDispatcher {

    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);

    @Autowired
    private GenericWebApplicationContext ac;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void register() throws Exception {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MapperMethod mapperMethod = new MapperMethod(configuration, mappedStatementName);
            MappedStatement mappedStatement = configuration.getMappedStatement(mappedStatementName);
            ParameterMap parameterMap = mappedStatement.getParameterMap();
            parameterMap.getType();
            List<ParameterMapping> parameterMappings = parameterMap.getParameterMappings();
            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
        }
        for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!key.equals("selectByPrimaryKey")) {
                continue;
            }
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(key).methods(RequestMethod.GET).build();

            try (ClassGenerator cg = ClassGenerator.newInstance(this.getClass().getClassLoader())) {
                cg.addImportedPackages("org.apache.ibatis.session", "java.util");
                cg.setClassName("cn.sliew.rtomde.executor.mapper.SysUserMapper");
                cg.addField("private " + SqlSessionFactory.class.getCanonicalName() + " sqlSessionFactory;");

                StringBuilder methodBody = new StringBuilder();
                methodBody.append("public Object selectByPrimaryKey(Long id)");
                methodBody.append("{");
                methodBody.append("SqlSession sqlSession = this.sqlSessionFactory.openSession();");
                methodBody.append("List objects = sqlSession.selectList(\"selectByPrimaryKey\", $1);");
                methodBody.append("return ($r) objects.get(0);");
                methodBody.append("}");
                cg.addMethod(methodBody.toString());
                Class<?> cl = cg.toClass();
                ac.registerBean("cn.sliew.rtomde.executor.mapper.SysUserMapper", cl);
                Object bean = ac.getBean("cn.sliew.rtomde.executor.mapper.SysUserMapper");
                Method selectByPrimaryKey = cl.getMethod("selectByPrimaryKey", Long.class);
                mappingRegistry.registerMapping(requestMappingInfo, bean, selectByPrimaryKey);
            }
        }
    }

    public Object registerHandler(@PathVariable Long id) {
        return map.get("selectByPrimaryKey").invoke(sqlSessionFactory.openSession(), "selectByPrimaryKey", new Object[]{id});
    }
}