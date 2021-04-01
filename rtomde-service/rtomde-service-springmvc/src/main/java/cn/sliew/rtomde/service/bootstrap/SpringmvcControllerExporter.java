package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.service.bytecode.dispatcher.NameUtil;
import cn.sliew.rtomde.service.bytecode.dispatcher.ServiceExporter;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;

public class SpringmvcControllerExporter implements ServiceExporter {

    private final MybatisApplicationOptions application;
    private final Class<?> controllerClazz;
    private final String namespace;
    private final List<MappedStatement> mappedStatements;
    private final GenericApplicationContext ac;
    private final RequestMappingHandlerMapping mappingRegistry;

    public SpringmvcControllerExporter(MybatisApplicationOptions application,
                                       Class<?> controllerClazz,
                                       String namespace,
                                       List<MappedStatement> mappedStatements,
                                       GenericApplicationContext ac,
                                       RequestMappingHandlerMapping mappingRegistry) {
        this.application = application;
        this.controllerClazz = controllerClazz;
        this.namespace = namespace;
        this.mappedStatements = mappedStatements;
        this.ac = ac;
        this.mappingRegistry = mappingRegistry;
    }

    @Override
    public void export() {
        try {
            ac.registerBean(namespace, controllerClazz);
            Object instance = ac.getBean(namespace, controllerClazz);
            for (MappedStatement mappedStatement : mappedStatements) {
                Class<?> paramType = mappedStatement.getParameterMap().getType();
                Method method = instance.getClass().getMethod(NameUtil.mappedStatementId(mappedStatement.getId()), paramType);
                RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths("/" + application.getId() + "/" + NameUtil.mappedStatementId(mappedStatement.getId())).build();
                mappingRegistry.registerMapping(requestMappingInfo, instance, method);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("注册失败!", e);
        }
    }

}
