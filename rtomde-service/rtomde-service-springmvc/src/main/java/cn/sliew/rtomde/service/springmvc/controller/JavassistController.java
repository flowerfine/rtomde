package cn.sliew.rtomde.service.springmvc.controller;

import cn.sliew.rtomde.common.utils.ClassUtils;
import cn.sliew.rtomde.executor.bytecode.BeanGenerator;
import cn.sliew.rtomde.executor.bytecode.CustomizedLoaderClassPath;
import cn.sliew.rtomde.executor.mapper.MapperInvoker;
import cn.sliew.rtomde.executor.mapper.MapperMethod;
import cn.sliew.rtomde.executor.mapper.PlainMapperInvoker;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Configuration
public class JavassistController {

    private static ClassPool classPool = ClassPool.getDefault();

    private final ConcurrentMap<String, MapperInvoker> map = new ConcurrentHashMap<>(4);

    @Autowired
    private GenericWebApplicationContext ac;
    @Autowired
    private RequestMappingHandlerMapping mappingRegistry;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    static {
        classPool.appendClassPath(new CustomizedLoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    @PostConstruct
    public void makeController() {
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MapperMethod mapperMethod = new MapperMethod(configuration, mappedStatementName);
            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
        }
        ac.registerBean("cn.sliew.rtomde.executor.MapperController", makeDispatcherController());
        Object bean = ac.getBean("cn.sliew.rtomde.executor.MapperController");
        Method[] methods = bean.getClass().getMethods();
        for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
            String id = entry.getKey();
            RequestMappingInfo requestMappingInfo = RequestMappingInfo.paths("/mapper/" + id).build();
            String name = id;
            if (id.contains(".")) {
                name = id.replace(".", "_");
            }
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    mappingRegistry.registerMapping(requestMappingInfo, bean, method);
                    break;
                }
            }
        }
    }

    public Class<?> makeDispatcherController() {
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
        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("/mapper", constpool)});
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
            controllerClass.writeFile();
            return controllerClass.toClass(ClassUtils.getClassLoader(JavassistController.class), getClass().getProtectionDomain());
        } catch (CannotCompileException e) {
            log.error("create Controller:[{}] failed", controllerClass.getName(), e);
            throw new RuntimeException("create Controller:" + controllerClass.getName() + " failed.", e);
        } catch (Throwable e) {
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
        List<CtMethod> methods = new ArrayList<>(map.keySet().size());
        for (String id : map.keySet()) {
            CtMethod method = makeMapperMethod(declaring, id);
            // 方法上添加注解
            MethodInfo info = method.getMethodInfo();
            AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            // @RequestMapping
            Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
            //Mapping路径(使用方法名)
            ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
            pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + id, constpool)});
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
//            Class<?> paramType = makeParamType(mappedStatement.getParameterMap());
            Class<?> paramType = mappedStatement.getParameterMap().getType();
//            Class<?> resultType = makeResultTypes(mappedStatement.getResultMaps());
            Class<?> resultType = mappedStatement.getResultMaps().get(0).getType();
            return generateMapperMethod(resultType, paramType, id, declaring);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Class<?> makeParamType(ParameterMap map) {
        BeanGenerator paramBeanG = BeanGenerator.newInstance(this.getClass().getClassLoader());
        paramBeanG.className(map.getType().getName());
        List<ParameterMapping> parameterMappings = map.getParameterMappings();
        for (ParameterMapping mapping : parameterMappings) {
            paramBeanG.setgetter(mapping.getProperty(), mapping.getJavaType());
        }
        return paramBeanG.toClass();
    }

    private Class<?> makeResultTypes(List<ResultMap> maps) {
        if (maps.size() > 1) {
            throw new RuntimeException("only allow one resultmap");
        }
        ResultMap resultMap = maps.get(0);
        BeanGenerator paramBeanG = BeanGenerator.newInstance(this.getClass().getClassLoader());
        paramBeanG.className(resultMap.getType().getName());
        List<ResultMapping> resultMappings = resultMap.getResultMappings();
        for (ResultMapping mapping : resultMappings) {
            paramBeanG.setgetter(mapping.getProperty(), mapping.getJavaType());
        }
        return paramBeanG.toClass();
    }

    private CtMethod generateMapperMethod(Class<?> resultType, Class<?> paramType, String id, CtClass declaring) throws Exception {
        String name = id;
        if (id.contains(".")) {
            name = id.replace(".", "_");
        }
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), name, new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();
        methodBody.append("{");
        methodBody.append("return mapperDispatcher.execute(\"" + id + "\", $args);");
        methodBody.append("}");
        method.setBody(methodBody.toString());
        return method;
    }

}
