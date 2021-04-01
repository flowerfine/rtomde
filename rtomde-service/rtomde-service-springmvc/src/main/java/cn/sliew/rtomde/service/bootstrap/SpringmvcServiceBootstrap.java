package cn.sliew.rtomde.service.bootstrap;

import cn.sliew.rtomde.common.bytecode.ClassGenerator;
import cn.sliew.rtomde.common.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.platform.mybatis.config.MybatisApplicationOptions;
import cn.sliew.rtomde.platform.mybatis.config.MybatisPlatformOptions;
import cn.sliew.rtomde.platform.mybatis.mapping.MappedStatement;
import cn.sliew.rtomde.platform.mybatis.session.SqlSessionFactory;
import cn.sliew.rtomde.service.bytecode.config.dispatcher.MapperDispatcher;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SpringmvcServiceBootstrap implements ApplicationRunner {

    private static ClassLoader classLoader = ClassUtils.getClassLoader(SpringmvcServiceBootstrap.class);
    private static ClassPool classPool = ClassGenerator.getClassPool(classLoader);

    @Autowired
    private GenericWebApplicationContext ac;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    static {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MybatisPlatformOptions platform = sqlSessionFactory.getPlatform();
        platform.getAllApplicationOptions().forEach(this::makeDispatcherController);
    }

    private void makeDispatcherController(MybatisApplicationOptions applicationOptions) {
        Map<String, List<MappedStatement>> namespaces = applicationOptions.getMappedStatements().stream().collect(Collectors.groupingBy(ms ->
                ms.getId().lastIndexOf(".") != -1 ?
                        ms.getId().substring(0, ms.getId().lastIndexOf(".")) :
                        ms.getId()));
        Map<String, Class<?>> dispatcherControllers = new HashMap<>(namespaces.size());
        for (Map.Entry<String, List<MappedStatement>> entry : namespaces.entrySet()) {
            Class<?> dispatcherController = doMakeDispatcherController(entry.getKey(), entry.getValue());
            dispatcherControllers.put(entry.getKey(), dispatcherController);
        }
        for (Map.Entry<String, Class<?>> entry : dispatcherControllers.entrySet()) {
            ac.registerBean(entry.getKey(), entry.getValue());
            Object bean = ac.getBean(entry.getKey());
            for (MappedStatement ms : namespaces.get(entry.getKey())) {
                registerRequestMapper(applicationOptions, ms.getId(), bean, ms);
            }
        }

    }

    private Class<?> doMakeDispatcherController(String namespace, List<MappedStatement> mappedStatements) {
        CtClass controllerClass = classPool.makeClass(namespace);
        ClassFile ccFile = controllerClass.getClassFile();

        ConstPool constpool = ccFile.getConstPool();

        // @RestController
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation controller = new Annotation("org.springframework.web.bind.annotation.RestController", constpool);
        classAttr.addAnnotation(controller);

        // @RequestMapping
        Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
        // mapper path
        ArrayMemberValue memberValue = new ArrayMemberValue(constpool);
        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("{application}", constpool)});
        requestMapping.addMemberValue("path", memberValue);
        classAttr.addAnnotation(requestMapping);

        ccFile.addAttribute(classAttr);
        try {
            controllerClass.addField(makeAutowiredField(controllerClass, constpool));
            // method
            CtMethod[] methods = makeRequestMapping(controllerClass, constpool, mappedStatements);
            for (CtMethod m : methods) {
                controllerClass.addMethod(m);
            }
            controllerClass.writeFile();
            return controllerClass.toClass(classLoader, getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        } catch (IOException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        } catch (NotFoundException e) {
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

    private CtMethod[] makeRequestMapping(CtClass declaring, ConstPool constpool, List<MappedStatement> mappedStatements) {
        List<CtMethod> methods = new ArrayList<>(mappedStatements.size());
        for (MappedStatement ms : mappedStatements) {
            CtMethod method = makeMapperMethod(declaring, ms);
            // 方法上添加注解
            MethodInfo info = method.getMethodInfo();
            AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            //@RequestMapping
            Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.GetMapping", constpool);
            ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
            pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + ms.getId().replace(".", "/"), constpool)});
            requestMapping.addMemberValue("path", pathValue);
            methodAttr.addAnnotation(requestMapping);
            //@ResponseBody
            Annotation responseBody = new Annotation("org.springframework.web.bind.annotation.ResponseBody", constpool);
            methodAttr.addAnnotation(responseBody);

            // 参数上添加注解
            ParameterAnnotationsAttribute paramAttr = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
            Annotation[][] paramAnnos = new Annotation[2][1];
            //@PathVariable
            Annotation pathVariable = new Annotation("org.springframework.web.bind.annotation.PathVariable", constpool);
            pathVariable.addMemberValue("name", new StringMemberValue("application", constpool));
            paramAnnos[0][0] = pathVariable;
            //@RequestBody
            Annotation requestBody = new Annotation("org.springframework.web.bind.annotation.RequestBody", constpool);
            paramAnnos[1][0] = requestBody;
            paramAttr.setAnnotations(paramAnnos);

            info.addAttribute(methodAttr);
            info.addAttribute(paramAttr);
            methods.add(method);
        }
        return methods.toArray(new CtMethod[0]);
    }

    private CtMethod makeMapperMethod(CtClass declaring, MappedStatement ms) {
        try {
            Class<?> paramType = ms.getParameterMap().getType();
            Class<?> resultType = ms.getResultMap().getType();
            return generateMapperMethod(resultType, paramType, ms.getId(), declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CtMethod generateMapperMethod(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id.replace(".", "_"), new CtClass[]{classPool.get("java.lang.String"), classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return mapperDispatcher.execute(\"" + id + "\", $1, new Object[]{$2});");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

    private void registerRequestMapper(MybatisApplicationOptions application, String id, Object bean, MappedStatement mappedStatement) {
        try {
            Class<?> paramType = mappedStatement.getParameterMap().getType();
            Method method = bean.getClass().getMethod(id.replace(".", "_"), String.class, paramType);

            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths("/{application}/" + id.replace(".", "/")).build();

            mappingRegistry.registerMapping(requestMappingInfo, bean, method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("注册失败!", e);
        }
    }
}
