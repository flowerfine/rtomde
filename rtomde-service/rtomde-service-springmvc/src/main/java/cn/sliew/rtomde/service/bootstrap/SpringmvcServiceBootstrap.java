package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.Configuration;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class SpringmvcServiceBootstrap implements ApplicationRunner {

    private String application;

    private static ClassLoader classLoader = ClassUtils.getClassLoader(SpringmvcServiceBootstrap.class);
    private static ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    @Autowired
    private GenericWebApplicationContext ac;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private MapperDispatcher dispatcher;

    static {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        this.application = configuration.getApplication();

        ac.registerBean("springmvc.MapperController", makeDispatcherController());
        Object bean = ac.getBean("springmvc.MapperController");
        for (String id : dispatcher.getMapperInvokers().keySet()) {
            registerRequestMapper(id, bean, configuration.getMappedStatement(id));
        }
        //todo 获取在线文档
//        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mappingRegistry.getHandlerMethods();
//        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
//            RequestMappingInfo key = entry.getKey();
//            HandlerMethod value = entry.getValue();
//            System.out.println(key.getDirectPaths() + "=" + value);
//        }
    }

    private Class<?> makeDispatcherController() {
        CtClass controllerClass = classPool.makeClass("cn.sliew.rtomde.executor.MapperController");
        ClassFile ccFile = controllerClass.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        // @RestController
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation controller = new Annotation("org.springframework.web.bind.annotation.RestController", constpool);
        classAttr.addAnnotation(controller);

        // @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        Annotation scope = new Annotation("org.springframework.context.annotation.Scope", constpool);
        EnumMemberValue enumMemberValue = new EnumMemberValue(constpool);
        enumMemberValue.setType("org.springframework.context.annotation.ScopedProxyMode");
        enumMemberValue.setValue("TARGET_CLASS");
        scope.addMemberValue("proxyMode", enumMemberValue);
        classAttr.addAnnotation(scope);

        // @RequestMapping
        Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
        // mapper path
        ArrayMemberValue memberValue = new ArrayMemberValue(constpool);
        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + this.application, constpool)});
        requestMapping.addMemberValue("path", memberValue);
        classAttr.addAnnotation(requestMapping);

        ccFile.addAttribute(classAttr);
        try {
            controllerClass.addField(makeAutowiredField(controllerClass, constpool));
            // method
            CtMethod[] methods = makeRequestMapping(controllerClass, constpool);
            for (CtMethod m : methods) {
                controllerClass.addMethod(m);
            }
            return controllerClass.toClass(classLoader, getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        }
    }

    private CtField makeAutowiredField(CtClass declaring, ConstPool constpool) {
        try {
            CtField ctField = new CtField(classPool.get(MapperDispatcher.class.getName()), "mapperDispatcher", declaring);
            ctField.setModifiers(Modifier.PRIVATE);
            FieldInfo fieldInfo = ctField.getFieldInfo();
            // @Autowired
            AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation autowired = new Annotation("org.springframework.beans.factory.annotation.Autowired", constpool);
            fieldAttr.addAnnotation(autowired);
            fieldInfo.addAttribute(fieldAttr);
            return ctField;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("make dispatcher field failed.", e);
            throw new RuntimeException("make @Autowired field failed.", e);
        }
    }

    private CtMethod[] makeRequestMapping(CtClass declaring, ConstPool constpool) {
        Set<String> invokers = dispatcher.getMapperInvokers().keySet();
        List<CtMethod> methods = new ArrayList<>(invokers.size());
        for (String id : invokers) {
            CtMethod method = makeMapperMethod(declaring, id);
            // 方法上添加注解
            MethodInfo info = method.getMethodInfo();
            AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            // @RequestMapping
            Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.GetMapping", constpool);
            //Mapping路径(使用方法名)
            ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
            pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + id.replace(".", "/"), constpool)});
            requestMapping.addMemberValue("path", pathValue);
            methodAttr.addAnnotation(requestMapping);
            //@ResponseBody
            Annotation responseBody = new Annotation("org.springframework.web.bind.annotation.ResponseBody", constpool);
            methodAttr.addAnnotation(responseBody);
            //参数上注解@RequestBody
            Annotation requestBody = new Annotation("org.springframework.web.bind.annotation.RequestBody", constpool);
            ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
            Annotation[][] anno = new Annotation[][]{{requestBody}};
            parameterAnnotationsAttribute.setAnnotations(anno);

            info.addAttribute(methodAttr);
            info.addAttribute(parameterAnnotationsAttribute);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeMapperMethod(CtClass declaring, String id) {
        try {
            MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Class<?> resultType = mappedStatement.getResultMap().getType();
            return generateMapperMethod(resultType, paramType, id, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethod(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return mapperDispatcher.execute(\"" + id + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

    private void registerRequestMapper(String id, Object bean, MappedStatement mappedStatement) {
        try {
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Method method = bean.getClass().getMethod(id.replace(".", "_"), paramType);

            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths("/" + this.application + "/" + id.replace(".", "/")).build();

            mappingRegistry.registerMapping(requestMappingInfo, bean, method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("注册失败!", e);
        }
    }
}
