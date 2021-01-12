package cn.sliew.rtomde.executor.controller;

import cn.sliew.rtomde.executor.mapper.SysUser;
import cn.sliew.rtomde.executor.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;

    @GetMapping("/sqluser/{id}")
    public SysUser getSqlUser(@PathVariable Long id) {
//        mappingRegistry.registerMapping(null, null, null);
//        这里使用的是org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
//        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(this.resolveEmbeddedValuesInPatterns(requestMapping.path())).methods(requestMapping.method()).params(requestMapping.params()).headers(requestMapping.headers()).consumes(requestMapping.consumes()).produces(requestMapping.produces()).mappingName(requestMapping.name());
//        if (customCondition != null) {
//            builder.customCondition(customCondition);
//        }
//
//        return builder.options(this.config).build();
        //org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
        Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
        for (MappedStatement mappedStatement : mappedStatements) {
//            System.out.println(mappedStatement.getId());
        }


//        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
//        System.out.println(mappedStatementNames);
//        for (String mappedStatementName : mappedStatementNames) {
//            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths(mappedStatementName).methods(RequestMethod.GET).build();
//
//        }
        MappedStatement mappedStatement = configuration.getMappedStatement("cn.sliew.rtomde.executor.mapper.SysUserMapper.selectByPrimaryKey");
        ParameterMap parameterMap = mappedStatement.getParameterMap();
//        System.out.println(parameterMap);

        SqlSession sqlSession = sqlSessionFactory.openSession();
//        List<Object> objects = sqlSession.selectList("cn.sliew.rtomde.executor.mapper.SysUserMapper.selectByPrimaryKey", id);
        List<Object> objects = sqlSession.selectList("selectByPrimaryKey", id);
        return (SysUser) objects.get(0);
    }
}