package cn.sliew.rtomde.service.springmvc.controller;

import cn.sliew.rtomde.executor.bytecode.BeanGenerator;
import cn.sliew.rtomde.executor.mapper.MapperInvoker;
import cn.sliew.rtomde.executor.mapper.MapperMethod;
import cn.sliew.rtomde.executor.mapper.PlainMapperInvoker;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
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
        ClassClassPath path = new ClassClassPath(JavassistController.class);
        classPool.insertClassPath(path);
    }

    @PostConstruct
    public void makeController() {
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
        for (String mappedStatementName : mappedStatementNames) {
            MapperMethod mapperMethod = new MapperMethod(configuration, mappedStatementName);
            map.putIfAbsent(mappedStatementName, new PlainMapperInvoker(mapperMethod));
        }
        for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
            String key = entry.getKey();
        }
    }

    public void makeDispatcherController() {
        CtClass controllerClass = classPool.makeClass("cn.sliew.rtomde.executor.MapperController");
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
        memberValue.setValue(new StringMemberValue[]{new StringMemberValue("/mapper", constpool)});
        requestMapping.addMemberValue("path", memberValue);
        classAttr.addAnnotation(requestMapping);

        ccFile.addAttribute(classAttr);
        try {
            // mapper dispatcher
//            CtField mapField = CtField.make(String.format("private %s map;", map.getClass().getName()), controllerClass);
//            controllerClass.addField(mapField);
            controllerClass.addField(makeAutowiredField(controllerClass, constpool));
            // method
            CtMethod[] methods = makeRequestMapping(controllerClass, constpool);
            for (CtMethod m : methods) {
                controllerClass.addMethod(m);
            }
            Class<?> clazz = controllerClass.toClass();
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
        try {
            CtClass parentCtClass = classPool.get(declaring.getName());

            CtMethod[] declaredMethods = parentCtClass.getDeclaredMethods();
            CtMethod[] methods = new CtMethod[declaredMethods.length];
            for (Map.Entry<String, MapperInvoker> entry : map.entrySet()) {
                String id = entry.getKey();
                MapperInvoker invoker = entry.getValue();

            }
            for (int i = 0; i < declaredMethods.length; i++) {
                // 生成方法体
                CtMethod method = generatedMetaMethod(declaredMethods[i], declaring);
                // 方法上添加注解
                MethodInfo info = method.getMethodInfo();
                AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
                // 添加 @RequestMapping注解
                Annotation requestMapping = new Annotation("org.springframework.web.bind.annotation.RequestMapping", constpool);
                //Mapping路径(使用方法名)
                ArrayMemberValue pathValue = new ArrayMemberValue(constpool);
                pathValue.setValue(new StringMemberValue[]{new StringMemberValue("/" + declaredMethods[i].getName(), constpool)});
                requestMapping.addMemberValue("path", pathValue);
                methodAttr.addAnnotation(requestMapping);
                // 添加@ResponseBody注解
                Annotation responseBody = new Annotation("org.springframework.web.bind.annotation.ResponseBody", constpool);
                methodAttr.addAnnotation(responseBody);
                //参数上注解@RequestBody
                Annotation requestBody = new Annotation("org.springframework.web.bind.annotation.RequestBody", constpool);
                ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
                javassist.bytecode.annotation.Annotation[][] anno = new javassist.bytecode.annotation.Annotation[][]{{requestBody}};
                parameterAnnotationsAttribute.setAnnotations(anno);

                info.addAttribute(methodAttr);
                info.addAttribute(parameterAnnotationsAttribute);
                methods[i] = method;
            }
            return methods;
        } catch (CannotCompileException | NotFoundException e) {
            log.error("create request mapping failed for interface:[{}]", interfaceClass.getName(), e);
            throw new RuntimeException("create request mapping failed for interface:" + interfaceClass.getName(), e);
        }
    }

    private CtMethod makeMapperMethod(CtClass declaring, String id, MapperInvoker invoker) {
        MappedStatement mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
        Class<?> paramType = makeParamType(mappedStatement.getParameterMap());
        Class<?> resultType = makeResultTypes(mappedStatement.getResultMaps());
        return generateMapperMethod(resultType, paramType, id, declaring);
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
        CtMethod method = new CtMethod(classPool.get(resultType.getName()), id, new CtClass[]{classPool.get(paramType.getName())}, declaring);
        StringBuilder methodBody = new StringBuilder();



        return method;
    }

}
